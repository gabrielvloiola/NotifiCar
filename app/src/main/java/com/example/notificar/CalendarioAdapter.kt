package com.example.notificar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notificar.databinding.ItemDiaCalendarioBinding
import java.util.Calendar

// Modelo de dados (mantido igual)
data class DiaCalendario(
    val dia: Int,
    val temIncidente: Boolean,
    val listaIncidentes: List<String> = emptyList()
)

class CalendarioAdapter(
    private val dias: List<DiaCalendario>,
    private val onDiaClick: (DiaCalendario) -> Unit
) : RecyclerView.Adapter<CalendarioAdapter.DiaViewHolder>() {

    // Pega o dia de hoje para saber o que é futuro
    private val diaHoje = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    inner class DiaViewHolder(val binding: ItemDiaCalendarioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaViewHolder {
        val binding = ItemDiaCalendarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        val item = dias[position]

        // Configura o número do dia
        if (item.dia == 0) {
            holder.binding.tvDiaNumero.text = ""
            holder.binding.tvDiaNumero.background = null
            holder.itemView.isClickable = false
        } else {
            holder.binding.tvDiaNumero.text = item.dia.toString()

            // --- NOVA LÓGICA DE CORES ---
            when {
                item.dia > diaHoje -> {
                    // Futuro: Cinza
                    holder.binding.tvDiaNumero.setBackgroundResource(R.drawable.bolinha_cinza)
                    // Opcional: Desativar clique no futuro
                    // holder.itemView.isClickable = false
                }
                item.temIncidente -> {
                    // Passado/Hoje com problema: Vermelho
                    holder.binding.tvDiaNumero.setBackgroundResource(R.drawable.bolinha_vermelha)
                }
                else -> {
                    // Passado/Hoje limpo: Verde
                    holder.binding.tvDiaNumero.setBackgroundResource(R.drawable.bolinha_verde)
                }
            }

            holder.itemView.setOnClickListener {
                onDiaClick(item)
            }
        }
    }

    override fun getItemCount() = dias.size
}