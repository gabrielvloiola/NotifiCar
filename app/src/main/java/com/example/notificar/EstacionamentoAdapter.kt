package com.example.notificar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EstacionamentoAdapter(private val listaEstacionamentos: List<Estacionamento>) : RecyclerView.Adapter<EstacionamentoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Estes IDs (tvNomeEstacionamento, etc.) devem existir no seu layout 'item_estacionamento.xml'
        val nome: TextView = itemView.findViewById(R.id.tvNomeEstacionamento)
        val endereco: TextView = itemView.findViewById(R.id.tvEnderecoEstacionamento)
        val distancia: TextView = itemView.findViewById(R.id.tvDistancia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Certifique-se de que criou o ficheiro 'item_estacionamento.xml' no passo anterior
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_estacionamento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val estacionamento = listaEstacionamentos[position]
        holder.nome.text = estacionamento.nome
        holder.endereco.text = estacionamento.endereco
        holder.distancia.text = estacionamento.distancia
    }

    override fun getItemCount() = listaEstacionamentos.size
}