package com.example.inventariocarniceria.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.inventariocarniceria.data.scale.ParsedPluPreview
import com.example.inventariocarniceria.data.scale.ScaleState
import com.example.inventariocarniceria.data.scale.TcpScaleDevice
import com.example.inventariocarniceria.data.local.BranchEntity
import com.example.inventariocarniceria.data.local.BranchOrderEntity
import com.example.inventariocarniceria.data.local.DespieceDetalleEntity
import com.example.inventariocarniceria.data.local.DespieceTransformacionEntity
import com.example.inventariocarniceria.data.local.InventoryCaptureEntity
import com.example.inventariocarniceria.data.local.LocalProductEntity
import com.example.inventariocarniceria.data.local.PriceAuditEntity
import com.example.inventariocarniceria.data.local.ProductEntity
import com.example.inventariocarniceria.data.local.StockEntryEntity
import com.example.inventariocarniceria.data.local.SupplierEntity
import com.example.inventariocarniceria.data.repository.InventoryRepository
import com.example.inventariocarniceria.utils.MeatBarcodeParser
import com.example.inventariocarniceria.utils.MeatBarcodeResult
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DespieceItemState(
    val productCode: Int,
    val productName: String,
    val weightKg: Double = 0.0,
    val isWaste: Boolean = false,
    val isCaptured: Boolean = false
)

data class DespieceUiState(
    val parentProductCode: Int = 20,
    val parentProductName: String = "Pierna de Cerdo sin Hueso",
    val initialWeightKg: Double = 0.0,
    val items: List<DespieceItemState> = listOf(
        DespieceItemState(21, "Bistec de Pierna de Cerdo"),
        DespieceItemState(22, "Maciza de Cerdo Fileteada"),
        DespieceItemState(23, "Maciza de Cerdo en Trozo"),
        DespieceItemState(25, "Carne Molida / Recortes Aprovechables"),
        DespieceItemState(999, "Merma / Grasa / Hueso Residual", isWaste = true)
    ),
    val notes: String = "",
    val isCompleted: Boolean = false
) {
    val totalDerivedWeightKg: Double
        get() = kotlin.math.round(items.filter { !it.isWaste }.sumOf { it.weightKg } * 1000) / 1000.0

    val totalWasteKg: Double
        get() = kotlin.math.round(items.filter { it.isWaste }.sumOf { it.weightKg } * 1000) / 1000.0

    val totalCapturedKg: Double
        get() = kotlin.math.round((totalDerivedWeightKg + totalWasteKg) * 1000) / 1000.0

    val balanceDiscrepancyKg: Double
        get() = kotlin.math.round((initialWeightKg - totalCapturedKg) * 1000) / 1000.0

    val wastePercentage: Double
        get() = if (initialWeightKg > 0) kotlin.math.round((totalWasteKg / initialWeightKg * 100.0) * 10) / 10.0 else 0.0

    val yieldPercentage: Double
        get() = if (initialWeightKg > 0) kotlin.math.round((totalDerivedWeightKg / initialWeightKg * 100.0) * 10) / 10.0 else 0.0
}

