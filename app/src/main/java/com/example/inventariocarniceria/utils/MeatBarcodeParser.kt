package com.example.inventariocarniceria.utils

import com.example.inventariocarniceria.data.local.ProductEntity
import java.util.Locale

/**
 * Resultado de la decodificación inteligente de un código de barras de carne, caja o ticket.
 */
data class MeatBarcodeResult(
    val rawCode: String,
    val product: ProductEntity?,
    val weightKg: Double,
    val units: Int = 1,
    val unitType: String = "CAJA",
    val isWeightExtractedFromBarcode: Boolean = false,
    val explanation: String = "",
    val lotNumber: String = "",
    val boxFolio: String = ""
)

/**
 * Decodificador universal inteligente de códigos de barras y QR para cajas de empacadora,
 * rastro y tickets de balanza industrial.
 *
 * Soporta:
 * 1. Códigos industriales de empacadora delimitados por '|' (Kekén, SuKarne, TIF) con peso, lote y folio.
 * 2. Cajas con peso neto directo (ej: 22.30, 22.30 kg) o 4 dígitos (2230 -> 22.30 kg).
 * 3. Tickets de balanza Systel / EAN-13 de mostrador (prefijos 20, 21, 28, 29).
 * 4. Códigos QR con formato texto o clave-valor (ej: "PLU:45;PESO:20.0").
 */
object MeatBarcodeParser {

