package com.example.inventariocarniceria.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.inventariocarniceria.MainActivity
import com.example.inventariocarniceria.R
import com.example.inventariocarniceria.data.scale.TcpScaleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class ScaleBackgroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    companion object {
        const val CHANNEL_ID = "sgnia_scale_channel"
        const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            try {
                val intent = Intent(context, ScaleBackgroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, ScaleBackgroundService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Adquirir WakeLock para que el CPU se mantenga despierto con la pantalla apagada
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SgniaCarniceria:ScaleServiceWakeLock"
        )?.apply {
            setReferenceCounted(false)
            try {
                acquire(24 * 60 * 60 * 1000L) // 24 horas
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Adquirir WifiLock para mantener el socket TCP activo en Wi-Fi
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        wifiLock = wifiManager?.createWifiLock(
            WifiManager.WIFI_MODE_FULL_HIGH_PERF,
            "SgniaCarniceria:ScaleServiceWifiLock"
        )?.apply {
            setReferenceCounted(false)
            try {
                acquire()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Iniciar Foreground Service
        val notification = buildNotification("Iniciando servicio de básculas en red...")
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Asegurar que el TcpScaleManager esté escuchando todas las básculas en paralelo
        val scaleManager = TcpScaleManager.getInstance(applicationContext)
        scaleManager.startAllScaleListeners()

        // Observar cambios de estado para actualizar la notificación en vivo
        serviceScope.launch {
            scaleManager.scalesList.collectLatest { list ->
                val connectedCount = list.count { it.isConnected }
                val total = list.size
                val currentWeight = scaleManager.scaleState.value.currentWeight
                val activeDevice = scaleManager.scaleState.value.deviceName
                val text = "🟢 $connectedCount/$total básculas en línea • $activeDevice: ${String.format(Locale.US, "%.3f", currentWeight)} kg"
                updateNotification(text)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Retornar START_STICKY para que Android reviva el servicio automáticamente si fuera destruido
        val scaleManager = TcpScaleManager.getInstance(applicationContext)
        scaleManager.startAllScaleListeners()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (_: Exception) {}
        try {
            if (wifiLock?.isHeld == true) wifiLock?.release()
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio de Básculas TCP SGNIA",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene la conexión Wi-Fi TCP activa con las básculas incluso con la pantalla apagada o app minimizada"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SGNIA Básculas TCP en Línea")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(text))
    }
}
