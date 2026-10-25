package com.example.inventariocarniceria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.example.inventariocarniceria.data.scale.TcpScaleManager
import com.example.inventariocarniceria.data.local.AppDatabase
import com.example.inventariocarniceria.data.repository.InventoryRepository
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.view.KeyEvent
import com.example.inventariocarniceria.service.ScaleBackgroundService
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModelFactory

class MainActivity : ComponentActivity() {

    private val barcodeScanBuffer = StringBuilder()
    private var lastKeyTime = 0L

    private val viewModel: InventoryViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val scaleManager = TcpScaleManager.getInstance(applicationContext)
        val repository = InventoryRepository(
            productDao = database.productDao(),
            captureDao = database.inventoryCaptureDao(),
            stockEntryDao = database.stockEntryDao(),
            branchOrderDao = database.branchOrderDao(),
            localProductDao = database.localProductDao(),
            supplierDao = database.supplierDao(),
            branchDao = database.branchDao(),
            priceAuditDao = database.priceAuditDao(),
            despieceDao = database.despieceDao(),
            scaleManager = scaleManager
        )
        InventoryViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solicitar permiso de notificaciones en Android 13+ para servicio en segundo plano
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Iniciar servicio en segundo plano para mantener la conexión TCP activa con pantalla apagada
        ScaleBackgroundService.startService(applicationContext)
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            SgniaTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF141312))
                        .systemBarsPadding(),
                    color = SgniaTheme.colors.backgroundPrimary
                ) {
                    MainNavigation(viewModel = viewModel)
                }
            }
        }
    }

    /**
     * Captura automática de lectores de código de barras USB y Bluetooth (modo pistola/teclado HID).
     * Los lectores emiten caracteres en ráfaga (<50ms) seguidos de Enter (KEYCODE_ENTER).
     */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()
            if (now - lastKeyTime > 400L) {
                barcodeScanBuffer.clear()
            }
            lastKeyTime = now

            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                val barcode = barcodeScanBuffer.toString().trim()
                barcodeScanBuffer.clear()
                if (barcode.length >= 3) {
                    viewModel.processScannedBarcode(barcode)
                    return true
                }
            } else {
                val char = event.unicodeChar
                if (char > 0 && (char.toChar().isLetterOrDigit() || char.toChar() in "-_.:;=/+")) {
                    barcodeScanBuffer.append(char.toChar())
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
