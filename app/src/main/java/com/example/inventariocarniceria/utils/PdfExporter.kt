package com.example.inventariocarniceria.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.inventariocarniceria.data.local.InventoryCaptureEntity
import com.example.inventariocarniceria.data.local.ProductEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    // Paleta de Colores Corporativa Ejecutiva
    private val COLOR_PRIMARY = Color.rgb(155, 28, 35)      // #9B1C23 Borgoña Institucional SGNIA
    private val COLOR_HEADER_DARK = Color.rgb(30, 41, 59)   // #1E293B Pizarra Oscuro para Encabezados
    private val COLOR_CHARCOAL = Color.rgb(33, 33, 33)      // #212121 Texto Principal
    private val COLOR_SLATE = Color.rgb(71, 85, 105)        // #475569 Texto Secundario
    private val COLOR_EMERALD = Color.rgb(22, 101, 52)      // #166534 Verde Aprobación / Surtido
    private val COLOR_BG_LIGHT = Color.rgb(248, 250, 252)   // #F8FAFC Fondo alternado filas
    private val COLOR_BORDER = Color.rgb(226, 232, 240)     // #E2E8F0 Separadores elegantes

    // =========================================================================
    // 1. ORDEN OFICIAL DE PEDIDO Y REABASTECIMIENTO DE SUCURSAL
    // =========================================================================
    fun generateAndShareVipInventoryPdf(
        context: Context,
        orders: List<InventoryCaptureEntity>,
        allProducts: List<ProductEntity> = emptyList(),
        branchName: String = "Sucursal Matriz Centro",
        managerName: String = "Encargado de Turno"
    ): File? {
        val productMap = allProducts.associateBy { it.code }
        val pdfDocument = PdfDocument()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateShort = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val folio = "ORD-$dateShort"
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        var pageNum = 1
        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
        var canvas = page.canvas

        // --- BANNER SUPERIOR CORPORATIVO (PÁGINA 1) ---
        paint.color = COLOR_PRIMARY
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Línea fina inferior de acento
        paint.color = COLOR_HEADER_DARK
        canvas.drawRect(0f, 75f, 595f, 78f, paint)

        // Textos institucionales
        paint.color = Color.WHITE
        paint.textSize = 19f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CARNICERÍA SGNIA", 30f, 36f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawText("ORDEN DE PEDIDO Y REABASTECIMIENTO DE SUCURSAL", 30f, 52f, paint)
        canvas.drawText("DEPARTAMENTO DE CONTROL DE INVENTARIOS Y LOGÍSTICA MATRIZ", 30f, 66f, paint)

        // Recuadro de Folio Oficial
        paint.color = Color.WHITE
        canvas.drawRoundRect(RectF(410f, 18f, 565f, 58f), 6f, 6f, paint)
        paint.color = COLOR_PRIMARY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(RectF(410f, 18f, 565f, 58f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = COLOR_SLATE
        canvas.drawText("FOLIO OFICIAL", 422f, 32f, paint)
        paint.textSize = 11f
        paint.color = COLOR_PRIMARY
        canvas.drawText(folio, 422f, 48f, paint)

        var y = 96f

        // --- BLOQUE DE METADATOS OPERATIVOS ---
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 60f), 8f, 8f, paint)
        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 60f), 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        val metaY = y + 18f
        paint.color = COLOR_CHARCOAL
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SUCURSAL SOLICITANTE:", 42f, metaY, paint)
        canvas.drawText("SOLICITADO POR:", 42f, metaY + 15f, paint)
        canvas.drawText("BÁSCULA VINCULADA:", 42f, metaY + 30f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(branchName.ifEmpty { "Sucursal Principal" }, 165f, metaY, paint)
        canvas.drawText(managerName.ifEmpty { "Encargado de Turno" }, 165f, metaY + 15f, paint)
        canvas.drawText("Systel Cuora Max (TCP/IP Wi-Fi • 3 decimales)", 165f, metaY + 30f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FECHA DE EMISIÓN:", 360f, metaY, paint)
        canvas.drawText("TIPO DE DOCUMENTO:", 360f, metaY + 15f, paint)
        canvas.drawText("DESTINATARIO:", 360f, metaY + 30f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(dateFormat.format(Date()), 460f, metaY, paint)
        paint.color = COLOR_PRIMARY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("REABASTECIMIENTO", 460f, metaY + 15f, paint)
        paint.color = COLOR_CHARCOAL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Centro de Distribución / Matriz", 460f, metaY + 30f, paint)

        y += 74f

        // Helper para dibujar encabezado de tabla
        fun drawTableHeader(c: Canvas, topY: Float) {
            paint.color = COLOR_HEADER_DARK
            c.drawRoundRect(RectF(30f, topY, 565f, topY + 24f), 5f, 5f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val thY = topY + 16f
            c.drawText("PLU", 38f, thY, paint)
            c.drawText("CORTE / PRODUCTO", 78f, thY, paint)
            c.drawText("EXISTENCIA EN TIENDA", 240f, thY, paint)
            c.drawText("PRESENTACIÓN", 340f, thY, paint)
            c.drawText("CANTIDAD A SURTIR", 430f, thY, paint)
        }

        drawTableHeader(canvas, y)
        y += 28f

        var totalEstimatedVolumeKg = 0.0
        var totalBoxesCount = 0
        var totalBarsCount = 0
        var totalPackagesCount = 0
        var totalBagsCount = 0

        if (orders.isEmpty()) {
            paint.color = COLOR_CHARCOAL
            paint.textSize = 10f
            canvas.drawText("Inventario 100% completo. No hay faltantes registrados para surtido.", 45f, y + 20f, paint)
            y += 40f
        } else {
            orders.forEachIndexed { index, order ->
                if (y > 710f) {
                    paint.color = Color.GRAY
                    paint.textSize = 8f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Página $pageNum • Carnicería SGNIA • Folio: $folio", 180f, 825f, paint)

                    pdfDocument.finishPage(page)
                    pageNum++
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
                    canvas = page.canvas

                    paint.color = COLOR_PRIMARY
                    canvas.drawRect(0f, 0f, 595f, 38f, paint)
                    paint.color = COLOR_HEADER_DARK
                    canvas.drawRect(0f, 38f, 595f, 40f, paint)

                    paint.color = Color.WHITE
                    paint.textSize = 11.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("CARNICERÍA SGNIA — ORDEN DE REABASTECIMIENTO (PÁGINA $pageNum)", 30f, 25f, paint)

                    y = 54f
                    drawTableHeader(canvas, y)
                    y += 28f
                }

                if (index % 2 == 1) {
                    paint.color = COLOR_BG_LIGHT
                    canvas.drawRect(30f, y - 4f, 565f, y + 17f, paint)
                }

                val product = productMap[order.productCode]

                // PLU
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = COLOR_SLATE
                canvas.drawText("#${order.productCode}", 38f, y + 10f, paint)

                // Nombre del producto
                paint.color = COLOR_CHARCOAL
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val safeName = if (order.productName.length > 25) order.productName.take(24) + "…" else order.productName
                canvas.drawText(safeName, 78f, y + 10f, paint)

                // Existencia en tienda
                val stockText = when (product?.orderUnitType) {
                    "BARRA" -> "${product.currentStockUnits.toInt()} barras"
                    "PAQUETE" -> "${product.currentStockUnits.toInt()} pqtes"
                    "BOLSA" -> "${product.currentStockUnits.toInt()} bolsas"
                    "CAJA" -> "${product.currentStockUnits.toInt()} cajas"
                    "PIEZA" -> "${product.currentStockUnits.toInt()} pzas"
                    else -> String.format(Locale.US, "%.3f kg", order.capturedWeight)
                }
                canvas.drawText(stockText, 240f, y + 10f, paint)

                // Presentación del producto
                val presText = when (product?.orderUnitType) {
                    "BARRA" -> "Barra ~${product.weightPerBoxKg} kg"
                    "PAQUETE" -> "Paquete ~${product.weightPerBoxKg} kg"
                    "BOLSA" -> "Bolsa ~${product.weightPerBoxKg} kg"
                    "CAJA" -> "Caja ~${product.weightPerBoxKg} kg"
                    "PIEZA" -> "Por Pieza"
                    else -> "Granel (kg)"
                }
                canvas.drawText(presText, 340f, y + 10f, paint)

                // Cantidad a Surtir calculada según presentación
                val effectiveUnitType = if (order.unitType != "KG") order.unitType else (product?.orderUnitType ?: "KG")
                val boxWeight = product?.weightPerBoxKg ?: 0.0

                val toOrderText: String
                val itemEstimatedKg: Double

                when (effectiveUnitType) {
                    "CAJA" -> {
                        val boxes = if (order.orderedUnits > 0) order.orderedUnits.toInt() else {
                            if (boxWeight > 0) Math.max(1, Math.round(order.shortage / boxWeight).toInt()) else 1
                        }
                        itemEstimatedKg = if (boxWeight > 0) boxes * boxWeight else order.shortage
                        totalBoxesCount += boxes
                        toOrderText = "$boxes CAJA(S) (~${String.format(Locale.US, "%.2f", itemEstimatedKg)} kg)"
                    }
                    "BARRA" -> {
                        val bars = if (order.orderedUnits > 0) order.orderedUnits.toInt() else {
                            if (boxWeight > 0) Math.max(1, Math.round(order.shortage / boxWeight).toInt()) else 1
                        }
                        itemEstimatedKg = if (boxWeight > 0) bars * boxWeight else order.shortage
                        totalBarsCount += bars
                        toOrderText = "$bars BARRA(S) (~${String.format(Locale.US, "%.2f", itemEstimatedKg)} kg)"
                    }
                    "PAQUETE" -> {
                        val pqtes = if (order.orderedUnits > 0) order.orderedUnits.toInt() else {
                            if (boxWeight > 0) Math.max(1, Math.round(order.shortage / boxWeight).toInt()) else 1
                        }
                        itemEstimatedKg = if (boxWeight > 0) pqtes * boxWeight else order.shortage
                        totalPackagesCount += pqtes
                        toOrderText = "$pqtes PQTE(S) (~${String.format(Locale.US, "%.2f", itemEstimatedKg)} kg)"
                    }
                    "BOLSA" -> {
                        val bolsas = if (order.orderedUnits > 0) order.orderedUnits.toInt() else {
                            if (boxWeight > 0) Math.max(1, Math.round(order.shortage / boxWeight).toInt()) else 1
                        }
                        itemEstimatedKg = if (boxWeight > 0) bolsas * boxWeight else order.shortage
                        totalBagsCount += bolsas
                        toOrderText = "$bolsas BOLSA(S) (~${String.format(Locale.US, "%.2f", itemEstimatedKg)} kg)"
                    }
                    "BOTE" -> {
                        val botes = if (order.orderedUnits > 0) order.orderedUnits.toInt() else {
                            if (boxWeight > 0) Math.max(1, Math.round(order.shortage / boxWeight).toInt()) else 1
                        }
                        itemEstimatedKg = if (boxWeight > 0) botes * boxWeight else order.shortage
                        toOrderText = "$botes BOTE(S) (~${String.format(Locale.US, "%.2f", itemEstimatedKg)} kg)"
                    }
                    "PIEZA" -> {
                        val pieces = if (order.orderedUnits > 0) order.orderedUnits.toInt() else Math.max(1, order.shortage.toInt())
                        itemEstimatedKg = order.shortage
                        toOrderText = "$pieces PIEZA(S)"
                    }
                    else -> {
                        itemEstimatedKg = order.shortage
                        toOrderText = String.format(Locale.US, "%.3f kg", order.shortage)
                    }
                }

                totalEstimatedVolumeKg += itemEstimatedKg

                paint.color = COLOR_EMERALD
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(toOrderText, 430f, y + 10f, paint)

                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.5f
                canvas.drawLine(30f, y + 17f, 565f, y + 17f, paint)

                y += 21f
            }
        }

        // --- VERIFICAR ESPACIO PARA RESUMEN Y FIRMAS ---
        if (y > 630f) {
            paint.color = Color.GRAY
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Página $pageNum • Carnicería SGNIA • Folio: $folio", 180f, 825f, paint)

            pdfDocument.finishPage(page)
            pageNum++
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
            canvas = page.canvas

            paint.color = COLOR_PRIMARY
            canvas.drawRect(0f, 0f, 595f, 38f, paint)
            paint.color = COLOR_HEADER_DARK
            canvas.drawRect(0f, 38f, 595f, 40f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("CARNICERÍA SGNIA — RESUMEN Y AUTORIZACIÓN (PÁG. $pageNum)", 30f, 25f, paint)

            y = 56f
        } else {
            y += 15f
        }

        // --- TARJETAS KPI DE RESUMEN OPERATIVO ---
        val cardWidth = 168f
        val cardHeight = 46f

        // KPI 1: Volumen Estimado Total
        paint.color = Color.rgb(254, 242, 242)
        canvas.drawRoundRect(RectF(30f, y, 30f + cardWidth, y + cardHeight), 7f, 7f, paint)
        paint.color = COLOR_PRIMARY
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("VOLUMEN ESTIMADO A SURTIR", 38f, y + 15f, paint)
        paint.textSize = 13f
        canvas.drawText(String.format(Locale.US, "%.2f kg", totalEstimatedVolumeKg), 38f, y + 34f, paint)

        // KPI 2: Total Productos
        val kpi2X = 30f + cardWidth + 15f
        paint.color = Color.rgb(240, 253, 244)
        canvas.drawRoundRect(RectF(kpi2X, y, kpi2X + cardWidth, y + cardHeight), 7f, 7f, paint)
        paint.color = COLOR_EMERALD
        paint.textSize = 8f
        canvas.drawText("TOTAL PRODUCTOS EN LISTA", kpi2X + 8f, y + 15f, paint)
        paint.textSize = 13f
        canvas.drawText("${orders.size} ítems", kpi2X + 8f, y + 34f, paint)

        // KPI 3: Destinatario / Canal
        val kpi3X = kpi2X + cardWidth + 15f
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(kpi3X, y, kpi3X + cardWidth, y + cardHeight), 7f, 7f, paint)
        paint.color = COLOR_CHARCOAL
        paint.textSize = 8f
        canvas.drawText("DESTINATARIO OFICIAL", kpi3X + 8f, y + 15f, paint)
        paint.textSize = 11f
        canvas.drawText("Centro Distribución / Matriz", kpi3X + 8f, y + 34f, paint)

        y += cardHeight + 14f

        // --- NOTAS OPERATIVAS ---
        paint.color = COLOR_SLATE
        paint.textSize = 7.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("NOTAS DE SURTIDO:", 32f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("• Surtir cajas, barras, paquetes y bolsas cerradas según especificación de proveedor para control de inventario.", 32f, y + 11f, paint)
        canvas.drawText("• Los pesos en kilogramos corresponden a tara y cubicaje promedio estándar, sujetos a validación de báscula en recepción.", 32f, y + 21f, paint)

        y += 44f

        // --- SECCIÓN DE FIRMAS FORMALES ---
        paint.color = COLOR_CHARCOAL
        paint.strokeWidth = 1f
        canvas.drawLine(50f, y, 230f, y, paint)
        canvas.drawLine(365f, y, 545f, y, paint)

        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SOLICITA (ENCARGADO DE TIENDA)", 52f, y + 14f, paint)
        canvas.drawText("AUTORIZA / RECIBE (LOGÍSTICA)", 375f, y + 14f, paint)

        // Pie de página final
        paint.color = Color.GRAY
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Página $pageNum de $pageNum • Carnicería SGNIA • Folio: $folio", 180f, 825f, paint)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "SGNIA_Orden_Pedido_${dateShort}.pdf")
        return saveAndSharePdfFile(context, pdfDocument, outputFile, "Compartir Orden de Pedido")
    }

    // =========================================================================
    // 2. CORTE FINANCIERO Y AUDITORÍA DIARIA DE VENTAS
    // =========================================================================
    fun generateAndShareVipSalesReport(
        context: Context,
        captures: List<InventoryCaptureEntity>,
        allProducts: List<ProductEntity>,
        branchName: String = "Sucursal Matriz Centro",
        managerName: String = "Encargado de Turno"
    ): File? {
        val productMap = allProducts.associateBy { it.code }
        val pdfDocument = PdfDocument()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateShort = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val folio = "VTA-$dateShort"
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        var pageNum = 1
        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
        var canvas = page.canvas

        // --- BANNER SUPERIOR VENTAS ---
        paint.color = COLOR_HEADER_DARK
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        paint.color = COLOR_PRIMARY
        canvas.drawRect(0f, 75f, 595f, 78f, paint)

        // Textos del Banner
        paint.color = Color.WHITE
        paint.textSize = 19f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CARNICERÍA SGNIA", 30f, 36f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawText("CORTE FINANCIERO Y AUDITORÍA DIARIA DE VENTAS", 30f, 52f, paint)
        canvas.drawText("CONTROL DE KILOGRAMOS DESPLAZADOS Y FACTURACIÓN EN MOSTRADOR", 30f, 66f, paint)

        // Recuadro de Folio
        paint.color = Color.WHITE
        canvas.drawRoundRect(RectF(410f, 18f, 565f, 58f), 6f, 6f, paint)
        paint.color = COLOR_HEADER_DARK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.2f
        canvas.drawRoundRect(RectF(410f, 18f, 565f, 58f), 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = COLOR_SLATE
        canvas.drawText("FOLIO AUDITORÍA", 422f, 32f, paint)
        paint.textSize = 11f
        paint.color = COLOR_PRIMARY
        canvas.drawText(folio, 422f, 48f, paint)

        var y = 96f

        // --- BLOQUE DE METADATOS DE CORTE ---
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 60f), 8f, 8f, paint)
        paint.color = COLOR_BORDER
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(RectF(30f, y, 565f, y + 60f), 8f, 8f, paint)
        paint.style = Paint.Style.FILL

        val metaY = y + 18f
        paint.color = COLOR_CHARCOAL
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SUCURSAL:", 42f, metaY, paint)
        canvas.drawText("RESPONSABLE DE CORTE:", 42f, metaY + 15f, paint)
        canvas.drawText("SISTEMA DE PESAJE:", 42f, metaY + 30f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(branchName.ifEmpty { "Sucursal Principal" }, 170f, metaY, paint)
        canvas.drawText(managerName.ifEmpty { "Encargado de Turno" }, 170f, metaY + 15f, paint)
        canvas.drawText("Systel Cuora Max (TCP/IP Wi-Fi • Calibración Oficial)", 170f, metaY + 30f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("FECHA DE CORTE:", 360f, metaY, paint)
        canvas.drawText("ESTATUS:", 360f, metaY + 15f, paint)
        canvas.drawText("MONEDA:", 360f, metaY + 30f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(dateFormat.format(Date()), 465f, metaY, paint)
        paint.color = COLOR_EMERALD
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CONCILIADO", 465f, metaY + 15f, paint)
        paint.color = COLOR_CHARCOAL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("MXN ($ Pesos)", 465f, metaY + 30f, paint)

        y += 74f

        fun drawSalesTableHeader(c: Canvas, topY: Float) {
            paint.color = COLOR_HEADER_DARK
            c.drawRoundRect(RectF(30f, topY, 565f, topY + 24f), 5f, 5f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val thY = topY + 16f
            c.drawText("PLU", 38f, thY, paint)
            c.drawText("CORTE / PRODUCTO", 78f, thY, paint)
            c.drawText("CATEGORÍA", 235f, thY, paint)
            c.drawText("KILOS VENDIDOS", 320f, thY, paint)
            c.drawText("PRECIO/KG", 425f, thY, paint)
            c.drawText("TOTAL VENTA", 500f, thY, paint)
        }

        drawSalesTableHeader(canvas, y)
        y += 28f

        var totalKilosVendidos = 0.0
        var totalVentaDinero = 0.0

        val salesItems = captures.mapNotNull { capture ->
            val product = productMap[capture.productCode]
            val price = product?.price ?: 0.0
            val kilosSold = capture.shortage
            if (kilosSold > 0) {
                val totalMoney = kilosSold * price
                SalesRowData(
                    code = capture.productCode,
                    name = capture.productName,
                    category = product?.category ?: "Carnes",
                    kilosSold = kilosSold,
                    pricePerKg = price,
                    totalSale = totalMoney
                )
            } else null
        }.sortedByDescending { it.totalSale }

        if (salesItems.isEmpty()) {
            paint.color = COLOR_CHARCOAL
            paint.textSize = 10f
            canvas.drawText("No hay movimientos de venta registrados en la sesión actual.", 45f, y + 20f, paint)
            y += 40f
        } else {
            salesItems.forEachIndexed { index, item ->
                if (y > 710f) {
                    paint.color = Color.GRAY
                    paint.textSize = 8f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText("Página $pageNum • Carnicería SGNIA • Folio: $folio", 180f, 825f, paint)

                    pdfDocument.finishPage(page)
                    pageNum++
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
                    canvas = page.canvas

                    paint.color = COLOR_HEADER_DARK
                    canvas.drawRect(0f, 0f, 595f, 38f, paint)
                    paint.color = COLOR_PRIMARY
                    canvas.drawRect(0f, 38f, 595f, 40f, paint)

                    paint.color = Color.WHITE
                    paint.textSize = 11.5f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText("CARNICERÍA SGNIA — CORTE DE VENTAS (PÁGINA $pageNum)", 30f, 25f, paint)

                    y = 54f
                    drawSalesTableHeader(canvas, y)
                    y += 28f
                }

                if (index % 2 == 1) {
                    paint.color = COLOR_BG_LIGHT
                    canvas.drawRect(30f, y - 4f, 565f, y + 17f, paint)
                }

                totalKilosVendidos += item.kilosSold
                totalVentaDinero += item.totalSale

                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = COLOR_SLATE
                canvas.drawText("#${item.code}", 38f, y + 10f, paint)

                paint.color = COLOR_CHARCOAL
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                val safeName = if (item.name.length > 24) item.name.take(23) + "…" else item.name
                canvas.drawText(safeName, 78f, y + 10f, paint)
                canvas.drawText(item.category, 235f, y + 10f, paint)

                paint.color = COLOR_EMERALD
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(String.format(Locale.US, "%.3f kg", item.kilosSold), 320f, y + 10f, paint)

                paint.color = COLOR_CHARCOAL
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(String.format(Locale.US, "$%.2f", item.pricePerKg), 425f, y + 10f, paint)

                paint.color = COLOR_CHARCOAL
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(String.format(Locale.US, "$%.2f", item.totalSale), 500f, y + 10f, paint)

                paint.color = COLOR_BORDER
                paint.strokeWidth = 0.5f
                canvas.drawLine(30f, y + 17f, 565f, y + 17f, paint)

                y += 21f
            }
        }

        if (y > 630f) {
            paint.color = Color.GRAY
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Página $pageNum • Carnicería SGNIA • Folio: $folio", 180f, 825f, paint)

            pdfDocument.finishPage(page)
            pageNum++
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNum).create())
            canvas = page.canvas

            paint.color = COLOR_HEADER_DARK
            canvas.drawRect(0f, 0f, 595f, 38f, paint)
            paint.color = COLOR_PRIMARY
            canvas.drawRect(0f, 38f, 595f, 40f, paint)

            paint.color = Color.WHITE
            paint.textSize = 11.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("CARNICERÍA SGNIA — RESUMEN FINANCIERO (PÁG. $pageNum)", 30f, 25f, paint)

            y = 56f
        } else {
            y += 15f
        }

        // --- TARJETAS KPI DE CORTE FINANCIERO ---
        val cardWidth = 168f
        val cardHeight = 46f

        // KPI 1: Kilos Vendidos
        paint.color = Color.rgb(240, 253, 244)
        canvas.drawRoundRect(RectF(30f, y, 30f + cardWidth, y + cardHeight), 7f, 7f, paint)
        paint.color = COLOR_EMERALD
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL KILOS VENDIDOS", 38f, y + 15f, paint)
        paint.textSize = 13f
        canvas.drawText(String.format(Locale.US, "%.3f kg", totalKilosVendidos), 38f, y + 34f, paint)

        // KPI 2: Venta Total
        val kpi2X = 30f + cardWidth + 15f
        paint.color = Color.rgb(255, 247, 237)
        canvas.drawRoundRect(RectF(kpi2X, y, kpi2X + cardWidth, y + cardHeight), 7f, 7f, paint)
        paint.color = Color.rgb(194, 65, 12)
        paint.textSize = 8f
        canvas.drawText("VENTA TOTAL GENERADA", kpi2X + 8f, y + 15f, paint)
        paint.textSize = 13f
        paint.color = COLOR_CHARCOAL
        canvas.drawText(String.format(Locale.US, "$%.2f MXN", totalVentaDinero), kpi2X + 8f, y + 34f, paint)

        // KPI 3: Promedio
        val avgPrice = if (totalKilosVendidos > 0) totalVentaDinero / totalKilosVendidos else 0.0
        val kpi3X = kpi2X + cardWidth + 15f
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(RectF(kpi3X, y, kpi3X + cardWidth, y + cardHeight), 7f, 7f, paint)
        paint.color = COLOR_SLATE
        paint.textSize = 8f
        canvas.drawText("PROMEDIO FACTURADO / KG", kpi3X + 8f, y + 15f, paint)
        paint.textSize = 13f
        canvas.drawText(String.format(Locale.US, "$%.2f/kg", avgPrice), kpi3X + 8f, y + 34f, paint)

        y += cardHeight + 40f

        // --- FIRMAS FORMALES DE ENTREGA Y ARQUEO ---
        paint.color = COLOR_CHARCOAL
        paint.strokeWidth = 1f
        canvas.drawLine(50f, y, 230f, y, paint)
        canvas.drawLine(365f, y, 545f, y, paint)

        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ENTREGA (ENCARGADO DE TIENDA)", 52f, y + 14f, paint)
        canvas.drawText("RECIBE (AUDITORÍA / GERENCIA)", 375f, y + 14f, paint)

        // Pie de página final
        paint.color = Color.GRAY
        paint.textSize = 8f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Página $pageNum de $pageNum • Carnicería SGNIA • Folio: $folio", 180f, 825f, paint)

        pdfDocument.finishPage(page)

        val outputFile = File(context.cacheDir, "SGNIA_Corte_Ventas_${dateShort}.pdf")
        return saveAndSharePdfFile(context, pdfDocument, outputFile, "Compartir Corte de Ventas")
    }

    // Compartición genérica
    private fun saveAndSharePdfFile(context: Context, doc: PdfDocument, file: File, title: String): File? {
        try {
            doc.writeTo(FileOutputStream(file))
            doc.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, title))
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            doc.close()
            return null
        }
    }

    // Retrocompatibilidad
    fun generateAndSharePdf(context: Context, orders: List<InventoryCaptureEntity>): File? {
        return generateAndShareVipInventoryPdf(
            context = context,
            orders = orders,
            allProducts = emptyList(),
            branchName = "Sucursal Matriz Centro",
            managerName = "Encargado de Turno"
        )
    }
}

private data class SalesRowData(
    val code: Int,
    val name: String,
    val category: String,
    val kilosSold: Double,
    val pricePerKg: Double,
    val totalSale: Double
)
