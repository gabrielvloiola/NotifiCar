package com.example.notificar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.notificar.databinding.ItemUsuarioAdminBinding

class AdminUsersAdapter(
    private val listaUsuarios: MutableList<Usuario>,
    private val onActionClick: (Usuario) -> Unit
) : RecyclerView.Adapter<AdminUsersAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemUsuarioAdminBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUsuarioAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = listaUsuarios[position]

        holder.binding.tvUserName.text = usuario.nome
        holder.binding.tvUserEmail.text = usuario.email

        // Colora o botão de Vermelho para indicar PERIGO (Excluir)
        holder.binding.btnBlock.setColorFilter(Color.RED)

        holder.binding.btnBlock.setOnClickListener {
            onActionClick(usuario)
        }
    }

    override fun getItemCount() = listaUsuarios.size

    // Função separada para remover da lista visualmente
    fun removerUsuario(usuario: Usuario) {
        val position = listaUsuarios.indexOf(usuario)
        if (position != -1) {
            listaUsuarios.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}