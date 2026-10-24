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

    // =========================================================================
    // 1. CONTRATO FORMAL DE LICENCIAMIENTO DE SOFTWARE (2 PÁGINAS A4)
    // =========================================================================
    fun generateContractPdf(context: Context, owner: OwnerRecord): File {
        val document = PdfDocument()
        val logoBitmap = loadLogoBitmap(context)
        val folio = "CTR-${owner.id.take(6).uppercase(Locale.US)}"
        val fechaStr = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "MX")).format(Date(owner.createdAt))

        // -------------------------------------------------------------
        // PÁGINA 1: DECLARACIONES Y CLÁUSULAS 1 A 4 (INCLUYE CANCELACIÓN)
        // -------------------------------------------------------------
        val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page1 = document.startPage(pageInfo1)
        var canvas = page1.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(Color.WHITE)
        drawCorporateHeader(canvas, paint, logoBitmap, "CONTRATO DE LICENCIAMIENTO", folio, fechaStr)

        var y = 115f
        paint.color = Color.rgb(20, 20, 20)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13f
        canvas.drawText("CONTRATO PRIVADO DE LICENCIAMIENTO DE SOFTWARE Y SERVICIOS", 30f, y, paint)
        y += 14f

        paint.color = Color.rgb(100, 100, 100)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 8.5f
        canvas.drawText("Sistema Especializado de Pesaje Industrial en Red TCP/IP, Control de Mermas y POS", 30f, y, paint)
        y += 18f

        // CAJA 1: COMPARECIENTES Y DECLARACIONES
        paint.color = Color.rgb(248, 248, 248)
        val partBox = RectF(30f, y, 565f, y + 80f)
        canvas.drawRoundRect(partBox, 6f, 6f, paint)
        paint.color = Color.rgb(220, 220, 220)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(partBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(40, 40, 40)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("COMPARECENCIA DE LAS PARTES:", 40f, y + 16f, paint)

        paint.color = Color.rgb(30, 30, 30)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("• EL PROVEEDOR: SGNIA COMPANY / Gamaliel Velázquez Galicia (Nopalucan de la Granja, Puebla, México).", 40f, y + 32f, paint)
        canvas.drawText("• EL CLIENTE / PROPIETARIO: ${owner.ownerName}, en representación de '${owner.companyName.ifBlank { "Carnicería" }}'.", 40f, y + 46f, paint)
        canvas.drawText("• CONTACTO CLIENTE: Teléfono / WhatsApp: ${owner.phoneWhatsApp.ifBlank { "N/A" }}  |  Correo: ${owner.email.ifBlank { "N/A" }}", 40f, y + 60f, paint)
        canvas.drawText("Ambas partes se reconocen mutuamente plena capacidad legal para suscribir el presente acuerdo.", 40f, y + 72f, paint)
        y += 94f

        // CLÁUSULAS 1 A 4
        y = drawClauseTitle(canvas, paint, "CLÁUSULA PRIMERA. OBJETO DEL CONTRATO", y)
        y = drawParagraph(
            canvas, paint,
            "El PROVEEDOR concede al CLIENTE una licencia de uso no exclusiva, intransferible y revocable del sistema " +
            "'SGNIA Carnicería POS' (destinado a terminales de mostrador con integración de básculas industriales TCP/IP) y " +
            "'SGNIA Consola Dueño' (panel administrativo de unificación y supervisión), para el número de sucursales pactadas.",
            y
        )

        y = drawClauseTitle(canvas, paint, "CLÁUSULA SEGUNDA. MODALIDAD Y VIGENCIA DE LA LICENCIA", y)
        y = drawParagraph(
            canvas, paint,
            "El software se contrata bajo la modalidad: ${owner.membershipTier.label} (${owner.membershipTier.badge}). " +
            "En modalidades de renta temporal (mensual o anual), la vigencia estará condicionada al pago puntual del periodo. " +
            "En la modalidad de Venta Definitiva (Licencia Perpetua LIFE), el derecho de uso en los equipos registrados es indefinido.",
            y
        )

        y = drawClauseTitle(canvas, paint, "CLÁUSULA TERCERA. CONTRAPRESTACIÓN ECONÓMICA Y CONDICIONES DE PAGO", y)
        y = drawParagraph(
            canvas, paint,
            "El CLIENTE acuerda cubrir la cantidad total de $${String.format(Locale.US, "%,.2f", owner.totalAmount)} MXN " +
            "($${String.format(Locale.US, "%,.2f", owner.pricePerBranch)} MXN por cada sucursal activa contratada) " +
            "mediante ${owner.paymentMethod}. Estatus actual del contrato: ${if (owner.paymentStatus == "PAGADO") "PAGADO (LIQUIDADO)" else "PENDIENTE DE PAGO"}.",
            y
        )

        y = drawClauseTitle(canvas, paint, "CLÁUSULA CUARTA. CANCELACIÓN, RESCISIÓN Y SUSPENSIÓN DEL SERVICIO", y)
        y = drawParagraph(
            canvas, paint,
            "a) Cancelación Voluntaria: El CLIENTE podrá dar por concluido el contrato en cualquier momento notificando al PROVEEDOR. " +
            "En modalidades de renta (mensual o anual), los periodos devengados no son reembolsables y el servicio concluirá al término del ciclo pagado.\n" +
            "b) Rescisión por Falta de Pago: En caso de adeudo superior a 3 días naturales tras la fecha de corte, el PROVEEDOR queda plenamente " +
            "facultado para pausar o revocar las licencias de las terminales mediante claves criptográficas de bloqueo inmediato (Kill-Switch).\n" +
            "c) Efectos de la Cancelación: Al darse de baja, cesa la habilitación de uso del software. La base de datos local de productos y clientes " +
            "permanece íntegramente en poder del CLIENTE dentro de sus dispositivos físicos.",
            y
        )

        drawCorporateFooter(canvas, paint, "Página 1 de 2  •  Contrato Formal SGNIA COMPANY")
        document.finishPage(page1)

        // -------------------------------------------------------------
        // PÁGINA 2: SUCURSALES AUTORIZADAS, CONFIDENCIALIDAD Y FIRMAS
        // -------------------------------------------------------------
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page2 = document.startPage(pageInfo2)
        canvas = page2.canvas

        canvas.drawColor(Color.WHITE)
        drawCorporateHeader(canvas, paint, logoBitmap, "ANEXO DE TERMINALES Y FIRMAS", folio, fechaStr)

        y = 115f
        y = drawClauseTitle(canvas, paint, "CLÁUSULA QUINTA. TERMINALES Y SUCURSALES AUTORIZADAS", y)
        paint.color = Color.rgb(90, 90, 90)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText("Las licencias otorgadas quedan asociadas exclusivamente a los siguientes identificadores de hardware (Hardware IDs):", 30f, y, paint)
        y += 12f

        // Tabla de sucursales
        paint.color = Color.rgb(20, 19, 18)
        canvas.drawRect(30f, y, 565f, y + 20f, paint)
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 8f
        canvas.drawText("#", 38f, y + 13f, paint)
        canvas.drawText("TIPO / SUCURSAL", 60f, y + 13f, paint)
        canvas.drawText("HARDWARE ID VINCULADO", 200f, y + 13f, paint)
        canvas.drawText("ESTADO DE LICENCIA", 390f, y + 13f, paint)
        y += 20f

        // Fila 0: Consola Dueño (si existe)
        var rowIdx = 1
        if (owner.ownerHardwareId.isNotBlank()) {
            paint.color = Color.rgb(245, 245, 245)
            canvas.drawRect(30f, y, 565f, y + 22f, paint)
            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            canvas.drawText("👑", 38f, y + 15f, paint)
            canvas.drawText("Consola Dueño (Admin)", 60f, y + 15f, paint)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText(owner.ownerHardwareId, 200f, y + 15f, paint)
            paint.color = Color.rgb(0, 105, 92)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(owner.ownerStatus, 390f, y + 15f, paint)
            y += 22f
        }

        owner.branches.forEachIndexed { i, branch ->
            val bg = if (i % 2 == 0) Color.rgb(252, 252, 252) else Color.rgb(245, 245, 245)
            paint.color = bg
            canvas.drawRect(30f, y, 565f, y + 22f, paint)
            paint.color = Color.rgb(220, 220, 220)
            paint.style = Paint.Style.STROKE
            canvas.drawLine(30f, y + 22f, 565f, y + 22f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            canvas.drawText("$rowIdx", 38f, y + 15f, paint)
            canvas.drawText(branch.name.take(24), 60f, y + 15f, paint)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText(branch.hardwareId.ifBlank { "Pendiente" }, 200f, y + 15f, paint)
            paint.color = Color.rgb(0, 105, 92)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(branch.status, 390f, y + 15f, paint)
            y += 22f
            rowIdx++
        }
        y += 14f

        y = drawClauseTitle(canvas, paint, "CLÁUSULA SEXTA. PROPIEDAD DE LOS DATOS Y CONFIDENCIALIDAD", y)
        y = drawParagraph(
            canvas, paint,
            "El CLIENTE es el único propietario de los catálogos, registros de pesaje, existencias y montos de venta generados. " +
            "El sistema opera bajo una arquitectura 100% Offline-First en la red local de la carnicería, garantizando que ninguna información " +
            "comercial sea transmitida, vendida ni compartida con terceros.",
            y
        )

        y = drawClauseTitle(canvas, paint, "CLÁUSULA SÉPTIMA. SOPORTE TÉCNICO Y MANTENIMIENTO", y)
        y = drawParagraph(
            canvas, paint,
            "El PROVEEDOR brindará asistencia técnica remota por WhatsApp (+52 276 109 5688) para la configuración de básculas industriales, " +
            "resolución de dudas operativas y mantenimiento de la plataforma conforme a la póliza del servicio.",
            y
        )
        y += 10f

        // SECCIÓN DE FIRMAS DE CONFORMIDAD
        paint.color = Color.rgb(40, 40, 40)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("CONFORMIDAD Y FIRMA DE LAS PARTES:", 30f, y, paint)
        y += 12f

        paint.color = Color.rgb(90, 90, 90)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8f
        canvas.drawText("Enteradas las partes del contenido y fuerza legal del presente instrumento, lo firman en Nopalucan de la Granja, Puebla:", 30f, y, paint)
        y += 45f

        // Caja de Firma Proveedor (Izquierda)
        paint.color = Color.rgb(80, 80, 80)
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(50f, y, 260f, y, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(20, 20, 20)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("POR EL PROVEEDOR", 155f, y + 14f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText("Gamaliel Velázquez Galicia", 155f, y + 26f, paint)
        canvas.drawText("Liderazgo Técnico · SGNIA COMPANY", 155f, y + 37f, paint)

        // Caja de Firma Cliente (Derecha)
        paint.color = Color.rgb(80, 80, 80)
        paint.style = Paint.Style.STROKE
        canvas.drawLine(335f, y, 545f, y, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(20, 20, 20)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("POR EL CLIENTE / TITULAR", 440f, y + 14f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText(owner.ownerName, 440f, y + 26f, paint)
        canvas.drawText(owner.companyName.ifBlank { "Propietario Carnicería" }, 440f, y + 37f, paint)
        paint.textAlign = Paint.Align.LEFT

        drawCorporateFooter(canvas, paint, "Página 2 de 2  •  Contrato Formal SGNIA COMPANY")
        document.finishPage(page2)

        // Guardar archivo
        val contractsDir = File(context.cacheDir, "contracts").apply { mkdirs() }
        val cleanName = owner.ownerName.trim().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
        val pdfFile = File(contractsDir, "SGNIA_Contrato_${cleanName}_${owner.id.take(4)}.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return pdfFile
    }

    // =========================================================================
    // 2. RECIBO OFICIAL DE COBRO Y PAGO (1 PÁGINA A4)
    // =========================================================================
    fun generateReceiptPdf(context: Context, owner: OwnerRecord): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val logoBitmap = loadLogoBitmap(context)
        val folio = "REC-${owner.id.take(6).uppercase(Locale.US)}"
        val fechaEmision = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        canvas.drawColor(Color.WHITE)
        drawCorporateHeader(canvas, paint, logoBitmap, "RECIBO DE COBRO Y PAGO", folio, fechaEmision)

        var currentY = 120f
        paint.color = Color.rgb(30, 30, 30)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("RECIBO OFICIAL DE COBRO Y COMPROBANTE DE PAGO", 30f, currentY, paint)

        // SELLO DE PAGO
        val isPaid = owner.paymentStatus == "PAGADO"
        val badgeColor = if (isPaid) Color.rgb(46, 125, 50) else Color.rgb(211, 47, 47)
        val badgeBg = if (isPaid) Color.rgb(232, 245, 233) else Color.rgb(255, 235, 238)
        val badgeText = if (isPaid) "● PAGADO" else "● PENDIENTE"

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
        currentY += 24f

        // CAJA DATOS DEL CLIENTE
        val hasOwnerHw = owner.ownerHardwareId.isNotBlank()
        val clientBoxHeight = if (hasOwnerHw) 95f else 75f
        paint.color = Color.rgb(248, 248, 248)
        val clientBox = RectF(30f, currentY, 565f, currentY + clientBoxHeight)
        canvas.drawRoundRect(clientBox, 8f, 8f, paint)
        paint.color = Color.rgb(220, 220, 220)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(clientBox, 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(80, 80, 80)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("DATOS DEL TITULAR Y CARNICERÍA:", 45f, currentY + 18f, paint)

        paint.color = Color.rgb(20, 20, 20)
        paint.textSize = 10.5f
        canvas.drawText("Dueño: ${owner.ownerName}", 45f, currentY + 36f, paint)
        canvas.drawText("Empresa: ${owner.companyName.ifBlank { "Carnicería" }}", 45f, currentY + 52f, paint)
        canvas.drawText("WhatsApp: ${owner.phoneWhatsApp.ifBlank { "No registrado" }}", 320f, currentY + 36f, paint)
        canvas.drawText("Correo: ${owner.email.ifBlank { "No registrado" }}", 320f, currentY + 52f, paint)

        if (hasOwnerHw) {
            paint.color = Color.rgb(0, 105, 92)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            paint.textSize = 9f
            canvas.drawText("👑 Consola Dueño HW: ${owner.ownerHardwareId}", 45f, currentY + 70f, paint)
            canvas.drawText("Clave Admin: ${owner.ownerLicenseKey.ifBlank { "PENDIENTE" }}", 320f, currentY + 70f, paint)
        }
        currentY += (clientBoxHeight + 16f)

        // CAJA RESUMEN
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
        val statusSuffix = if (owner.contractStatus != "ACTIVO") " • [${owner.contractStatus}]" else ""
        canvas.drawText("Modalidad: ${owner.membershipTier.badge}$statusSuffix", 45f, currentY + 33f, paint)

        paint.color = Color.rgb(20, 20, 20)
        paint.textSize = 10f
        canvas.drawText("Total Sucursales: ${owner.branches.size} tiendas", 320f, currentY + 18f, paint)
        paint.color = Color.rgb(100, 100, 100)
        paint.textSize = 9f
        canvas.drawText("Método de Pago: ${owner.paymentMethod}", 320f, currentY + 33f, paint)
        currentY += 60f

        // TABLA SUCURSALES
        paint.color = Color.rgb(40, 40, 40)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10.5f
        canvas.drawText("DESGLOSE DE SUCURSALES Y CLAVES CRIPTOGRÁFICAS", 30f, currentY, paint)
        currentY += 10f

        paint.color = Color.rgb(30, 30, 30)
        canvas.drawRect(30f, currentY, 565f, currentY + 20f, paint)
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 8.5f
        canvas.drawText("#", 38f, currentY + 13f, paint)
        canvas.drawText("SUCURSAL", 58f, currentY + 13f, paint)
        canvas.drawText("HARDWARE ID", 185f, currentY + 13f, paint)
        canvas.drawText("CLAVE DE ACTIVACIÓN", 300f, currentY + 13f, paint)
        canvas.drawText("IMPORTE", 495f, currentY + 13f, paint)
        currentY += 20f

        owner.branches.forEachIndexed { idx, branch ->
            val rowBg = if (idx % 2 == 0) Color.rgb(252, 252, 252) else Color.rgb(245, 245, 245)
            paint.color = rowBg
            canvas.drawRect(30f, currentY, 565f, currentY + 24f, paint)
            paint.color = Color.rgb(230, 230, 230)
            paint.style = Paint.Style.STROKE
            canvas.drawLine(30f, currentY + 24f, 565f, currentY + 24f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 9f
            canvas.drawText("${idx + 1}", 38f, currentY + 15f, paint)
            canvas.drawText(branch.name.take(20), 58f, currentY + 15f, paint)
            paint.color = Color.rgb(80, 80, 80)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            paint.textSize = 8.5f
            canvas.drawText(branch.hardwareId, 185f, currentY + 15f, paint)
            paint.color = Color.rgb(0, 105, 92)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            canvas.drawText(branch.generatedKey.ifBlank { "PENDIENTE" }, 300f, currentY + 15f, paint)
            paint.color = Color.rgb(20, 20, 20)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("$${String.format(Locale.US, "%,.2f", owner.pricePerBranch)}", 495f, currentY + 15f, paint)
            currentY += 24f
        }
        currentY += 15f

        // TOTALES
        val totalBox = RectF(330f, currentY, 565f, currentY + 58f)
        paint.color = Color.rgb(245, 245, 245)
        canvas.drawRoundRect(totalBox, 6f, 6f, paint)
        paint.color = Color.rgb(220, 220, 220)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(totalBox, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(80, 80, 80)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        canvas.drawText("Subtotal (${owner.branches.size} sucursales):", 345f, currentY + 20f, paint)
        paint.color = Color.rgb(20, 20, 20)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$${String.format(Locale.US, "%,.2f", owner.totalAmount)} MXN", 550f, currentY + 20f, paint)

        paint.color = Color.rgb(20, 20, 20)
        paint.textSize = 12f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL COBRADO:", 345f, currentY + 44f, paint)
        paint.color = if (isPaid) Color.rgb(46, 125, 50) else Color.rgb(198, 40, 40)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$${String.format(Locale.US, "%,.2f", owner.totalAmount)} MXN", 550f, currentY + 44f, paint)
        paint.textAlign = Paint.Align.LEFT
        currentY += 75f

        // SELLO Y FIRMA DE RECIBIDO
        paint.color = Color.rgb(80, 80, 80)
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(50f, currentY + 40f, 250f, currentY + 40f, paint)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(20, 20, 20)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("RECIBÍ DE CONFORMIDAD", 150f, currentY + 54f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText("Gamaliel Velázquez Galicia · SGNIA COMPANY", 150f, currentY + 66f, paint)
        paint.textAlign = Paint.Align.LEFT

        val hashSecurity = "SHA256:${owner.id.replace("-", "").uppercase(Locale.US)}-${owner.branches.size}SUC-${owner.membershipTier.code}"
        paint.color = Color.rgb(120, 120, 120)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 8f
        canvas.drawText("Sello Digital de Cobro: $hashSecurity", 280f, currentY + 45f, paint)
        canvas.drawText("Validez formal para comprobación de servicios y rentas.", 280f, currentY + 58f, paint)

        drawCorporateFooter(canvas, paint, "Recibo Oficial SGNIA COMPANY  •  Software Industrial")
        document.finishPage(page)

        val receiptsDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val cleanName = owner.ownerName.trim().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
        val pdfFile = File(receiptsDir, "SGNIA_Recibo_${cleanName}_${owner.id.take(4)}.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return pdfFile
    }

    // =========================================================================
    // 3. CONSTANCIA OFICIAL DE CANCELACIÓN Y RESCISIÓN (1 PÁGINA A4)
    // =========================================================================
    fun generateCancellationPdf(context: Context, owner: OwnerRecord): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val logoBitmap = loadLogoBitmap(context)
        val folio = "BAJA-${owner.id.take(6).uppercase(Locale.US)}"
        val fechaEmision = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "MX")).format(Date())

        canvas.drawColor(Color.WHITE)
        drawCorporateHeader(canvas, paint, logoBitmap, "CONSTANCIA DE RESCISIÓN / BAJA", folio, fechaEmision)

        var y = 120f
        paint.color = Color.rgb(180, 40, 40)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 13.5f
        canvas.drawText("CONSTANCIA OFICIAL DE CANCELACIÓN Y RESCISIÓN DE SERVICIO", 30f, y, paint)
        y += 15f

        paint.color = Color.rgb(100, 100, 100)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 8.5f
        canvas.drawText("Acta formal de baja definitiva y desactivación de licencias industriales", 30f, y, paint)
        y += 20f

        // CAJA DATOS DE LA BAJA
        paint.color = Color.rgb(255, 245, 245)
        val box = RectF(30f, y, 565f, y + 80f)
        canvas.drawRoundRect(box, 6f, 6f, paint)
        paint.color = Color.rgb(230, 180, 180)
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(box, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(160, 30, 30)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText("DATOS DEL CONTRATO DADO DE BAJA:", 40f, y + 18f, paint)

        paint.color = Color.rgb(30, 30, 30)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9f
        canvas.drawText("• Cliente / Titular: ${owner.ownerName}  |  Empresa: ${owner.companyName.ifBlank { "Carnicería" }}", 40f, y + 34f, paint)
        canvas.drawText("• Modalidad que se cancela: ${owner.membershipTier.label} (${owner.membershipTier.badge})", 40f, y + 48f, paint)
        canvas.drawText("• Fecha efectiva de baja: $fechaEmision  |  Estatus: CANCELADO / REVOCADO", 40f, y + 62f, paint)
        canvas.drawText("• Sucursales que causan baja operativa: ${owner.branches.size} tiendas registradas", 40f, y + 74f, paint)
        y += 95f

        y = drawClauseTitle(canvas, paint, "DECLARACIÓN FORMAL DE RESCISIÓN", y)
        y = drawParagraph(
            canvas, paint,
            "Por medio del presente instrumento se formaliza la terminación y cancelación definitiva del contrato de prestación de " +
            "servicios y licenciamiento del software SGNIA. Ambas partes acuerdan la desactivación de los accesos operativos y " +
            "la emisión de las correspondientes claves criptográficas de revocación e inhabilitación (Kill-Switch).",
            y
        )

        y = drawClauseTitle(canvas, paint, "TERMINALES Y DISPOSITIVOS INHABILITADOS", y)

        // Tabla de bajas
        paint.color = Color.rgb(20, 19, 18)
        canvas.drawRect(30f, y, 565f, y + 18f, paint)
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 8f
        canvas.drawText("SUCURSAL / TERMINAL", 40f, y + 12f, paint)
        canvas.drawText("HARDWARE ID", 220f, y + 12f, paint)
        canvas.drawText("ESTADO FINAL", 420f, y + 12f, paint)
        y += 18f

        if (owner.ownerHardwareId.isNotBlank()) {
            paint.color = Color.rgb(250, 240, 240)
            canvas.drawRect(30f, y, 565f, y + 20f, paint)
            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 8.5f
            canvas.drawText("Consola Dueño (Admin)", 40f, y + 14f, paint)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText(owner.ownerHardwareId, 220f, y + 14f, paint)
            paint.color = Color.rgb(180, 40, 40)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("🚫 ACCESO REVOCADO", 420f, y + 14f, paint)
            y += 20f
        }

        owner.branches.forEach { b ->
            paint.color = Color.rgb(250, 240, 240)
            canvas.drawRect(30f, y, 565f, y + 20f, paint)
            paint.color = Color.rgb(30, 30, 30)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            canvas.drawText(b.name.take(24), 40f, y + 14f, paint)
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText(b.hardwareId, 220f, y + 14f, paint)
            paint.color = Color.rgb(180, 40, 40)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("🚫 ACCESO REVOCADO", 420f, y + 14f, paint)
            y += 20f
        }
        y += 15f

        y = drawClauseTitle(canvas, paint, "FINIQUITO, NO ADEUDO Y SALVAGUARDA DE INFORMACIÓN", y)
        y = drawParagraph(
            canvas, paint,
            "1. Ambas partes manifiestan de común acuerdo que no existen pagos pendientes ni adeudos recíprocos derivados del contrato.\n" +
            "2. Se certifica que la base de datos comercial e histórica de la carnicería queda bajo resguardo exclusivo del CLIENTE " +
            "en sus dispositivos locales, sin que el PROVEEDOR conserve copias de dicha información.\n" +
            "3. Las partes se otorgan el más amplio finiquito que en derecho proceda, concluyendo todo vínculo contractual.",
            y
        )
        y += 25f

        // Firmas
        paint.color = Color.rgb(80, 80, 80)
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(50f, y, 260f, y, paint)
        canvas.drawLine(335f, y, 545f, y, paint)
        paint.style = Paint.Style.FILL

        paint.color = Color.rgb(20, 20, 20)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("POR EL PROVEEDOR", 155f, y + 14f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText("Gamaliel Velázquez Galicia · SGNIA COMPANY", 155f, y + 26f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9f
        canvas.drawText("POR EL CLIENTE QUE CAUSA BAJA", 440f, y + 14f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText(owner.ownerName, 440f, y + 26f, paint)
        paint.textAlign = Paint.Align.LEFT

        drawCorporateFooter(canvas, paint, "Constancia Oficial de Baja  •  SGNIA COMPANY © 2026")
        document.finishPage(page)

        val cancellationsDir = File(context.cacheDir, "cancellations").apply { mkdirs() }
        val cleanName = owner.ownerName.trim().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
        val pdfFile = File(cancellationsDir, "SGNIA_Constancia_Baja_${cleanName}_${owner.id.take(4)}.pdf")

        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return pdfFile
    }

    // =========================================================================
    // UTILIDADES GRÁFICAS Y COMPARTIR POR WHATSAPP / SISTEMA
    // =========================================================================
    private fun loadLogoBitmap(context: Context): Bitmap? {
        return try {
            BitmapFactory.decodeResource(context.resources, R.drawable.sglogo)?.let {
                Bitmap.createScaledBitmap(it, 60, 60, true)
            }
        } catch (_: Exception) { null }
    }

    private fun drawCorporateHeader(
        canvas: Canvas,
        paint: Paint,
        logoBitmap: Bitmap?,
        docSubHeader: String,
        folio: String,
        fechaStr: String
    ) {
        paint.color = Color.rgb(20, 19, 18)
        canvas.drawRect(0f, 0f, 595f, 85f, paint)

        // Línea verde esmeralda
        paint.color = Color.rgb(74, 222, 128)
        canvas.drawRect(0f, 85f, 595f, 89f, paint)

        if (logoBitmap != null) {
            canvas.drawBitmap(logoBitmap, 30f, 12f, paint)
        }

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 16f
        canvas.drawText("SGNIA COMPANY", 100f, 34f, paint)

        paint.color = Color.rgb(238, 221, 200)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        paint.textSize = 8.5f
        canvas.drawText("SOFTWARE QUE PROTEGE Y HACE CRECER TU NEGOCIO  •  $docSubHeader", 100f, 48f, paint)

        paint.color = Color.rgb(180, 180, 180)
        paint.textSize = 8f
        canvas.drawText("https://sgnia.xyz  •  WhatsApp: +52 276 109 5688  •  Nopalucan de la Granja, Puebla", 100f, 62f, paint)

        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 9.5f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(folio, 565f, 34f, paint)

        paint.color = Color.rgb(238, 221, 200)
        paint.textSize = 8f
        canvas.drawText(fechaStr, 565f, 48f, paint)
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawCorporateFooter(canvas: Canvas, paint: Paint, footerText: String) {
        paint.color = Color.rgb(20, 19, 18)
        canvas.drawRect(0f, 814f, 595f, 842f, paint)

        paint.color = Color.rgb(238, 221, 200)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "$footerText  •  Gamaliel Velázquez Galicia · Liderazgo Técnico  •  sgnia.xyz",
            297f, 831f, paint
        )
        paint.textAlign = Paint.Align.LEFT
    }

    private fun drawClauseTitle(canvas: Canvas, paint: Paint, title: String, currentY: Float): Float {
        paint.color = Color.rgb(20, 20, 20)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 9.5f
        canvas.drawText(title, 30f, currentY, paint)
        return currentY + 12f
    }

    private fun drawParagraph(canvas: Canvas, paint: Paint, text: String, currentY: Float): Float {
        paint.color = Color.rgb(60, 60, 60)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 8.5f

        val maxWidth = 535f
        val lines = text.split("\n")
        var y = currentY

        for (rawLine in lines) {
            val words = rawLine.split(" ")
            var currentLine = ""
            for (word in words) {
                val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
                val width = paint.measureText(candidate)
                if (width <= maxWidth) {
                    currentLine = candidate
                } else {
                    canvas.drawText(currentLine, 30f, y, paint)
                    y += 11f
                    currentLine = word
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, 30f, y, paint)
                y += 11f
            }
        }
        return y + 6f
    }

    fun sharePdf(context: Context, pdfFile: File, title: String, subject: String, body: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareReceiptPdf(context: Context, pdfFile: File) {
        sharePdf(
            context = context,
            pdfFile = pdfFile,
            title = "Compartir Comprobante de Pago Oficial",
            subject = "Comprobante de Pago Oficial SGNIA - ${pdfFile.nameWithoutExtension}",
            body = "Estimado cliente, adjuntamos su comprobante fiscal/comercial de pago emitido por SGNIA COMPANY."
        )
    }

    fun shareContractPdf(context: Context, pdfFile: File) {
        sharePdf(
            context = context,
            pdfFile = pdfFile,
            title = "Compartir Contrato Formal de Licenciamiento",
            subject = "Contrato de Licenciamiento SGNIA COMPANY - ${pdfFile.nameWithoutExtension}",
            body = "Estimado cliente, adjuntamos el Contrato Privado de Licenciamiento de Software y Servicios celebrado con SGNIA COMPANY."
        )
    }

    fun shareCancellationPdf(context: Context, pdfFile: File) {
        sharePdf(
            context = context,
            pdfFile = pdfFile,
            title = "Compartir Constancia Oficial de Cancelación",
            subject = "Constancia de Cancelación y Rescisión SGNIA - ${pdfFile.nameWithoutExtension}",
            body = "Constancia formal de rescisión de servicios, cancelación de contrato y revocación de terminales emitida por SGNIA COMPANY."
        )
    }
}
