package com.example.notificar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SolicitacaoAdapter(private val listaSolicitacoes: List<Solicitacao>) : RecyclerView.Adapter<SolicitacaoAdapter.SolicitacaoViewHolder>() {

    inner class SolicitacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val motivo: TextView = itemView.findViewById(R.id.tvMotivo)
        val modeloCarro: TextView = itemView.findViewById(R.id.tvModeloCarro)
        val statusIcon: ImageView = itemView.findViewById(R.id.imgStatusIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolicitacaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_solicitacao, parent, false)
        return SolicitacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: SolicitacaoViewHolder, position: Int) {
        val solicitacao = listaSolicitacoes[position]

        holder.motivo.text = solicitacao.motivo

        // Requisito 2: Mostra o modelo e a placa
        val modelo = solicitacao.modeloCarro ?: "Carro Desconhecido"
        holder.modeloCarro.text = "$modelo (${solicitacao.placa})"

        // Mostra o ícone correto
        if (solicitacao.status == "recebida") {
            holder.statusIcon.setImageResource(R.drawable.ic_alerta)
        } else {
            holder.statusIcon.setImageResource(R.drawable.ic_check) // Crie um ícone 'check'
        }
    }

    override fun getItemCount() = listaSolicitacoes.size
}