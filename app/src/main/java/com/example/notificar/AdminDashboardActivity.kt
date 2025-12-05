package com.example.notificar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.notificar.databinding.ActivityAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {

        // ---------------------------------------------------------
        // 1. Botão: Gerenciar Usuários
        // ---------------------------------------------------------
        binding.cardUsers.setOnClickListener {
            val intent = Intent(this, AdminUsersActivity::class.java)
            startActivity(intent)
        }

        // ---------------------------------------------------------
        // 2. Botão: Moderar Depoimentos
        // ---------------------------------------------------------
        binding.cardDepoimentos.setOnClickListener {
            val intent = Intent(this, AdministradoresDepoimentosActivity::class.java)
            startActivity(intent)
        }

        // ---------------------------------------------------------
        // 3. Botão: Notificar Todos
        // ---------------------------------------------------------
        binding.cardNotify.setOnClickListener {
            val intent = Intent(this, AdminNotificationActivity::class.java)
            startActivity(intent)
        }

        // ---------------------------------------------------------
        // 4. Botão: Sair (Atualizado com Logout Seguro)
        // ---------------------------------------------------------
        binding.cardLogout.setOnClickListener {
            // 1. Desloga do Firebase Auth
            FirebaseAuth.getInstance().signOut()

            // 2. Prepara a volta para a tela de Login
            val intent = Intent(this, AdminLoginActivity::class.java)

            // 3. Limpa a pilha de telas (impede que o botão "Voltar" retorne ao Admin)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }
}