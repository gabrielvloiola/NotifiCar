package com.example.notificar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Chamado quando uma nova notificação chega
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Verifica se a mensagem tem dados para mostrar
        remoteMessage.notification?.let {
            mostrarNotificacao(it.title, it.body)
        }
    }

    // Chamado quando o Token do dispositivo muda (ou é criado pela 1ª vez)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Aqui deveríamos enviar o novo token para o Firestore, se o user estiver logado
        // (Faremos isso na TelaPrincipal para garantir)
    }

    private fun mostrarNotificacao(titulo: String?, mensagem: String?) {
        val intent = Intent(this, TelaPrincipal::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "notificar_channel_id"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notificar) // Use um ícone que já tenha
            .setContentTitle(titulo ?: "NotifiCar")
            .setContentText(mensagem)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Para Android Oreo (API 26) e superior, é preciso criar um canal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificações de Solicitação",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}