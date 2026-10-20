package com.example.inventariocarniceria.utils

import android.content.Context
import android.content.Intent
import com.example.inventariocarniceria.data.local.InventoryCaptureEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppExporter {

    fun generateFormattedMessage(orders: List<InventoryCaptureEntity>): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.append("🥩 *PEDIDO DE SURTIDO - CARNICERÍA* 🥩\n")
        sb.append("📅 Fecha: $currentDate\n")
        sb.append("------------------------------------\n\n")

        if (orders.isEmpty()) {
            sb.append("✅ *Sin faltantes registrados. El stock está completo.*\n")
        } else {
            orders.forEach { item ->
                val codeFormatted = String.format(Locale.US, "%03d", item.productCode)
                val shortageFormatted = String.format(Locale.US, "%.2f", item.shortage)

                val toOrderFormatted = if (item.orderedUnits > 0) {
                    when (item.unitType) {
                        "BARRA" -> "${item.orderedUnits.toInt()} BARRA(S) (~$shortageFormatted kg)"
                        "PAQUETE" -> "${item.orderedUnits.toInt()} PAQUETE(S) (~$shortageFormatted kg)"
                        "BOLSA" -> "${item.orderedUnits.toInt()} BOLSA(S) (~$shortageFormatted kg)"
                        "CAJA" -> "${item.orderedUnits.toInt()} CAJA(S) (~$shortageFormatted kg)"
                        "BOTE" -> "${item.orderedUnits.toInt()} BOTE(S) (~$shortageFormatted kg)"
                        "PIEZA" -> "${item.orderedUnits.toInt()} PIEZA(S)"
                        else -> String.format(Locale.US, "%.3f kg", item.shortage)
                    }
                } else {
                    String.format(Locale.US, "%.3f kg", item.shortage)
                }

                sb.append("📌 *[$codeFormatted] ${item.productName.uppercase()}*\n")
                sb.append("   • 🚨 *A PEDIR: $toOrderFormatted*\n\n")
            }
        }

        sb.append("------------------------------------\n")
        sb.append("📱 _Generado automáticamente desde App Inventario Carnicería_")
        return sb.toString()
    }

    fun shareToWhatsApp(context: Context, orders: List<InventoryCaptureEntity>) {
        val message = generateFormattedMessage(orders)
        shareRawText(context, message)
    }

    fun shareRawText(context: Context, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Compartir Orden de Carnicería")
        context.startActivity(shareIntent)
    }
}