data class InventoryUiState(
    val isLoggedIn: Boolean = false,
    val currentBranch: String = "Sucursal Matriz Centro",
    val currentUserRole: String = "ENCARGADO", // ENCARGADO or ADMIN
    val adminPin: String = "1234",
    val codeInput: String = "",
    val selectedProduct: ProductEntity? = null,
    val isProductNotFound: Boolean = false,
    val snackbarMessage: String? = null,
    val isEditorOpen: Boolean = false,
    val editingProduct: ProductEntity? = null,
    val isQuickCreateOpen: Boolean = false,
    val quickCreateCode: Int? = null,
    val isPriceAdjustmentOpen: Boolean = false,
    val isQendraImportOpen: Boolean = false,
    val isDespieceOpen: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private val themePrefs = repository.scaleManager.context?.getSharedPreferences("sgnia_theme_prefs", android.content.Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        run {
            val savedMode = themePrefs?.getString("theme_mode", null)
            if (savedMode != null) {
                try {
                    com.example.inventariocarniceria.theme.SgniaThemeMode.valueOf(savedMode)
                } catch (e: Exception) {
                    com.example.inventariocarniceria.theme.SgniaThemeMode.NOIR_CREMA
                }
            } else {
                com.example.inventariocarniceria.theme.SgniaThemeMode.NOIR_CREMA
            }
        }
    )
    val themeMode: StateFlow<com.example.inventariocarniceria.theme.SgniaThemeMode> = _themeMode.asStateFlow()

    fun toggleTheme() {
        val nextMode = if (_themeMode.value == com.example.inventariocarniceria.theme.SgniaThemeMode.NOIR_CREMA) {
            com.example.inventariocarniceria.theme.SgniaThemeMode.OBSIDIAN_GOLD
        } else {
            com.example.inventariocarniceria.theme.SgniaThemeMode.NOIR_CREMA
        }
        _themeMode.value = nextMode
        themePrefs?.edit()?.putString("theme_mode", nextMode.name)?.apply()
    }

    private val _despieceState = MutableStateFlow(DespieceUiState())
    val despieceState: StateFlow<DespieceUiState> = _despieceState.asStateFlow()

    private val _scannedBarcodeResult = MutableStateFlow<MeatBarcodeResult?>(null)
    val scannedBarcodeResult: StateFlow<MeatBarcodeResult?> = _scannedBarcodeResult.asStateFlow()

    val scaleState: StateFlow<ScaleState> = repository.scaleState
    val scale1Config: StateFlow<TcpScaleDevice> = repository.scale1Config
    val scale2Config: StateFlow<TcpScaleDevice> = repository.scale2Config
    val scalesList: StateFlow<List<TcpScaleDevice>> = repository.scalesList

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentCaptures: StateFlow<List<InventoryCaptureEntity>> = repository.recentCaptures
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingOrders: StateFlow<List<InventoryCaptureEntity>> = repository.pendingOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentStockEntries: StateFlow<List<StockEntryEntity>> = repository.recentStockEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBranchOrders: StateFlow<List<BranchOrderEntity>> = repository.allBranchOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allSuppliers: StateFlow<List<SupplierEntity>> = repository.allSuppliers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBranches: StateFlow<List<BranchEntity>> = repository.allBranches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Local Products for Manager's private investment
    private val _selectedBranchFlow = MutableStateFlow("Sucursal Matriz Centro")
    val localProducts: StateFlow<List<LocalProductEntity>> = _selectedBranchFlow
        .flatMapLatest { branchId -> repository.getLocalProductsByBranch(branchId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentPriceAudits: StateFlow<List<PriceAuditEntity>> = repository.recentPriceAudits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransformaciones: StateFlow<List<DespieceTransformacionEntity>> = repository.allTransformaciones
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        onCodeInputChanged("24")
        viewModelScope.launch {
            scaleState.collectLatest { state ->
                val plu = state.lastPluCode
                if (plu != null && plu.toString() != _uiState.value.codeInput) {
                    onCodeInputChanged(plu.toString())
                }
            }
        }
    }

    fun loginUser(branchName: String, role: String) {
        _selectedBranchFlow.value = branchName
        _uiState.value = _uiState.value.copy(
            isLoggedIn = true,
            currentBranch = branchName,
            currentUserRole = role,
            snackbarMessage = "Sesión iniciada: $branchName ($role)"
        )
    }

    fun logout() {
        _uiState.value = _uiState.value.copy(isLoggedIn = false)
    }

    fun updateAdminPin(newPin: String) {
        _uiState.value = _uiState.value.copy(
            adminPin = newPin,
            snackbarMessage = "Contraseña de Admin actualizada correctamente"
        )
    }

    fun onCodeInputChanged(newCode: String) {
        _uiState.value = _uiState.value.copy(codeInput = newCode)

        viewModelScope.launch {
            val query = newCode.trim().lowercase()
            val codeNum = query.toIntOrNull()

            val products = allProducts.value
            val product = if (codeNum != null) {
                products.find { it.localCode == codeNum || it.code == codeNum }
            } else if (query.isNotEmpty()) {
                products.find {
                    it.name.lowercase().contains(query) || it.synonyms.lowercase().contains(query)
                }
            } else null

            if (product != null) {
                _uiState.value = _uiState.value.copy(
                    selectedProduct = product,
                    isProductNotFound = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    selectedProduct = null,
                    isProductNotFound = query.isNotEmpty()
                )
            }
        }
    }

    fun updateProductLocalCode(product: ProductEntity, newLocalCode: Int) {
        viewModelScope.launch {
            val updated = product.copy(localCode = newLocalCode)
            repository.saveProduct(updated)
            onCodeInputChanged(newLocalCode.toString())
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Código local actualizado: #${newLocalCode} para '${product.name}'"
            )
        }
    }

    fun captureWeight() {
        val product = _uiState.value.selectedProduct ?: return
        val currentWeight = repository.scaleState.value.currentWeight

        viewModelScope.launch {
            // Solo los productos derivados con padre no generan faltante automático (se descuentan del padre)
            val effectiveMaxStock = if (product.parentProductCode != null) currentWeight else product.maxStock
            repository.captureWeight(
                productCode = product.localCode,
                productName = product.name,
                capturedWeight = currentWeight,
                maxStock = effectiveMaxStock
            )
            // Si el producto tiene un producto padre (materia prima como Pierna sin hueso),
            // descontar automáticamente los kilos pesados del padre
            var parentMsg = ""
            if (product.parentProductCode != null && currentWeight > 0) {
                repository.deductWeightFromParent(product.parentProductCode, currentWeight, product.name)
                parentMsg = " (Descontado de Base #${product.parentProductCode})"
            }
            val formattedWeight = String.format(java.util.Locale.US, "%.3f", currentWeight)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Capturado: [${product.localCode}] ${product.name} - ${formattedWeight} kg$parentMsg"
            )
        }
    }

    fun addManualOrder(
        product: ProductEntity,
        shortageKg: Double,
        orderedUnits: Double = 0.0,
        unitType: String = "KG"
    ) {
        viewModelScope.launch {
            repository.addManualOrder(product, shortageKg, orderedUnits, unitType)
            val kgFormatted = String.format(java.util.Locale.US, "%.2f", shortageKg)
            val unitsInt = orderedUnits.toInt()
            val info = when (unitType) {
                "CAJA" -> "$unitsInt ${if (unitsInt == 1) "caja" else "cajas"} (~$kgFormatted kg)"
                "BARRA" -> "$unitsInt ${if (unitsInt == 1) "barra" else "barras"} (~$kgFormatted kg)"
                "PAQUETE" -> "$unitsInt ${if (unitsInt == 1) "paquete" else "paquetes"} (~$kgFormatted kg)"
                "BOLSA" -> "$unitsInt ${if (unitsInt == 1) "bolsa" else "bolsas"} (~$kgFormatted kg)"
                "BOTE" -> "$unitsInt ${if (unitsInt == 1) "bote" else "botes"} (~$kgFormatted kg)"
                "PIEZA" -> "$unitsInt pzas"
                else -> "$kgFormatted kg"
            }
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Pedido agregado: ${product.name} ($info)"
            )
        }
    }

    fun deleteOrderItem(productCode: Int) {
        viewModelScope.launch {
            repository.deleteCaptureByCode(productCode)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Producto eliminado del pedido"
            )
        }
    }

    fun linkDerivative(childCode: Int, parentCode: Int?) {
        viewModelScope.launch {
            repository.linkDerivative(childCode, parentCode)
            val msg = if (parentCode != null) {
                "Producto #$childCode vinculado como derivado de #$parentCode"
            } else {
                "Producto #$childCode desvinculado de producto padre"
            }
            _uiState.value = _uiState.value.copy(snackbarMessage = msg)
        }
    }

    // --- RECEPCIÓN Y ENTRADA DE STOCK ---
    fun registerStockEntry(kilosReceived: Double, supplierNotes: String?) {
        val product = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            val entry = StockEntryEntity(
                branchId = _uiState.value.currentBranch,
                productCode = product.localCode,
                productName = product.name,
                kilosReceived = kilosReceived,
                costPerKg = product.price,
                supplierNotes = supplierNotes
            )
            repository.registerStockEntry(entry)
            val formattedKilos = String.format(java.util.Locale.US, "%.3f", kilosReceived)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Entrada Registrada: ${product.name} (+${formattedKilos} kg)"
            )
        }
    }

    // --- AL VUELO: CREACIÓN RÁPIDA DE PRODUCTO ---
    fun openQuickCreateDialog() {
        val codeNum = _uiState.value.codeInput.toIntOrNull()
        _uiState.value = _uiState.value.copy(
            isQuickCreateOpen = true,
            quickCreateCode = codeNum
        )
    }

    fun closeQuickCreateDialog() {
        _uiState.value = _uiState.value.copy(
            isQuickCreateOpen = false,
            quickCreateCode = null
        )
    }

    fun saveProductAlVuelo(
        product: ProductEntity,
        initialKilos: Double,
        supplierNotes: String?
    ) {
        viewModelScope.launch {
            repository.createProductAndRegisterEntry(product, initialKilos, supplierNotes)
            closeQuickCreateDialog()
            onCodeInputChanged(product.code.toString())
            val formattedKilos = String.format(java.util.Locale.US, "%.3f", initialKilos)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "¡Creado Al Vuelo! [${product.code}] ${product.name} (+${formattedKilos} kg)"
            )
        }
    }

    // --- LOCAL PRODUCTS FOR MANAGER'S INVESTMENT ---
    fun addLocalProduct(
        code: Int = 85,
        investmentName: String = "Botanas SGNIA",
        name: String,
        category: String = "Botanas",
        piecesPerBolsita: Int = 6,
        quantityToMake: Int = 0,
        quantityInStock: Int = 0,
        costPrice: Double = 0.0,
        salePrice: Double = 15.0
    ) {
        viewModelScope.launch {
            val item = LocalProductEntity(
                branchId = _uiState.value.currentBranch,
                code = code,
                investmentName = investmentName.trim().ifEmpty { "Botanas SGNIA" },
                name = name.trim(),
                category = category,
                piecesPerBolsita = piecesPerBolsita.coerceAtLeast(1),
                quantityToMake = quantityToMake.coerceAtLeast(0),
                quantityInStock = quantityInStock.coerceAtLeast(0),
                costPrice = costPrice,
                salePrice = salePrice
            )
            repository.insertLocalProduct(item)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Inversión agregada: $name en '$investmentName'")
        }
    }

    fun updateLocalProduct(product: LocalProductEntity) {
        viewModelScope.launch {
            repository.updateLocalProduct(product)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Inversión actualizada: ${product.name}")
        }
    }

    fun updateLocalProductQuantity(product: LocalProductEntity, newQty: Int) {
        viewModelScope.launch {
            repository.updateLocalProduct(product.copy(quantityInStock = newQty.coerceAtLeast(0)))
        }
    }

    fun updateLocalProductQuantityToMake(product: LocalProductEntity, newQtyToMake: Int) {
        viewModelScope.launch {
            repository.updateLocalProduct(product.copy(quantityToMake = newQtyToMake.coerceAtLeast(0)))
        }
    }

    fun deleteLocalProduct(product: LocalProductEntity) {
        viewModelScope.launch {
            repository.deleteLocalProduct(product)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Inversión eliminada: ${product.name}")
        }
    }

    // --- SUPPLIERS & BRANCHES ---
    fun addSupplier(name: String, phone: String, contact: String, categories: String) {
        viewModelScope.launch {
            val supplier = SupplierEntity(name = name, phone = phone, contactName = contact, suppliedCategories = categories)
            repository.insertSupplier(supplier)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Proveedor registrado: $name")
        }
    }

    fun deleteSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Proveedor eliminado: ${supplier.name}")
        }
    }

    fun addBranch(branchName: String, managerName: String, phone: String, password: String) {
        viewModelScope.launch {
            val branch = BranchEntity(branchName = branchName, managerName = managerName, phone = phone, password = password)
            repository.insertBranch(branch)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Sucursal creada: $branchName")
        }
    }

    fun updateBranch(branch: BranchEntity) {
        viewModelScope.launch {
            repository.insertBranch(branch)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Sucursal actualizada: ${branch.branchName}")
        }
    }

    fun deleteBranch(branch: BranchEntity) {
        viewModelScope.launch {
            repository.deleteBranch(branch)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Sucursal eliminada: ${branch.branchName}")
        }
    }

    // --- ADMIN METHODS ---
    fun clearBranchOrders() {
        viewModelScope.launch {
            repository.clearBranchOrders()
            _uiState.value = _uiState.value.copy(snackbarMessage = "Unificación de pedidos limpiada")
        }
    }

    fun openNewProductEditor() {
        _uiState.value = _uiState.value.copy(
            isEditorOpen = true,
            editingProduct = null
        )
    }

    fun openProductEditor(product: ProductEntity) {
        _uiState.value = _uiState.value.copy(
            isEditorOpen = true,
            editingProduct = product
        )
    }

    fun closeProductEditor() {
        _uiState.value = _uiState.value.copy(
            isEditorOpen = false,
            editingProduct = null
        )
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.saveProduct(product)
            closeProductEditor()
            if (_uiState.value.codeInput.toIntOrNull() == product.code) {
                onCodeInputChanged(product.code.toString())
            }
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Producto guardado: [${product.code}] ${product.name}"
            )
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            if (_uiState.value.selectedProduct?.code == product.code) {
                _uiState.value = _uiState.value.copy(selectedProduct = null)
            }
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Producto eliminado: ${product.name}"
            )
        }
    }

    fun deleteAllProducts() {
        viewModelScope.launch {
            repository.deleteAllProducts()
            _uiState.value = _uiState.value.copy(
                selectedProduct = null,
                codeInput = "",
                snackbarMessage = "Catálogo de productos vaciado completamente"
            )
        }
    }

    fun clearAllDataForFreshStart() {
        viewModelScope.launch {
            repository.clearAllDataForFreshStart()
            _uiState.value = _uiState.value.copy(
                currentBranch = "",
                isLoggedIn = false,
                currentUserRole = "ADMIN",
                selectedProduct = null,
                codeInput = "",
                snackbarMessage = "¡Sistema reiniciado en blanco! Listo para configuración oficial de dueño."
            )
        }
    }

    fun setManualWeight(weight: Double) {
        repository.scaleManager.setManualWeight(weight)
    }

    fun clearScannedBarcodeResult() {
        _scannedBarcodeResult.value = null
    }

    fun processScannedBarcode(rawBarcode: String) {
        val products = allProducts.value
        val result = MeatBarcodeParser.parse(rawBarcode, products)
        _scannedBarcodeResult.value = result

        if (result.product != null) {
            onCodeInputChanged(result.product.localCode.toString())
            if (result.weightKg > 0.0) {
                setManualWeight(result.weightKg)
            }
            val formattedWeight = String.format(Locale.US, "%.2f", result.weightKg)
            val msg = if (result.weightKg > 0.0) {
                "📦 Escaneado: [${result.product.localCode}] ${result.product.name} • ${formattedWeight} kg"
            } else {
                "📦 Escaneado: [${result.product.localCode}] ${result.product.name} (Colocar en báscula)"
            }
            _uiState.value = _uiState.value.copy(snackbarMessage = msg)
        } else {
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "⚠️ Código no reconocido en catálogo: $rawBarcode"
            )
        }
    }

    fun selectActiveScale(scaleId: Int) {
        repository.selectActiveScale(scaleId)
    }

    fun resetToOfficialScaleIps() {
        repository.resetToOfficialScaleIps()
        _uiState.value = _uiState.value.copy(
            snackbarMessage = "IPs oficiales restablecidas: Báscula 1 (192.168.100.40) y Báscula 2 (192.168.100.50)"
        )
    }

    fun addScale(name: String, ip: String, port: Int) {
        repository.addScale(name, ip, port)
        _uiState.value = _uiState.value.copy(snackbarMessage = "Báscula '$name' agregada a la red")
    }

    fun updateScale(scale: TcpScaleDevice) {
        repository.updateScale(scale)
        _uiState.value = _uiState.value.copy(snackbarMessage = "Báscula '${scale.name}' actualizada")
    }

    fun deleteScale(scaleId: Int) {
        repository.deleteScale(scaleId)
        _uiState.value = _uiState.value.copy(snackbarMessage = "Báscula eliminada de la red")
    }

    fun updateScaleConfigs(scale1Ip: String, scale1Port: Int, scale2Ip: String, scale2Port: Int) {
        repository.updateScaleConfigs(scale1Ip, scale1Port, scale2Ip, scale2Port)
        _uiState.value = _uiState.value.copy(snackbarMessage = "Configuración de básculas guardada y aplicada")
    }

    fun pingScale(scaleId: Int, ip: String, port: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val (ok, message) = repository.pingScale(scaleId, ip, port)
            onResult(ok, message)
            if (!ok) {
                _uiState.value = _uiState.value.copy(snackbarMessage = "Báscula $scaleId: $message")
            }
        }
    }

    fun extractPlusFromScale(scaleId: Int, onResult: (List<ParsedPluPreview>, String) -> Unit) {
        viewModelScope.launch {
            val (plus, message) = repository.extractPlusFromScale(scaleId)
            onResult(plus, message)
            _uiState.value = _uiState.value.copy(snackbarMessage = message)
        }
    }

    fun importParsedPlus(plus: List<ParsedPluPreview>) {
        viewModelScope.launch {
            val count = repository.importParsedPlus(plus)
            closeQendraImportDialog()
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "¡$count productos integrados con éxito! Se conservó todo tu catálogo existente."
            )
        }
    }

    fun clearSnackbarMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun clearAllCaptures() {
        viewModelScope.launch {
            repository.clearAllCaptures()
            _uiState.value = _uiState.value.copy(snackbarMessage = "Historial y pedidos limpiados")
        }
    }

    // ==========================================
    // 1. EDICIÓN RÁPIDA DE PRECIOS & AUDITORÍA
    // ==========================================
    fun openPriceAdjustmentDialog() {
        _uiState.value = _uiState.value.copy(isPriceAdjustmentOpen = true)
    }

    fun closePriceAdjustmentDialog() {
        _uiState.value = _uiState.value.copy(isPriceAdjustmentOpen = false)
    }

    fun updateProductPrice(product: ProductEntity, newPrice: Double) {
        viewModelScope.launch {
            val user = if (_uiState.value.currentUserRole == "ADMIN") "Administrador" else "Encargado (${_uiState.value.currentBranch})"
            repository.updateProductPrice(
                product = product,
                newPrice = newPrice,
                managerName = user,
                branchId = _uiState.value.currentBranch
            )
            // Actualizar si está seleccionado actualmente
            if (_uiState.value.selectedProduct?.code == product.code) {
                _uiState.value = _uiState.value.copy(selectedProduct = product.copy(price = newPrice))
            }
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "Precio actualizado: ${product.name} -> $${String.format(java.util.Locale.US, "%.2f", newPrice)}/kg"
            )
        }
    }

    suspend fun getSystelPluExportContent(): String {
        return repository.generateSystelPluFile()
    }

    // ==========================================
    // 2. IMPORTACIÓN DE CATÁLOGO QENDRA
    // ==========================================
    fun openQendraImportDialog() {
        _uiState.value = _uiState.value.copy(isQendraImportOpen = true)
    }

    fun closeQendraImportDialog() {
        _uiState.value = _uiState.value.copy(isQendraImportOpen = false)
    }

    fun importQendraCatalog(rawText: String) {
        viewModelScope.launch {
            val count = repository.importQendraCatalog(rawText)
            closeQendraImportDialog()
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "¡Catálogo Qendra importado! $count productos actualizados/creados"
            )
        }
    }

    // ==========================================
    // 3. MÓDULO DE DESPIECE / RENDIMIENTO DE PIERNA
    // ==========================================
    fun openDespiece() {
        _uiState.value = _uiState.value.copy(isDespieceOpen = true)
    }

    fun closeDespiece() {
        _uiState.value = _uiState.value.copy(isDespieceOpen = false)
    }

    fun setDespieceInitialWeight(weight: Double) {
        _despieceState.value = _despieceState.value.copy(initialWeightKg = weight)
    }

    fun captureDespieceItemWeight(productCode: Int, weight: Double) {
        val updatedItems = _despieceState.value.items.map { item ->
            if (item.productCode == productCode) {
                item.copy(weightKg = weight, isCaptured = true)
            } else item
        }
        _despieceState.value = _despieceState.value.copy(items = updatedItems)
    }

    fun updateDespieceNotes(notes: String) {
        _despieceState.value = _despieceState.value.copy(notes = notes)
    }

    fun resetDespiece() {
        _despieceState.value = DespieceUiState()
    }

    fun finalizeDespiece() {
        val state = _despieceState.value
        if (state.initialWeightKg <= 0.0) {
            _uiState.value = _uiState.value.copy(snackbarMessage = "Error: Ingrese primero el peso inicial de la pierna")
            return
        }

        viewModelScope.launch {
            val transformacion = DespieceTransformacionEntity(
                branchId = _uiState.value.currentBranch,
                parentProductCode = state.parentProductCode,
                parentProductName = state.parentProductName,
                initialWeightKg = state.initialWeightKg,
                totalDerivedWeightKg = state.totalDerivedWeightKg,
                totalWasteKg = state.totalWasteKg,
                wastePercentage = state.wastePercentage,
                carniceroName = "Encargado (${_uiState.value.currentBranch})",
                notes = state.notes
            )

            val detalles = state.items.map { item ->
                val yield = if (state.initialWeightKg > 0) (item.weightKg / state.initialWeightKg) * 100.0 else 0.0
                DespieceDetalleEntity(
                    despieceId = 0,
                    productCode = item.productCode,
                    productName = item.productName,
                    weightKg = item.weightKg,
                    yieldPercentage = kotlin.math.round(yield * 10) / 10.0,
                    isWaste = item.isWaste
                )
            }

            val id = repository.registerDespiece(transformacion, detalles)
            _despieceState.value = _despieceState.value.copy(isCompleted = true)
            _uiState.value = _uiState.value.copy(
                isDespieceOpen = false,
                snackbarMessage = "¡Despiece #$id finalizado! Stock actualizado: Pierna (-${state.initialWeightKg}kg), Derivados (+${state.totalDerivedWeightKg}kg)"
            )
            resetDespiece()
        }
    }
}

class InventoryViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
