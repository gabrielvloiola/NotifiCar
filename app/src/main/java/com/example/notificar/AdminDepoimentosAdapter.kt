package com.example.notificar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminDepoimentosAdapter(
    private val listaDepoimentos: MutableList<Depoimento>,
    private val onDeleteClick: (Depoimento) -> Unit
) : RecyclerView.Adapter<AdminDepoimentosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView = view.findViewById(R.id.tvUserName)
        val tvTexto: TextView = view.findViewById(R.id.tvContent)
        val tvTempo: TextView = view.findViewById(R.id.tvDate) // Usamos o TextView de data para mostrar o tempo
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_depoimento_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaDepoimentos[position]

        holder.tvNome.text = item.nome
        holder.tvTexto.text = item.texto

        // CORREÇÃO AQUI: Mudamos de .data para .tempo
        holder.tvTempo.text = item.tempo

        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount() = listaDepoimentos.size

    fun removerItem(depoimento: Depoimento) {
        val index = listaDepoimentos.indexOf(depoimento)
        if (index != -1) {
            listaDepoimentos.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}