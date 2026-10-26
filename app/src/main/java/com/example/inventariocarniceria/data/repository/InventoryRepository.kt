package com.example.inventariocarniceria.data.repository

import com.example.inventariocarniceria.data.scale.TcpScaleManager
import com.example.inventariocarniceria.data.scale.ParsedPluPreview
import com.example.inventariocarniceria.data.scale.ScaleState
import com.example.inventariocarniceria.data.scale.TcpScaleDevice
import com.example.inventariocarniceria.data.local.BranchDao
import com.example.inventariocarniceria.data.local.BranchEntity
import com.example.inventariocarniceria.data.local.BranchOrderDao
import com.example.inventariocarniceria.data.local.BranchOrderEntity
import com.example.inventariocarniceria.data.local.DespieceDao
import com.example.inventariocarniceria.data.local.DespieceDetalleEntity
import com.example.inventariocarniceria.data.local.DespieceTransformacionEntity
import com.example.inventariocarniceria.data.local.InventoryCaptureDao
import com.example.inventariocarniceria.data.local.InventoryCaptureEntity
import com.example.inventariocarniceria.data.local.LocalProductDao
import com.example.inventariocarniceria.data.local.LocalProductEntity
import com.example.inventariocarniceria.data.local.PriceAuditDao
import com.example.inventariocarniceria.data.local.PriceAuditEntity
import com.example.inventariocarniceria.data.local.ProductDao
import com.example.inventariocarniceria.data.local.ProductEntity
import com.example.inventariocarniceria.data.local.StockEntryDao
import com.example.inventariocarniceria.data.local.StockEntryEntity
import com.example.inventariocarniceria.data.local.SaleTicketDao
import com.example.inventariocarniceria.data.local.SaleTicketEntity
import com.example.inventariocarniceria.data.local.SupplierDao
import com.example.inventariocarniceria.data.local.SupplierEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class InventoryRepository(
    private val productDao: ProductDao,
    private val captureDao: InventoryCaptureDao,
    private val stockEntryDao: StockEntryDao,
    private val branchOrderDao: BranchOrderDao,
    private val localProductDao: LocalProductDao,
    private val supplierDao: SupplierDao,
    private val branchDao: BranchDao,
    private val priceAuditDao: PriceAuditDao,
    private val despieceDao: DespieceDao,
    val scaleManager: TcpScaleManager,
    private val saleTicketDao: SaleTicketDao? = null
) {
    val scaleState: StateFlow<ScaleState> = scaleManager.scaleState
    val scale1Config: StateFlow<TcpScaleDevice> = scaleManager.scale1Config
    val scale2Config: StateFlow<TcpScaleDevice> = scaleManager.scale2Config
    val scalesList: StateFlow<List<TcpScaleDevice>> = scaleManager.scalesList

    fun selectActiveScale(scaleId: Int) = scaleManager.selectActiveScale(scaleId)
    fun resetToOfficialScaleIps() = scaleManager.resetToOfficialScaleIps()
    fun updateScaleConfigs(scale1Ip: String, scale1Port: Int, scale2Ip: String, scale2Port: Int) =
        scaleManager.updateScaleConfigs(scale1Ip, scale1Port, scale2Ip, scale2Port)
    fun addScale(
        name: String,
        ip: String,
        port: Int,
        brand: String = "Systel",
        model: String = "Cuora Max",
        protocol: String = "AUTO"
    ) = scaleManager.addScale(name, ip, port, brand, model, protocol)
    fun updateScale(scale: TcpScaleDevice) = scaleManager.updateScale(scale)
    fun deleteScale(scaleId: Int) = scaleManager.deleteScale(scaleId)
    suspend fun pingScale(scaleId: Int = 0, ip: String, port: Int) = scaleManager.pingScale(scaleId, ip, port)
    suspend fun extractPlusFromScale(scaleId: Int) = scaleManager.extractPlusFromScale(scaleId)
    fun setManualWeight(weight: Double) = scaleManager.setManualWeight(weight)
    fun clearManualWeight() = scaleManager.clearManualWeight()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val recentCaptures: Flow<List<InventoryCaptureEntity>> = captureDao.getRecentCaptures()
    val pendingOrders: Flow<List<InventoryCaptureEntity>> = captureDao.getPendingOrders()
    val recentStockEntries: Flow<List<StockEntryEntity>> = stockEntryDao.getRecentEntries()
    val allBranchOrders: Flow<List<BranchOrderEntity>> = branchOrderDao.getAllBranchOrders()
    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    val allBranches: Flow<List<BranchEntity>> = branchDao.getAllBranches()
    val recentPriceAudits: Flow<List<PriceAuditEntity>> = priceAuditDao.getRecentAudits()
    val allTransformaciones: Flow<List<DespieceTransformacionEntity>> = despieceDao.getAllTransformaciones()
    val allSaleTickets: Flow<List<SaleTicketEntity>> = saleTicketDao?.getAllTickets() ?: flowOf(emptyList())

    fun getLocalProductsByBranch(branchId: String): Flow<List<LocalProductEntity>> {
        return localProductDao.getLocalProductsByBranch(branchId)
    }

    suspend fun getProductByCode(code: Int): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductByCode(code)
    }

    suspend fun saveProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
        captureDao.deleteCapturesByCode(product.code)
    }

    suspend fun registerStockEntry(entry: StockEntryEntity) = withContext(Dispatchers.IO) {
        stockEntryDao.insertStockEntry(entry)
    }

    suspend fun createProductAndRegisterEntry(
        product: ProductEntity,
        kilosReceived: Double,
        supplierNotes: String?
    ) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
        val entry = StockEntryEntity(
            productCode = product.code,
            productName = product.name,
            kilosReceived = kilosReceived,
            costPerKg = product.price,
            supplierNotes = supplierNotes ?: "Creación Al Vuelo"
        )
        stockEntryDao.insertStockEntry(entry)
    }

    suspend fun captureWeight(
        productCode: Int,
        productName: String,
        capturedWeight: Double,
        maxStock: Double
    ) = withContext(Dispatchers.IO) {
        val shortageRaw = maxStock - capturedWeight
        val shortage = if (shortageRaw > 0) kotlin.math.round(shortageRaw * 1000) / 1000.0 else 0.0

        captureDao.deleteCapturesByCode(productCode)

        val product = productDao.getProductByCode(productCode)
        val unitType = product?.orderUnitType ?: "KG"
        val boxWeight = product?.weightPerBoxKg ?: 0.0
        val orderedUnits = if (shortage > 0) {
            if (boxWeight > 0) {
                kotlin.math.max(1.0, kotlin.math.ceil(shortage / boxWeight))
            } else if (unitType == "PIEZA") {
                kotlin.math.max(1.0, kotlin.math.ceil(shortage))
            } else {
                shortage
            }
        } else {
            0.0
        }

        val capture = InventoryCaptureEntity(
            productCode = productCode,
            productName = productName,
            capturedWeight = capturedWeight,
            maxStock = maxStock,
            shortage = shortage,
            orderedUnits = orderedUnits,
            unitType = unitType
        )
        captureDao.insertCapture(capture)

        // Actualizar la existencia en unidades si el producto tiene presentación por caja/peso
        if (product != null && boxWeight > 0) {
            val currentUnits = capturedWeight / boxWeight
            productDao.updateProduct(product.copy(currentStockUnits = currentUnits))
        }
    }

    suspend fun addManualOrder(
        product: ProductEntity,
        shortageKg: Double,
        orderedUnits: Double = 0.0,
        unitType: String = "KG"
    ) = withContext(Dispatchers.IO) {
        captureDao.deleteCapturesByCode(product.code)
        val effectiveUnitType = if (unitType.isNotBlank() && unitType != "KG") unitType else product.orderUnitType
        val boxWeight = product.weightPerBoxKg
        val effectiveUnits = if (orderedUnits > 0) {
            orderedUnits
        } else if (boxWeight > 0 && shortageKg > 0) {
            kotlin.math.max(1.0, kotlin.math.ceil(shortageKg / boxWeight))
        } else if (effectiveUnitType == "PIEZA" && shortageKg > 0) {
            kotlin.math.max(1.0, kotlin.math.ceil(shortageKg))
        } else {
            shortageKg
        }

        val capture = InventoryCaptureEntity(
            productCode = product.code,
            productName = product.name,
            capturedWeight = (product.maxStock - shortageKg).coerceAtLeast(0.0),
            maxStock = product.maxStock,
            shortage = shortageKg,
            orderedUnits = effectiveUnits,
            unitType = effectiveUnitType
        )
        captureDao.insertCapture(capture)
    }

    suspend fun deleteCaptureByCode(productCode: Int) = withContext(Dispatchers.IO) {
        captureDao.deleteCapturesByCode(productCode)
    }

    suspend fun deductWeightFromParent(
        parentCode: Int,
        weightKg: Double,
        derivedName: String
    ) = withContext(Dispatchers.IO) {
        val parent = productDao.getProductByCode(parentCode) ?: return@withContext
        val newMaxStock = (parent.maxStock - weightKg).coerceAtLeast(0.0)
        val newUnits = if (parent.weightPerBoxKg > 0) {
            newMaxStock / parent.weightPerBoxKg
        } else {
            (parent.currentStockUnits - (if (parent.unitsPerBox > 0) weightKg / parent.unitsPerBox else 0.0)).coerceAtLeast(0.0)
        }
        val updatedParent = parent.copy(
            maxStock = newMaxStock,
            currentStockUnits = newUnits
        )
        productDao.updateProduct(updatedParent)

        // Registrar auditoría de salida por derivado
        val entry = StockEntryEntity(
            productCode = parent.code,
            productName = parent.name,
            kilosReceived = -weightKg,
            costPerKg = parent.price,
            supplierNotes = "Deducción automática por derivado: $derivedName (-${String.format(java.util.Locale.US, "%.3f", weightKg)} kg)"
        )
        stockEntryDao.insertStockEntry(entry)
    }

    fun getDerivatives(parentCode: Int): Flow<List<ProductEntity>> = productDao.getDerivatives(parentCode)

    suspend fun getDerivativesList(parentCode: Int): List<ProductEntity> = withContext(Dispatchers.IO) {
        productDao.getDerivativesList(parentCode)
    }

    suspend fun linkDerivative(childCode: Int, parentCode: Int?) = withContext(Dispatchers.IO) {
        productDao.updateParentCode(childCode, parentCode)
    }

    suspend fun clearAllCaptures() = withContext(Dispatchers.IO) {
        captureDao.clearAllCaptures()
    }

    suspend fun clearBranchOrders() = withContext(Dispatchers.IO) {
        branchOrderDao.clearAllBranchOrders()
    }

    // Local Products
    suspend fun insertLocalProduct(product: LocalProductEntity) = withContext(Dispatchers.IO) {
        localProductDao.insertLocalProduct(product)
    }

    suspend fun updateLocalProduct(product: LocalProductEntity) = withContext(Dispatchers.IO) {
        localProductDao.updateLocalProduct(product)
    }

    suspend fun deleteLocalProduct(product: LocalProductEntity) = withContext(Dispatchers.IO) {
        localProductDao.deleteLocalProduct(product)
    }

    // Suppliers & Branches
    suspend fun insertSupplier(supplier: SupplierEntity) = withContext(Dispatchers.IO) {
        supplierDao.insertSupplier(supplier)
    }

    suspend fun deleteSupplier(supplier: SupplierEntity) = withContext(Dispatchers.IO) {
        supplierDao.deleteSupplier(supplier)
    }

    suspend fun insertBranch(branch: BranchEntity) = withContext(Dispatchers.IO) {
        branchDao.insertBranch(branch)
    }

    suspend fun deleteBranch(branch: BranchEntity) = withContext(Dispatchers.IO) {
        branchDao.deleteBranch(branch)
    }

    // --- 1. EDICIÓN RÁPIDA DE PRECIOS & AUDITORÍA ---
    suspend fun updateProductPrice(
        product: ProductEntity,
        newPrice: Double,
        managerName: String,
        branchId: String
    ) = withContext(Dispatchers.IO) {
        val oldPrice = product.price
        val updated = product.copy(price = newPrice)
        productDao.updateProduct(updated)

        val audit = PriceAuditEntity(
            productCode = product.code,
            productName = product.name,
            oldPrice = oldPrice,
            newPrice = newPrice,
            managerName = managerName,
            branchId = branchId
        )
        priceAuditDao.insertAudit(audit)
    }

    // --- EXPORTAR ARCHIVO PLANO SYSTEL / QENDRA ---
    suspend fun generateSystelPluFile(): String = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsList()
        val sb = StringBuilder()
        sb.append("# ARCHIVO PLANO DE ACTUALIZACION DE PRECIOS - BASCULAS SYSTEL / QENDRA\n")
        sb.append("# GENERADO POR CARNICERIA SGNIA VIP PRO v5.0\n")
        sb.append("# FORMATO: CODIGO_PLU;NOMBRE_PRODUCTO;PRECIO_KG;SECCION\n")
        products.forEach { p ->
            val section = when (p.category.lowercase()) {
                "res" -> 1
                "cerdo" -> 2
                "pollo" -> 3
                "quesos", "lacteos" -> 4
                "embutidos", "jamones", "salchichas" -> 5
                else -> 9
            }
            val formattedPrice = String.format(java.util.Locale.US, "%.2f", p.price)
            sb.append("${p.localCode};${p.name};$formattedPrice;$section\n")
        }
        sb.toString()
    }

    // --- 2. IMPORTAR CATÁLOGO EXISTENTE DE QENDRA ---
    suspend fun importQendraCatalog(content: String): Int = withContext(Dispatchers.IO) {
        var count = 0
        val lines = content.lines()
        val productsToInsert = mutableListOf<ProductEntity>()

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) continue

            // Soporta delimitadores comunes de Qendra: ; | , \t
            val delimiter = when {
                line.contains(";") -> ";"
                line.contains("|") -> "|"
                line.contains("\t") -> "\t"
                line.contains(",") -> ","
                else -> null
            } ?: continue

            val tokens = line.split(delimiter).map { it.trim() }
            if (tokens.size >= 3) {
                val code = tokens[0].toIntOrNull() ?: continue
                val name = tokens[1]
                val price = tokens[2].replace("$", "").toDoubleOrNull() ?: 0.0
                val category = if (tokens.size >= 4) {
                    when (tokens[3].lowercase()) {
                        "1", "res" -> "Res"
                        "2", "cerdo" -> "Cerdo"
                        "3", "pollo" -> "Pollo"
                        "4", "quesos", "lacteos" -> "Quesos"
                        "5", "embutidos", "jamones", "salchichas" -> "Embutidos"
                        else -> "Otros"
                    }
                } else "Carnicería"

                val existing = productDao.getProductByCode(code)
                val product = if (existing != null) {
                    existing.copy(
                        name = name.ifEmpty { existing.name },
                        price = if (price > 0) price else existing.price,
                        category = category
                    )
                } else {
                    ProductEntity(
                        code = code,
                        localCode = code,
                        name = name,
                        price = price,
                        category = category,
                        maxStock = 20.0
                    )
                }
                productsToInsert.add(product)
                count++
            }
        }

        if (productsToInsert.isNotEmpty()) {
            productDao.insertProducts(productsToInsert)
        }
        count
    }

    suspend fun importParsedPlus(plus: List<ParsedPluPreview>): Int = withContext(Dispatchers.IO) {
        var count = 0
        val productsToInsert = mutableListOf<ProductEntity>()
        for (plu in plus) {
            val existing = productDao.getProductByCode(plu.code)
            val category = autoClassifyCategory(plu.name, plu.section)
            val product = if (existing != null) {
                existing.copy(
                    name = plu.name.ifEmpty { existing.name },
                    price = if (plu.price > 0) plu.price else existing.price,
                    category = category
                )
            } else {
                ProductEntity(
                    code = plu.code,
                    localCode = plu.code,
                    name = plu.name,
                    price = plu.price,
                    category = category,
                    maxStock = 20.0
                )
            }
            productsToInsert.add(product)
            count++
        }
        if (productsToInsert.isNotEmpty()) {
            productDao.insertProducts(productsToInsert)
        }
        count
    }

    fun autoClassifyCategory(name: String, section: String = ""): String {
        val n = name.uppercase(java.util.Locale.ROOT)
        val s = section.uppercase(java.util.Locale.ROOT)
        return when {
            // 1. Quesos y Lácteos
            n.contains("QUESO") || n.contains("QESO") || n.contains("CREMA") ||
            n.contains("LECHE") || n.contains("MANTEQUILLA") || n.contains("YOGUR") ||
            n.contains("PANELA") || n.contains("OAXACA") || n.contains("ASADERO") ||
            s == "4" || s.contains("QUESO") || s.contains("LACTEO") -> "Quesos / Lácteos"

            // 2. Embutidos
            n.contains("JAMON") || n.contains("JAMÓN") || n.contains("SALCHICHA") ||
            n.contains("MORTADELA") || n.contains("PASTEL") || n.contains("TOCINO") ||
            s == "5" || s.contains("EMBUTIDO") -> "Embutidos"

            // 3. Pollo y Aves
            n.contains("POLLO") || n.contains("PECHUGA") || n.contains("ALITA") ||
            n.contains("ALIATA") || n.contains("MUSLO") || n.contains("MOLLEJA") ||
            n.contains("PATO") || n.contains("PAVO") || n.contains("GALLINA") ||
            s == "3" || s.contains("POLLO") -> "Pollo"

            // 4. Cerdo / Puerco
            n.contains("PUERCO") || n.contains("CERDO") || n.contains("CHAMORRO") ||
            n.contains("PASTOR") || n.contains("CHULETA") || n.contains("MANITA") ||
            n.contains("PIERNA") || n.contains("LOMO") || n.contains("LONGANIZA") ||
            n.contains("CHORIZO") || n.contains("CABEZA") || n.contains("MANTECA") ||
            n.contains("CHICHARRON") || n.contains("CHICHARRÓN") || n.contains("CARNITAS") ||
            s == "2" || s.contains("CERDO") || s.contains("PUERCO") -> "Cerdo"

            // 5. Res / Vacuno
            n.contains("RES") || n.contains("MENUDO") || n.contains("ESPINAZO") ||
            n.contains("COSTILLA") || n.contains("ARRACHERA") || n.contains("SUADERO") ||
            n.contains("LENGUA") || n.contains("BISTEC") || n.contains("BISTEK") ||
            n.contains("NEW YORK") || n.contains("RIB EYE") || n.contains("T-BONE") ||
            n.contains("PANZA") || n.contains("TRIPA") || n.contains("HIGADO") ||
            n.contains("HÍGADO") || n.startsWith("R.") || n.contains("R. ") ||
            n.contains("MILANESA") || n.contains("CHAMBERETE") || n.contains("DIEZMILLO") ||
            s == "1" || s.contains("RES") -> "Res"

            // 6. Otros (Pescados, Mariscos, Especias, Abarrotes)
            else -> "Otros"
        }
    }

    suspend fun fixUncategorizedProducts(): Int = withContext(Dispatchers.IO) {
        val all = productDao.getAllProductsList()
        val toUpdate = mutableListOf<ProductEntity>()
        for (p in all) {
            val cat = p.category.trim()
            if (cat.isBlank() || cat.equals("Carnicería", ignoreCase = true) || cat.equals("General", ignoreCase = true) ||
                (cat.equals("Res", ignoreCase = true) && (p.name.contains("PUERCO", ignoreCase = true) || p.name.contains("POLLO", ignoreCase = true) || p.name.contains("JAMON", ignoreCase = true) || p.name.contains("QESO", ignoreCase = true)))) {
                val correctCategory = autoClassifyCategory(p.name, p.category)
                if (correctCategory != p.category) {
                    toUpdate.add(p.copy(category = correctCategory))
                }
            }
        }
        if (toUpdate.isNotEmpty()) {
            productDao.insertProducts(toUpdate)
        }
        toUpdate.size
    }

    suspend fun getAllBranchesList(): List<BranchEntity> = withContext(Dispatchers.IO) {
        branchDao.getAllBranchesList()
    }

    // --- 3. MÓDULO DE DESPIECE / RENDIMIENTO DE PIERNA SIN HUESO ---
    suspend fun registerDespiece(
        transformacion: DespieceTransformacionEntity,
        detalles: List<DespieceDetalleEntity>
    ): Long = withContext(Dispatchers.IO) {
        // Guardar transformación y sus detalles
        val despieceId = despieceDao.insertDespieceCompleto(transformacion, detalles)

        // Descontar materia prima del stock (kilos negativos)
        val parentEntry = StockEntryEntity(
            branchId = transformacion.branchId,
            productCode = transformacion.parentProductCode,
            productName = transformacion.parentProductName,
            kilosReceived = -transformacion.initialWeightKg,
            costPerKg = 0.0,
            supplierNotes = "Salida por Despiece Lote #$despieceId"
        )
        stockEntryDao.insertStockEntry(parentEntry)

        // Sumar al stock cada derivado vendible
        detalles.filter { !it.isWaste && it.weightKg > 0 }.forEach { det ->
            val derivedEntry = StockEntryEntity(
                branchId = transformacion.branchId,
                productCode = det.productCode,
                productName = det.productName,
                kilosReceived = det.weightKg,
                costPerKg = 0.0,
                supplierNotes = "Entrada por Rendimiento Despiece Lote #$despieceId"
            )
            stockEntryDao.insertStockEntry(derivedEntry)
        }

        despieceId
    }

    suspend fun getDetallesForDespiece(despieceId: Long): List<DespieceDetalleEntity> = withContext(Dispatchers.IO) {
        despieceDao.getDetallesForDespiece(despieceId)
    }

    // --- 4. GESTIÓN DE VENTAS, TICKETS Y DEVOLUCIONES / INVERSIÓN DE VENTA ---
    suspend fun recordSaleTicket(
        productCode: Int,
        productName: String,
        weightKg: Double,
        pricePerKg: Double,
        operationNumber: String? = null,
        ticketBarcode: String? = null,
        branchId: String = "Sucursal Principal SGNIA",
        cashierName: String = "Encargado de Turno"
    ): SaleTicketEntity = withContext(Dispatchers.IO) {
        val opNum = operationNumber?.ifBlank { null } ?: "OP-${System.currentTimeMillis() % 1000000}"
        val barcode = ticketBarcode?.ifBlank { null } ?: ("20" + String.format(java.util.Locale.US, "%04d", productCode % 10000) + String.format(java.util.Locale.US, "%05d", (weightKg * 1000).toInt().coerceAtLeast(0)) + "0")
        val total = Math.round(weightKg * pricePerKg * 100.0) / 100.0

        val ticket = SaleTicketEntity(
            operationNumber = opNum,
            ticketBarcode = barcode,
            branchId = branchId,
            productCode = productCode,
            productName = productName,
            weightKg = weightKg,
            pricePerKg = pricePerKg,
            totalAmount = total,
            timestamp = System.currentTimeMillis(),
            status = "COMPLETADA",
            cashierName = cashierName
        )
        val insertedId = saleTicketDao?.insertTicket(ticket) ?: 0L
        ticket.copy(id = insertedId)
    }

    suspend fun reverseSaleTicket(
        ticketId: Long,
        reason: String
    ): Boolean = withContext(Dispatchers.IO) {
        val dao = saleTicketDao ?: return@withContext false
        val ticket = dao.getTicketById(ticketId) ?: return@withContext false
        if (ticket.status == "INVERTIDA") return@withContext true // ya estaba invertida

        val updated = ticket.copy(
            status = "INVERTIDA",
            reversalReason = reason,
            reversalTimestamp = System.currentTimeMillis()
        )
        dao.updateTicket(updated)

        // Reingresar la mercancía al inventario físico mediante StockEntry
        val returnStock = StockEntryEntity(
            branchId = ticket.branchId,
            productCode = ticket.productCode,
            productName = ticket.productName,
            kilosReceived = ticket.weightKg,
            costPerKg = ticket.pricePerKg,
            supplierNotes = "Reingreso por Devolución / Venta Invertida #${ticket.operationNumber} ($reason)",
            timestamp = System.currentTimeMillis()
        )
        stockEntryDao.insertStockEntry(returnStock)
        true
    }

    suspend fun findSaleTicket(query: String): SaleTicketEntity? = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isEmpty()) return@withContext null
        saleTicketDao?.findExactTicket(q)
    }

    // --- REINICIO TOTAL EN BLANCO PARA DUEÑO OFICIAL ---
    suspend fun clearAllDataForFreshStart() = withContext(Dispatchers.IO) {
        productDao.deleteAllProducts()
        localProductDao.deleteAllLocalProducts()
        branchDao.deleteAllBranches()
        branchOrderDao.clearAllBranchOrders()
        captureDao.clearAllCaptures()
        stockEntryDao.clearAllStockEntries()
        saleTicketDao?.clearAllTickets()
    }

    suspend fun deleteAllProducts() = withContext(Dispatchers.IO) {
        productDao.deleteAllProducts()
        captureDao.clearAllCaptures()
    }
}
