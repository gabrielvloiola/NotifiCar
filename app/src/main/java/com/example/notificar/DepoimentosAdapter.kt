package com.example.notificar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DepoimentosAdapter(
    private val lista: List<Depoimento>
) : RecyclerView.Adapter<DepoimentosAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView = view.findViewById(R.id.tvUserName)
        val tvTexto: TextView = view.findViewById(R.id.tvContent)
        val tvTempo: TextView = view.findViewById(R.id.tvDate)
        // Note que NÃO tem botão de delete aqui
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Você pode usar o mesmo layout 'item_depoimento_admin' se quiser,
        // mas idealmente teria um 'item_depoimento_user' sem o botão de lixeira.
        // Se usar o layout do admin, o botão de lixeira vai aparecer mas não vai fazer nada.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_depoimento_admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNome.text = item.nome
        holder.tvTexto.text = item.texto
        holder.tvTempo.text = item.tempo

        // Se estiver usando o layout do admin, esconda o botão de deletar:
        holder.itemView.findViewById<View>(R.id.btnDelete)?.visibility = View.GONE
    }

    override fun getItemCount() = lista.size
}