    fun parse(rawCode: String, allProducts: List<ProductEntity>): MeatBarcodeResult {
        val cleaned = rawCode.trim()
        if (cleaned.isEmpty()) {
            return MeatBarcodeResult(rawCode, null, 0.0, explanation = "Código vacío")
        }

        // -------------------------------------------------------------
        // FORMATO INDUSTRIAL KEKÉN / GRUPO PORCÍCOLA / SUKARNE DELIMITADO POR PIPE (|)
        // Ejemplo real escaneado: 01H1180201658|P124154A1|L0027567916|D04/10/2026|Q  22.30 |W0R00
        // Donde:
        // - 01H1180201658 -> Folio de Caja 1180201658
        // - P124154A1 -> Código de Producto de Proveedor 124154A1 (Cabeza Congelado)
        // - L0027567916 -> Lote 0027567916
        // - D04/10/2026 -> Fecha de Empaque
        // - Q  22.30   -> Peso Neto Exacto de la Caja (22.30 kg)
        // -------------------------------------------------------------
        if (cleaned.contains("|")) {
            val tokens = cleaned.split("|").map { it.trim() }
            var extractedWeight = 0.0
            var boxFolio = ""
            var lotNumber = ""
            var providerProdCode = ""

            for (token in tokens) {
                // 1. Extraer peso neto (token que inicia con Q, W, K o decimal)
                if (token.startsWith("Q", ignoreCase = true) || token.startsWith("W", ignoreCase = true) || token.startsWith("K", ignoreCase = true)) {
                    val rawNum = token.drop(1).trim().replace(",", ".")
                    val w = rawNum.toDoubleOrNull()
                    if (w != null && w in 0.10..400.0) {
                        extractedWeight = w
                    }
                } else {
                    val directNum = token.replace(",", ".").toDoubleOrNull()
                    if (directNum != null && directNum in 0.10..400.0) {
                        extractedWeight = directNum
                    }
                }

                // 2. Extraer folio de caja (token 01H... o H...)
                if (token.startsWith("01H", ignoreCase = true)) {
                    boxFolio = token.removePrefix("01H").removePrefix("01h")
                } else if (token.startsWith("H", ignoreCase = true) && token.drop(1).all { it.isDigit() }) {
                    boxFolio = token.drop(1)
                }

                // 3. Extraer código de producto del proveedor (P...)
                if (token.startsWith("P", ignoreCase = true) && token.length >= 3) {
                    providerProdCode = token.drop(1)
                }

                // 4. Extraer lote (L...)
                if (token.startsWith("L", ignoreCase = true) && token.length >= 3) {
                    lotNumber = token.drop(1)
                }
            }

            // Buscar coincidencia en catálogo de cortes
            var matchedProduct: ProductEntity? = null
            if (providerProdCode.isNotBlank()) {
                matchedProduct = findProductMatch(providerProdCode, allProducts)
                if (matchedProduct == null && providerProdCode.contains("124154", ignoreCase = true)) {
                    matchedProduct = allProducts.find { it.name.contains("cabeza", ignoreCase = true) }
                }
            }

            if (extractedWeight > 0.0 || matchedProduct != null) {
                return MeatBarcodeResult(
                    rawCode = cleaned,
                    product = matchedProduct,
                    weightKg = extractedWeight,
                    units = 1,
                    unitType = "KG",
                    isWeightExtractedFromBarcode = (extractedWeight > 0.0),
                    explanation = "📦 Caja Kekén / TIF: ${String.format(Locale.US, "%.3f", extractedWeight)} kg" +
                            if (boxFolio.isNotBlank()) " • Folio: $boxFolio" else "",
                    lotNumber = lotNumber,
                    boxFolio = boxFolio
                )
            }
        }

        // -------------------------------------------------------------
        // FORMATO 0: Código de barras con peso neto directo (ej. "22.30", "22,30", "22.30 kg", "22.3")
        // Frecuente en cajas de empacadora y rastro (Kekén, Porcícola Mexicano, SuKarne) donde un código es el peso neto
        // -------------------------------------------------------------
        val cleanWeightCandidate = cleaned.replace(" ", "")
            .replace("kg", "", ignoreCase = true)
            .replace("KG", "")
            .replace(",", ".")
        val directWeightNum = cleanWeightCandidate.toDoubleOrNull()
        if (directWeightNum != null && (cleaned.contains(".") || cleaned.contains(",") || cleaned.contains("kg", ignoreCase = true)) && directWeightNum in 0.10..400.0) {
            return MeatBarcodeResult(
                rawCode = cleaned,
                product = null,
                weightKg = directWeightNum,
                units = 1,
                unitType = "KG",
                isWeightExtractedFromBarcode = true,
                explanation = "⚖️ Peso neto extraído de caja: ${String.format(Locale.US, "%.3f", directWeightNum)} kg"
            )
        }

        // Si son 4 dígitos exactos y no existe un producto con ese código (ej. código "2230" -> 22.30 kg)
        if (cleaned.length == 4 && findProductMatch(cleaned, allProducts) == null) {
            val num4 = cleaned.toIntOrNull()
            if (num4 != null && num4 in 500..9999) {
                val weightCandidate = num4 / 100.0
                if (weightCandidate in 5.0..100.0) {
                    return MeatBarcodeResult(
                        rawCode = cleaned,
                        product = null,
                        weightKg = weightCandidate,
                        units = 1,
                        unitType = "KG",
                        isWeightExtractedFromBarcode = true,
                        explanation = "⚖️ Peso de caja (4 dígitos): ${String.format(Locale.US, "%.2f", weightCandidate)} kg"
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // FORMATO 1: Códigos QR con texto estructurado (PLU:45;PESO:20.0)
        // -------------------------------------------------------------
        if (cleaned.contains(";") || cleaned.contains("=") || cleaned.contains(":")) {
            val qrResult = parseQrStructured(cleaned, allProducts)
            if (qrResult != null) return qrResult
        }

        // -------------------------------------------------------------
        // FORMATO 2: Tickets de balanza EAN-13 (Prefijos 20, 21, 28, 29 - 13 dígitos)
        // Ejemplo: 20 00045 02000 2 -> PLU 45, 2.000 kg
        // -------------------------------------------------------------
        if (cleaned.length == 13 && (cleaned.startsWith("20") || cleaned.startsWith("21") || cleaned.startsWith("28") || cleaned.startsWith("29"))) {
            val pluPart = cleaned.substring(2, 7).toIntOrNull() ?: 0
            val weightDigits = cleaned.substring(7, 12).toDoubleOrNull() ?: 0.0
            val weightKg = weightDigits / 1000.0 // 02000 -> 2.000 kg

            val matchedProd = allProducts.find { it.localCode == pluPart || it.code == pluPart }
            if (matchedProd != null) {
                return MeatBarcodeResult(
                    rawCode = cleaned,
                    product = matchedProd,
                    weightKg = weightKg,
                    units = 1,
                    unitType = "KG",
                    isWeightExtractedFromBarcode = true,
                    explanation = "Ticket balanza Systel: PLU #$pluPart • ${String.format(Locale.US, "%.3f", weightKg)} kg"
                )
            }
        }

        // -------------------------------------------------------------
        // FORMATO 3: Cajas fijas o completas que coinciden directamente con el código
        // Si el código completo coincide con un PLU o código de barras registrado,
        // se toma ese producto directamente con su peso de catálogo (si aplica).
        // -------------------------------------------------------------
        val directProduct = findProductMatch(cleaned, allProducts)
        if (directProduct != null) {
            val standardBoxWeight = if (directProduct.weightPerBoxKg > 0.0) directProduct.weightPerBoxKg else directProduct.maxStock
            return MeatBarcodeResult(
                rawCode = cleaned,
                product = directProduct,
                weightKg = if (standardBoxWeight > 0.0) standardBoxWeight else 0.0,
                units = 1,
                unitType = if (directProduct.orderUnitType != "KG") directProduct.orderUnitType else "CAJA",
                isWeightExtractedFromBarcode = false,
                explanation = if (standardBoxWeight > 0.0) {
                    "Caja estándar: ${directProduct.name} • Peso de catálogo (${directProduct.weightPerBoxKg} kg)"
                } else {
                    "Producto identificado: ${directProduct.name} (Coloca la caja en la báscula para pesarla)"
                }
            )
        }

        // -------------------------------------------------------------
        // FORMATO 4: Cajas con peso en los últimos 4 dígitos
        // El usuario indicó: "las cajas tienen un código de barras y los últimos 4 dígitos es el peso"
        // Ejemplo: si termina en 2000 -> 20.00 kg. Si termina en 2550 -> 25.50 kg.
        // -------------------------------------------------------------
        if (cleaned.length >= 6) {
            val last4Str = cleaned.takeLast(4)
            val last4Num = last4Str.toIntOrNull()
            if (last4Num != null && last4Num > 0) {
                // Interpretar como peso con 2 decimales (ej. 2000 -> 20.00 kg, 0550 -> 5.50 kg)
                val weightCandidate = last4Num / 100.0
                val prefixCode = cleaned.dropLast(4)

                // Buscar producto por el prefijo (código que precede al peso)
                val prodByPrefix = findProductMatch(prefixCode, allProducts)
                if (prodByPrefix != null && weightCandidate in 0.20..150.0) {
                    return MeatBarcodeResult(
                        rawCode = cleaned,
                        product = prodByPrefix,
                        weightKg = weightCandidate,
                        units = 1,
                        unitType = if (prodByPrefix.orderUnitType != "KG") prodByPrefix.orderUnitType else "CAJA",
                        isWeightExtractedFromBarcode = true,
                        explanation = "Caja identificada: ${prodByPrefix.name} • Peso extraído de los últimos 4 dígitos (${last4Str}): ${String.format(Locale.US, "%.2f", weightCandidate)} kg"
                    )
                }
            }
        }

        // -------------------------------------------------------------
        // FORMATO 5: Búsqueda flexible por número entero (ej. el código es simplemente "45" o "0045")
        // -------------------------------------------------------------
        val numericCode = cleaned.toIntOrNull()
        if (numericCode != null) {
            val prodByNum = allProducts.find { it.localCode == numericCode || it.code == numericCode }
            if (prodByNum != null) {
                val weight = if (prodByNum.weightPerBoxKg > 0.0) prodByNum.weightPerBoxKg else 0.0
                return MeatBarcodeResult(
                    rawCode = cleaned,
                    product = prodByNum,
                    weightKg = weight,
                    units = 1,
                    unitType = if (prodByNum.orderUnitType != "KG") prodByNum.orderUnitType else "CAJA",
                    isWeightExtractedFromBarcode = false,
                    explanation = "PLU directo #${prodByNum.localCode}: ${prodByNum.name}"
                )
            }
        }

        return MeatBarcodeResult(
            rawCode = cleaned,
            product = null,
            weightKg = 0.0,
            explanation = "Código no reconocido ($cleaned). Puedes asignarlo a un producto en el catálogo."
        )
    }

    /**
     * Busca coincidencias de producto por código local, código numérico, sinónimos o nombre.
     */
    private fun findProductMatch(searchStr: String, allProducts: List<ProductEntity>): ProductEntity? {
        val query = searchStr.trim().lowercase()
        if (query.isEmpty()) return null

        // 1. Coincidencia exacta por PLU local
        val byLocal = allProducts.find { it.localCode.toString() == query }
        if (byLocal != null) return byLocal

        // 2. Coincidencia exacta por código principal
        val byCode = allProducts.find { it.code.toString() == query }
        if (byCode != null) return byCode

        // 3. Coincidencia exacta de token dentro del campo synonyms (donde se guardan códigos de barras o alias)
        val bySynonyms = allProducts.find { prod ->
            val list = prod.synonyms.lowercase().split(",", ";").map { it.trim() }
            list.contains(query)
        }
        if (bySynonyms != null) return bySynonyms

        // 4. Coincidencia por corte o nombre en etiquetas de empacadora (ej. "CABEZA CONGELADO" -> "CABEZA DE PUERCO")
        val byPartialName = allProducts.find { prod ->
            val pName = prod.name.lowercase()
            pName.contains(query) || query.contains(pName) ||
            (query.contains("cabeza") && pName.contains("cabeza")) ||
            (query.contains("pierna") && pName.contains("pierna")) ||
            (query.contains("menudo") && pName.contains("menudo"))
        }
        if (byPartialName != null) return byPartialName

        return null
    }

    /**
     * Parsea QR codes con sintaxis tipo "PLU:45;PESO:20.0" o "COD:45;KILOS:20"
     */
    private fun parseQrStructured(text: String, allProducts: List<ProductEntity>): MeatBarcodeResult? {
        try {
            val parts = text.split(";", "&")
            var plu: Int? = null
            var weight: Double? = null

            for (part in parts) {
                val kv = part.split("=", ":")
                if (kv.size == 2) {
                    val key = kv[0].trim().uppercase()
                    val value = kv[1].trim()
                    when (key) {
                        "PLU", "COD", "PRODUCTO", "ID", "CODE" -> plu = value.toIntOrNull()
                        "PESO", "WEIGHT", "KG", "KILOS" -> weight = value.replace(",", ".").replace("KG", "", ignoreCase = true).trim().toDoubleOrNull()
                    }
                }
            }

            if (plu != null) {
                val prod = allProducts.find { it.localCode == plu || it.code == plu }
                if (prod != null) {
                    val finalWeight = weight ?: (if (prod.weightPerBoxKg > 0) prod.weightPerBoxKg else 0.0)
                    return MeatBarcodeResult(
                        rawCode = text,
                        product = prod,
                        weightKg = finalWeight,
                        units = 1,
                        unitType = "CAJA",
                        isWeightExtractedFromBarcode = weight != null,
                        explanation = "Código QR estructurado: ${prod.name} • ${finalWeight} kg"
                    )
                }
            }
        } catch (_: Exception) { }
        return null
    }
}
