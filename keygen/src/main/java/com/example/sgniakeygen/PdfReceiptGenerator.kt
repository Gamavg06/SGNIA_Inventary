package com.example.sgniakeygen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReceiptGenerator {

    fun generateReceiptPdf(context: Context, owner: OwnerRecord): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Formato A4 estándar
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. FONDO BLANCO PURO
        canvas.drawColor(Color.WHITE)

        // 2. ENCABEZADO SUPERIOR CORPORATIVO (Franja Carbón Ejecutivo)
        paint.color = Color.rgb(20, 19, 18)
        canvas.drawRect(0f, 0f, 595f, 90f, paint)

        // Línea acento verde teal / esmeralda
        paint.color = Color.rgb(74, 222, 128)
        canvas.drawRect(0f, 90f, 595f, 94f, paint)

        // LOGO OFICIAL SGNIA
        val logoBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.sglogo)?.let {
                Bitmap.createScaledBitmap(it, 64, 64, true)
            }
        } catch (_: Exception) { null }

        if (logoBitmap != null) {
            canvas.drawBitmap(logoBitmap, 30f, 13f, paint)
        }

        // TEXTO ENCABEZADO
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText("SGNIA COMPANY", 105f, 38f, paint)

        paint.color = Color.rgb(238, 221, 200) // Crema
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 9f
        canvas.drawText("SOFTWARE QUE PROTEGE Y HACE CRECER TU NEGOCIO", 105f, 53f, paint)

        paint.color = Color.rgb(180, 180, 180)
        paint.textSize = 8.5f
        canvas.drawText("https://sgnia.xyz • WhatsApp: +52 276 109 5688 • Nopalucan, Puebla", 105f, 68f, paint)

        // FOLIO Y FECHA (A la derecha del encabezado)
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.RIGHT
        val folio = "FOLIO: REC-${owner.id.take(6).uppercase(Locale.US)}"
        canvas.drawText(folio, 565f, 38f, paint)

        paint.color = Color.rgb(238, 221, 200)
        paint.textSize = 8.5f
        val fechaEmision = "Fecha: " + SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText(fechaEmision, 565f, 53f, paint)
        paint.textAlign = Paint.Align.LEFT

        // 3. TÍTULO DEL DOCUMENTO
        var currentY = 125f
        paint.color = Color.rgb(30, 30, 30)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        canvas.drawText("COMPROBANTE OFICIAL DE LICENCIAMIENTO INDUSTRIAL", 30f, currentY, paint)

        // SELLO DE ESTATUS DE PAGO
        val isPaid = owner.paymentStatus == "PAGADO"
        val badgeColor = if (isPaid) Color.rgb(46, 125, 50) else Color.rgb(211, 47, 47)
        val badgeBg = if (isPaid) Color.rgb(232, 245, 233) else Color.rgb(255, 235, 238)
        val badgeText = if (isPaid) "● PAGADO" else "● PENDIENTE DE PAGO"

        paint.color = badgeBg
        val badgeRect = RectF(440f, currentY - 18f, 565f, currentY + 6f)
        canvas.drawRoundRect(badgeRect, 6f, 6f, paint)
        paint.color = badgeColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(badgeRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(badgeText, 502f, currentY - 4f, paint)
        paint.textAlign = Paint.Align.LEFT

        currentY += 25f

        // 4. CAJA: DATOS DEL CLIENTE / PROPIETARIO
        paint.color = Color.rgb(248, 248, 248)
        val clientBox = RectF(30f, currentY, 565f, currentY + 75f)
        canvas.drawRoundRect(clientBox, 8f, 8f, paint)
        paint.color = Color.rgb(220, 220, 220)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(clientBox, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(80, 80, 80)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("DATOS DEL TITULAR Y CARNICERÍA:", 45f, currentY + 20f, paint)

        paint.color = Color.rgb(20, 20, 20)
        paint.textSize = 11f
        canvas.drawText("Dueño: ${owner.ownerName}", 45f, currentY + 38f, paint)
        canvas.drawText("Empresa: ${owner.companyName.ifBlank { "Carnicería" }}", 45f, currentY + 54f, paint)

        canvas.drawText("WhatsApp: ${owner.phoneWhatsApp.ifBlank { "No registrado" }}", 320f, currentY + 38f, paint)
        canvas.drawText("Correo: ${owner.email.ifBlank { "No registrado" }}", 320f, currentY + 54f, paint)

        currentY += 95f

        // 5. CAJA: RESUMEN DE CONTRATO Y MEMBRESÍA
        paint.color = Color.rgb(248, 248, 248)
        val planBox = RectF(30f, currentY, 565f, currentY + 45f)
        canvas.drawRoundRect(planBox, 8f, 8f, paint)
        paint.color = Color.rgb(220, 220, 220)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(planBox, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(20, 20, 20)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        canvas.drawText("Plan: ${owner.membershipTier.label}", 45f, currentY + 18f, paint)
        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 9f
        canvas.drawText("Modalidad: ${owner.membershipTier.badge}", 45f, currentY + 33f, paint)

        paint.color = Color.rgb(20, 20, 20)
        paint.textSize = 10f
        canvas.drawText("Total Sucursales: ${owner.branches.size} tiendas", 320f, currentY + 18f, paint)
        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 9f
        canvas.drawText("Método de Pago: ${owner.paymentMethod}", 320f, currentY + 33f, paint)

        currentY += 65f

        // 6. TABLA: DESGLOSE DE SUCURSALES Y CLAVES CRIPTOGRÁFICAS
        paint.color = Color.rgb(40, 40, 40)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        canvas.drawText("DESGLOSE DE SUCURSALES Y CLAVES ASIGNADAS", 30f, currentY, paint)
        currentY += 10f

        // Cabecera de la tabla
        paint.color = Color.rgb(30, 30, 30)
        canvas.drawRect(30f, currentY, 565f, currentY + 22f, paint)
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("#", 38f, currentY + 14f, paint)
        canvas.drawText("SUCURSAL", 58f, currentY + 14f, paint)
        canvas.drawText("HARDWARE ID", 185f, currentY + 14f, paint)
        canvas.drawText("CLAVE DE ACTIVACIÓN", 300f, currentY + 14f, paint)
        canvas.drawText("IMPORTE", 495f, currentY + 14f, paint)

        currentY += 22f

        // Filas de sucursales
        owner.branches.forEachIndexed { idx, branch ->
            val rowBg = if (idx % 2 == 0) Color.rgb(252, 252, 252) else Color.rgb(245, 245, 245)
            paint.color = rowBg
            canvas.drawRect(30f, currentY, 565f, currentY + 26f, paint)

            paint.color = Color.rgb(230, 230, 230)
            paint.style = Paint.Style.STROKE
            canvas.drawLine(30f, currentY + 26f, 565f, currentY + 26f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            canvas.drawText("${idx + 1}", 38f, currentY + 16f, paint)
            canvas.drawText(branch.name.take(20), 58f, currentY + 16f, paint)

            paint.color = Color.rgb(80, 80, 80)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            paint.textSize = 8.5f
            canvas.drawText(branch.hardwareId, 185f, currentY + 16f, paint)

            paint.color = Color.rgb(0, 105, 92) // Verde teal
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            paint.textSize = 8.5f
            canvas.drawText(branch.generatedKey.ifBlank { "PENDIENTE" }, 300f, currentY + 16f, paint)

            paint.color = Color.rgb(20, 20, 20)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            canvas.drawText("$${String.format(Locale.US, "%,.2f", owner.pricePerBranch)}", 495f, currentY + 16f, paint)

            currentY += 26f
        }

        currentY += 15f

        // 7. TOTALES FINANCIEROS
        val totalBox = RectF(330f, currentY, 565f, currentY + 65f)
        paint.color = Color.rgb(245, 245, 245)
        canvas.drawRoundRect(totalBox, 6f, 6f, paint)
        paint.color = Color.rgb(220, 220, 220)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(totalBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(80, 80, 80)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        canvas.drawText("Subtotal (${owner.branches.size} sucursales):", 345f, currentY + 22f, paint)
        paint.color = Color.rgb(20, 20, 20)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$${String.format(Locale.US, "%,.2f", owner.totalAmount)} MXN", 550f, currentY + 22f, paint)

        paint.color = Color.rgb(20, 20, 20)
        paint.textSize = 13f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL A PAGAR:", 345f, currentY + 48f, paint)
        paint.color = if (isPaid) Color.rgb(46, 125, 50) else Color.rgb(198, 40, 40)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$${String.format(Locale.US, "%,.2f", owner.totalAmount)} MXN", 550f, currentY + 48f, paint)
        paint.textAlign = Paint.Align.LEFT

        currentY += 85f

        // 8. TÉRMINOS Y SELLO CRIPTOGRÁFICO DE AUTENTICIDAD
        paint.color = Color.rgb(120, 120, 120)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 8f
        canvas.drawText("SELLO DE SEGURIDAD DIGITAL SGNIA:", 30f, currentY, paint)
        currentY += 12f

        val hashSecurity = "SHA256:${owner.id.replace("-", "").uppercase(Locale.US)}-${owner.branches.size}SUC-${owner.membershipTier.code}"
        paint.color = Color.rgb(90, 90, 90)
        canvas.drawText(hashSecurity, 30f, currentY, paint)
        currentY += 14f

        canvas.drawText("Este comprobante certifica la entrega formal de licencias offline para básculas industriales.", 30f, currentY, paint)
        currentY += 11f
        canvas.drawText("Cada clave está vinculada criptográficamente al procesador exclusivo de cada tablet.", 30f, currentY, paint)

        // PIE DE PÁGINA
        paint.color = Color.rgb(20, 19, 18)
        canvas.drawRect(0f, 810f, 595f, 842f, paint)
        paint.color = Color.rgb(238, 221, 200)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Gamaliel Velazquez Galicia • Liderazgo Técnico SGNIA COMPANY • sgnia.xyz", 297f, 828f, paint)

        document.finishPage(page)

        // Guardar archivo en cache
        val receiptsDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val cleanName = owner.ownerName.trim().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
        val pdfFile = File(receiptsDir, "SGNIA_Comprobante_${cleanName}_${owner.id.take(4)}.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    fun shareReceiptPdf(context: Context, pdfFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Comprobante Oficial de Licenciamiento SGNIA")
            putExtra(Intent.EXTRA_TEXT, "Adjunto encontrarás tu comprobante oficial de compra y activación de licencias SGNIA.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir Comprobante PDF"))
    }
}
