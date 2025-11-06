package com.example.notificar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 1. ADICIONE O "onItemClick" AO CONSTRUTOR
class CarroAdapter(
    private val listaCarros: List<Carro>,
    private val onItemClick: (Carro) -> Unit  // Função que será chamada no clique
) : RecyclerView.Adapter<CarroAdapter.CarroViewHolder>() {

    inner class CarroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val modelo: TextView = itemView.findViewById(R.id.tvModeloCarro)
        val placa: TextView = itemView.findViewById(R.id.tvPlacaCarro)
        val logo: ImageView = itemView.findViewById(R.id.imgMarcaLogo)

        // 2. ADICIONE ESTA FUNÇÃO "BIND"
        fun bind(carro: Carro) {
            modelo.text = "${carro.modelo} (${carro.cor})"
            placa.text = "Placa: ${carro.placa}"

            val marcaLowerCase = carro.marca.lowercase()
            val logoDrawable = when {
                marcaLowerCase.contains("bmw") -> R.drawable.logo_bmw
                marcaLowerCase.contains("byd") -> R.drawable.logo_byd
                marcaLowerCase.contains("chevrolet") -> R.drawable.logo_chevrolet
                marcaLowerCase.contains("fiat") -> R.drawable.logo_fiat
                marcaLowerCase.contains("ford") -> R.drawable.logo_ford
                marcaLowerCase.contains("honda") -> R.drawable.logo_honda
                marcaLowerCase.contains("hyundai") -> R.drawable.logo_hyundai
                marcaLowerCase.contains("mercedes") -> R.drawable.logo_mercedes
                marcaLowerCase.contains("peugeot") -> R.drawable.logo_peugeot
                marcaLowerCase.contains("porsche") -> R.drawable.logo_porsche
                marcaLowerCase.contains("renault") -> R.drawable.logo_renault
                marcaLowerCase.contains("toyota") -> R.drawable.logo_toyota
                marcaLowerCase.contains("volkswagen") -> R.drawable.logo_volkswagen
                else -> R.drawable.logo_nobrand
            }
            logo.setImageResource(logoDrawable)

            // 3. CONFIGURE O CLIQUE NO ITEM INTEIRO
            itemView.setOnClickListener {
                onItemClick(carro)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarroViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carro, parent, false)
        return CarroViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarroViewHolder, position: Int) {
        // 4. CHAME A FUNÇÃO "BIND"
        holder.bind(listaCarros[position])
    }

    override fun getItemCount() = listaCarros.size
}