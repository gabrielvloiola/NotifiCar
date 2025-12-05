package com.example.notificar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notificar.databinding.ActivityEstacionamentoBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class EstacionamentoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstacionamentoBinding
    private lateinit var adapter: EstacionamentoAdapter
    private val listaEstacionamentos = mutableListOf<Estacionamento>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var locationOverlay: MyLocationNewOverlay
    private val client = OkHttpClient()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.any { it.value }) {
                activarLocalizacaoNoMapa()
                buscarEstacionamentosReais()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().load(this, sharedPrefs)
        Configuration.getInstance().userAgentValue = packageName

        binding = ActivityEstacionamentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupRecyclerView()
        setupMapa()
        verificarPermissoesIniciais()

        // --- CORREÇÃO: Recuperar a posição do carro ao abrir a tela ---
        verificarCarroEstacionado()
        // -------------------------------------------------------------

        binding.imgVoltar.setOnClickListener { finish() }

        binding.btnEstacioneiAqui.setOnClickListener {
            salvarOndeEstacionei()
        }
    }

    // --- NOVA FUNÇÃO QUE FALTAVA ---
    private fun verificarCarroEstacionado() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Busca o campo 'localizacaoEstacionado' do Firestore
                    val geoPoint = document.getGeoPoint("localizacaoEstacionado")

                    if (geoPoint != null) {
                        // Se existir, desenha o marcador no mapa
                        atualizarMapaComCarro(geoPoint.latitude, geoPoint.longitude)
                        // Opcional: Centralizar no carro
                        // binding.mapView.controller.animateTo(OsmGeoPoint(geoPoint.latitude, geoPoint.longitude))
                    }
                }
            }
            .addOnFailureListener {
                // Falha silenciosa ou log
            }
    }

    private fun verificarPermissoesIniciais() {
        if (temPermissao()) {
            activarLocalizacaoNoMapa()
            buscarEstacionamentosReais()
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun temPermissao(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun buscarEstacionamentosReais() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val pontoUsuario = OsmGeoPoint(location.latitude, location.longitude)
                    binding.mapView.controller.animateTo(pontoUsuario)
                    binding.mapView.controller.setZoom(16.0)
                    buscarNaAPI(location.latitude, location.longitude)
                }
            }
        } catch (e: SecurityException) { }
    }

    private fun buscarNaAPI(lat: Double, lon: Double) {
        val url = "https://overpass-api.de/api/interpreter?data=[out:json];nwr(around:2000,$lat,$lon)[amenity=parking];out center;"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val jsonString = response.body?.string()

                if (jsonString != null) {
                    val jsonObject = JSONObject(jsonString)
                    val elements = jsonObject.getJSONArray("elements")

                    withContext(Dispatchers.Main) {
                        listaEstacionamentos.clear()

                        // Opcional: Limpar marcadores de estacionamentos antigos (mas manter o carro)
                        // binding.mapView.overlays.removeIf { it is Marker && it.title != "Meu Carro" }

                        for (i in 0 until elements.length()) {
                            val item = elements.getJSONObject(i)
                            var latEst = 0.0
                            var lonEst = 0.0

                            if (item.has("lat") && item.has("lon")) {
                                latEst = item.getDouble("lat")
                                lonEst = item.getDouble("lon")
                            } else if (item.has("center")) {
                                val center = item.getJSONObject("center")
                                latEst = center.getDouble("lat")
                                lonEst = center.getDouble("lon")
                            } else {
                                continue
                            }

                            val tags = item.optJSONObject("tags")
                            var nome = tags?.optString("name")

                            if (nome.isNullOrEmpty()) {
                                val access = tags?.optString("access")
                                val operator = tags?.optString("operator")

                                nome = when {
                                    !operator.isNullOrEmpty() -> "Estacionamento $operator"
                                    access == "private" -> "Estacionamento Privado"
                                    access == "customers" -> "Estacionamento Clientes"
                                    else -> "Estacionamento"
                                }
                            }

                            val distancia = calcularDistancia(lat, lon, latEst, lonEst)
                            listaEstacionamentos.add(Estacionamento(nome!!, "Toque para ver no mapa", distancia))
                            adicionarMarcador(latEst, lonEst, nome)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Erro silencioso ou Toast
                }
            }
        }
    }

    private fun calcularDistancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val resultados = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, resultados)
        val metros = resultados[0].toInt()
        return if (metros > 1000) "${String.format("%.1f", metros / 1000.0)} km" else "$metros m"
    }

    private fun salvarOndeEstacionei() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    salvarNoFirestore(location.latitude, location.longitude)
                } else {
                    Toast.makeText(this, "Localização não encontrada. Ative o GPS.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) { }
    }

    private fun salvarNoFirestore(lat: Double, lng: Double) {
        val userId = auth.currentUser?.uid ?: return
        val ponto = GeoPoint(lat, lng)
        val dados = hashMapOf("localizacaoEstacionado" to ponto)

        db.collection("users").document(userId)
            .set(dados, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Local salvo!", Toast.LENGTH_SHORT).show()
                atualizarMapaComCarro(lat, lng)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupMapa() {
        val map = binding.mapView
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(15.0)
        map.controller.setCenter(OsmGeoPoint(-15.7942, -47.8822))
    }

    private fun setupRecyclerView() {
        adapter = EstacionamentoAdapter(listaEstacionamentos)
        binding.recyclerViewEstacionamentos.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewEstacionamentos.adapter = adapter
    }

    private fun adicionarMarcador(lat: Double, long: Double, titulo: String) {
        val marcador = Marker(binding.mapView)
        marcador.position = OsmGeoPoint(lat, long)
        marcador.title = titulo
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        binding.mapView.overlays.add(marcador)
    }

    private fun atualizarMapaComCarro(lat: Double, lng: Double) {
        // Remove marcador anterior do carro para não duplicar
        binding.mapView.overlays.removeIf { it is Marker && it.title == "Meu Carro" }

        val ponto = OsmGeoPoint(lat, lng)
        val marcador = Marker(binding.mapView)
        marcador.position = ponto
        marcador.title = "Meu Carro"
        marcador.icon = ContextCompat.getDrawable(this, R.drawable.ic_carro)
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        binding.mapView.overlays.add(marcador)
        binding.mapView.invalidate()
    }

    private fun activarLocalizacaoNoMapa() {
        if (!::locationOverlay.isInitialized) {
            locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), binding.mapView)
            locationOverlay.enableMyLocation()
            binding.mapView.overlays.add(locationOverlay)
        }
    }

    override fun onResume() { super.onResume(); binding.mapView.onResume() }
    override fun onPause() { super.onPause(); binding.mapView.onPause() }
}