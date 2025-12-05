package com.example.notificar

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.notificar.databinding.ActivityIncidentesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class IncidentesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncidentesBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listaDias = mutableListOf<DiaCalendario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Configura a base do calendário (dias verdes)
        configurarCalendarioBase()

        // 2. Busca os dados reais no Firebase
        buscarIncidentesNoFirebase()
    }

    // AQUI ESTAVA O ERRO: O nome agora está igual à chamada acima
    private fun configurarCalendarioBase() {
        val cal = Calendar.getInstance()
        val maxDias = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Define título (ex: Outubro 2023)
        val nomeMes = android.text.format.DateFormat.format("MMMM yyyy", cal.time)
        binding.tvMesAno.text = nomeMes.toString().uppercase()

        // Popula a lista inicial com dias "Verdes" (sem incidentes ainda)
        listaDias.clear()

        // Preenche de 1 até o último dia do mês
        for (i in 1..maxDias) {
            listaDias.add(DiaCalendario(i, false))
        }

        atualizarAdapter()
    }

    private fun buscarIncidentesNoFirebase() {
        val userId = auth.currentUser?.uid ?: return
        val cal = Calendar.getInstance()

        // Busca solicitações onde eu sou o destinatário
        db.collection("solicitacoes")
            .whereEqualTo("destinatarioUserId", userId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) return@addOnSuccessListener

                // Mapa para guardar incidentes por dia: Dia -> Lista de Motivos
                val incidentesMap = hashMapOf<Int, MutableList<String>>()

                for (document in result) {
                    val timestamp = document.getTimestamp("timestamp")
                    val motivo = document.getString("motivo") ?: "Incidente"

                    if (timestamp != null) {
                        val dataIncidente = timestamp.toDate()
                        val calInc = Calendar.getInstance()
                        calInc.time = dataIncidente

                        // Verifica se o incidente é deste mês e ano
                        if (calInc.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                            calInc.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {

                            val diaDoMes = calInc.get(Calendar.DAY_OF_MONTH)

                            if (!incidentesMap.containsKey(diaDoMes)) {
                                incidentesMap[diaDoMes] = mutableListOf()
                            }
                            incidentesMap[diaDoMes]?.add(motivo)
                        }
                    }
                }

                // Agora atualiza a nossa lista principal com os dados encontrados
                for (i in listaDias.indices) {
                    val diaNumero = listaDias[i].dia
                    if (incidentesMap.containsKey(diaNumero)) {
                        // Marca como VERMELHO e salva os motivos
                        val listaMotivos = incidentesMap[diaNumero] ?: emptyList()
                        // Atualiza o item da lista
                        listaDias[i] = DiaCalendario(diaNumero, true, listaMotivos)
                    }
                }

                // Avisa o adapter que os dados mudaram
                binding.rvCalendario.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar incidentes", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarAdapter() {
        // Configura RecyclerView com 7 colunas (dias da semana)
        binding.rvCalendario.layoutManager = GridLayoutManager(this, 7)
        binding.rvCalendario.adapter = CalendarioAdapter(listaDias) { diaClicado ->
            mostrarDetalhes(diaClicado)
        }
    }

    private fun mostrarDetalhes(dia: DiaCalendario) {
        if (dia.temIncidente) {
            val texto = StringBuilder()
            texto.append("Incidentes no dia ${dia.dia}:\n\n")

            dia.listaIncidentes.forEach { motivo ->
                texto.append("• $motivo\n")
            }

            binding.tvDetalhesIncidente.text = texto.toString()
            binding.tvDetalhesIncidente.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            binding.tvDetalhesIncidente.text = "Dia ${dia.dia}: Nenhum incidente registrado! 🎉"
            binding.tvDetalhesIncidente.setTextColor(getColor(android.R.color.holo_green_dark))

        }
    }
}