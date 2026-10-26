package com.example.inventariocarniceria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import com.example.inventariocarniceria.ui.design.NoirColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import com.example.inventariocarniceria.data.licensing.LicenseManager
import com.example.inventariocarniceria.data.preferences.LegalPreferences
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.ui.components.SgniaSidebar
import com.example.inventariocarniceria.ui.components.SgniaTopBar
import com.example.inventariocarniceria.ui.components.SgniaSidebarItem
import com.example.inventariocarniceria.ui.screens.about.AboutSgniaScreen
import com.example.inventariocarniceria.ui.screens.admin.AdminBranchesScreen
import com.example.inventariocarniceria.ui.screens.admin.AdminCatalogScreen
import com.example.inventariocarniceria.ui.screens.admin.AdminSecurityScreen
import com.example.inventariocarniceria.ui.screens.admin.AdminSuppliersScreen
import com.example.inventariocarniceria.ui.screens.admin.MasterOrderUnificationScreen
import com.example.inventariocarniceria.ui.screens.auth.SecurityVaultScreen
import com.example.inventariocarniceria.ui.screens.dashboard.ExecutiveOwnerDashboardScreen
import com.example.inventariocarniceria.ui.screens.legal.LegalAcceptanceScreen
import com.example.inventariocarniceria.ui.screens.licensing.LicenseActivationScreen
import com.example.inventariocarniceria.ui.screens.manager.ManagerInvestmentScreen
import com.example.inventariocarniceria.ui.screens.orders.OrdersScreen
import com.example.inventariocarniceria.ui.screens.reception.StockReceptionScreen
import com.example.inventariocarniceria.ui.screens.sales.SalesReversalScreen
import com.example.inventariocarniceria.ui.screens.scale.IndustrialScaleVisorScreen
import com.example.inventariocarniceria.ui.screens.settings.SettingsScreen
import com.example.inventariocarniceria.ui.screens.splash.SplashScreen
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.launch

@Composable
fun MainNavigation(viewModel: InventoryViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val pendingOrders by viewModel.pendingOrders.collectAsState()

    val context = LocalContext.current
    val isOwnerApp = context.packageName.endsWith(".admin")
    val legalPrefs = remember { LegalPreferences(context) }
    val isTermsAccepted by legalPrefs.isTermsAccepted.collectAsState(initial = null)
    var licenseInfo by remember { mutableStateOf(LicenseManager.checkLicense(context)) }
    var isHookDetected by remember { mutableStateOf(com.example.inventariocarniceria.data.security.AppSecurityGuard.isDebuggerOrHookActive()) }

    LaunchedEffect(showSplash) {
        licenseInfo = LicenseManager.checkLicense(context)
        isHookDetected = com.example.inventariocarniceria.data.security.AppSecurityGuard.isDebuggerOrHookActive()
    }

    // Monitoreo continuo de vigencia de licencia:
    // 1. Re-verificación inmediata cada vez que la app pasa a primer plano (ON_RESUME)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                licenseInfo = LicenseManager.checkLicense(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 2. Ticker en segundo plano cada 60 segundos por si el dispositivo se queda encendido en mostrador
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            licenseInfo = LicenseManager.checkLicense(context)
        }
    }

    SgniaTheme(themeMode = themeMode) {
        if (showSplash) {
            SplashScreen(
                onSplashFinished = { showSplash = false }
            )
        } else if (isHookDetected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(com.example.inventariocarniceria.ui.design.NoirColors.Background)
                    .padding(24.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = com.example.inventariocarniceria.ui.design.NoirColors.Danger,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    androidx.compose.material3.Text(
                        text = "ENTORNO NO SEGURO DETECTADO",
                        style = com.example.inventariocarniceria.ui.design.NoirType.title,
                        color = com.example.inventariocarniceria.ui.design.NoirColors.Danger
                    )
                    androidx.compose.material3.Text(
                        text = "Se ha detectado un depurador o inyector de memoria activo. Por seguridad del inventario y las ventas, la app ha suspendido el acceso.",
                        style = com.example.inventariocarniceria.ui.design.NoirType.body,
                        color = com.example.inventariocarniceria.ui.design.NoirColors.TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else if (isTermsAccepted == false) {
            LegalAcceptanceScreen(
                onAcceptanceComplete = { /* DataStore Flow actualiza automáticamente el estado */ }
            )
        } else if (licenseInfo.status != LicenseManager.LicenseStatus.ACTIVE) {
            LicenseActivationScreen(
                onActivationSuccess = {
                    licenseInfo = LicenseManager.checkLicense(context)
                }
            )
        } else if (!uiState.isLoggedIn) {
            // LOGIN EJECUTIVO ADAPTADO A SGNIA NOIR (NEGRO MATE Y CREMA)
            SecurityVaultScreen(
                viewModel = viewModel,
                themeMode = themeMode,
                onToggleTheme = { viewModel.toggleTheme() },
                onLoginSuccess = { }
            )
        } else if (isOwnerApp) {
            // =========================================================================
            // CONSOLA DEL DUEÑO: MENÚ DESPLEGABLE AL COSTADO (EXCLUSIVO APP DUEÑO .admin)
            // =========================================================================
            var selectedOwnerSection by remember { mutableStateOf("home") }
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.snackbarMessage) {
                uiState.snackbarMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearSnackbarMessage()
                }
            }

            val ownerSidebarItems = remember(pendingOrders.size) {
                listOf(
                    SgniaSidebarItem("home", "Inicio / Resumen", Icons.Default.Home),
                    SgniaSidebarItem("unificacion", "Unificación Pedidos", Icons.Default.MergeType, if (pendingOrders.isNotEmpty()) "${pendingOrders.size}" else null),
                    SgniaSidebarItem("sucursales", "Sucursales y Tiendas", Icons.Default.Business),
                    SgniaSidebarItem("proveedores", "Proveedores", Icons.Default.LocalShipping),
                    SgniaSidebarItem("seguridad", "Seguridad y PIN", Icons.Default.Lock),
                    SgniaSidebarItem("licencia", "Licencia de Software", Icons.Default.Security),
                    SgniaSidebarItem("acerca_de", "Acerca de SGNIA", Icons.Default.Info)
                )
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    SgniaSidebar(
                        brandTitle = "SGNIA",
                        subtitle = "CONSOLA DEL DUEÑO",
                        items = ownerSidebarItems,
                        selectedItemId = selectedOwnerSection,
                        onSelectItem = { sectionId ->
                            selectedOwnerSection = sectionId
                            scope.launch { drawerState.close() }
                        },
                        onLogout = {
                            scope.launch { drawerState.close() }
                            viewModel.logout()
                        },
                        supportText = "Administrador Principal"
                    )
                }
            ) {
                Scaffold(
                    containerColor = SgniaTheme.colors.backgroundPrimary,
                    snackbarHost = {
                        SnackbarHost(snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = NoirColors.SurfaceRaised,
                                contentColor = NoirColors.TextPrimary,
                                actionColor = NoirColors.Cream
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SgniaTheme.colors.backgroundPrimary)
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        SgniaTopBar(
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            searchPlaceholder = "Buscar pedidos o sucursal...",
                            onSearchClick = { selectedOwnerSection = "unificacion" },
                            onToggleTheme = { viewModel.toggleTheme() }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedOwnerSection) {
                                "home" -> ExecutiveOwnerDashboardScreen(
                                    viewModel = viewModel,
                                    themeMode = themeMode,
                                    onToggleTheme = { viewModel.toggleTheme() },
                                    onLogout = { viewModel.logout() },
                                    onOpenAdminConsole = { selectedOwnerSection = "unificacion" },
                                    onNavigateToSection = { section -> selectedOwnerSection = section }
                                )
                                "unificacion" -> MasterOrderUnificationScreen(viewModel = viewModel)
                                "sucursales" -> AdminBranchesScreen(viewModel = viewModel)
                                "proveedores" -> AdminSuppliersScreen(viewModel = viewModel)
                                "seguridad" -> AdminSecurityScreen(viewModel = viewModel)
                                "licencia" -> LicenseActivationScreen(onBack = { selectedOwnerSection = "home" })
                                "acerca_de" -> AboutSgniaScreen(onBack = { selectedOwnerSection = "home" })
                                else -> MasterOrderUnificationScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        } else {
            // =========================================================================
            // PANEL DE TIENDA: BÁSCULAS COMO MENÚ PRINCIPAL + RESTO EN MENÚ DESPLEGABLE
            // =========================================================================
            var selectedStoreSection by remember { mutableStateOf("basculas") }
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.snackbarMessage) {
                uiState.snackbarMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearSnackbarMessage()
                }
            }

            val allSaleTickets by viewModel.allSaleTickets.collectAsState()
            val storeSidebarItems = remember(allProducts.size, pendingOrders.size, allSaleTickets.size) {
                listOf(
                    SgniaSidebarItem("basculas", "Básculas (Principal)", Icons.Default.Scale),
                    SgniaSidebarItem("catalogo", "Catálogo de Cortes", Icons.Default.Inventory2, "${allProducts.size}"),
                    SgniaSidebarItem("recepcion", "Recepción Mercancía", Icons.Default.AddBusiness),
                    SgniaSidebarItem("devoluciones", "Invertir Venta / Bajas", Icons.Default.AssignmentReturn, if (allSaleTickets.isNotEmpty()) "${allSaleTickets.size}" else null),
                    SgniaSidebarItem("pedidos", "Pedidos de Surtido", Icons.AutoMirrored.Filled.ReceiptLong, if (pendingOrders.isNotEmpty()) "${pendingOrders.size}" else null),
                    SgniaSidebarItem("inversion", "Mi Inversión Local", Icons.Default.CardGiftcard),
                    SgniaSidebarItem("ajustes", "Ajustes de Sistema", Icons.Default.Settings),
                    SgniaSidebarItem("licencia", "Licencia de Software", Icons.Default.Security),
                    SgniaSidebarItem("acerca_de", "Acerca de SGNIA", Icons.Default.Info)
                )
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    SgniaSidebar(
                        brandTitle = "SGNIA",
                        subtitle = uiState.currentBranch.uppercase(),
                        items = storeSidebarItems,
                        selectedItemId = selectedStoreSection,
                        onSelectItem = { sectionId ->
                            selectedStoreSection = sectionId
                            scope.launch { drawerState.close() }
                        },
                        onLogout = {
                            scope.launch { drawerState.close() }
                            viewModel.logout()
                        },
                        supportText = "2 Básculas TCP/IP"
                    )
                }
            ) {
                Scaffold(
                    containerColor = SgniaTheme.colors.backgroundPrimary,
                    snackbarHost = {
                        SnackbarHost(snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = NoirColors.SurfaceRaised,
                                contentColor = NoirColors.TextPrimary,
                                actionColor = NoirColors.Cream
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SgniaTheme.colors.backgroundPrimary)
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (selectedStoreSection != "basculas") {
                            SgniaTopBar(
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                searchPlaceholder = "Buscar...",
                                onSearchClick = if (selectedStoreSection in listOf("catalogo", "recepcion", "devoluciones")) null else { { selectedStoreSection = "catalogo" } },
                                onToggleTheme = null
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedStoreSection) {
                                "basculas" -> IndustrialScaleVisorScreen(
                                    viewModel = viewModel,
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                                "catalogo" -> AdminCatalogScreen(viewModel = viewModel)
                                "recepcion" -> StockReceptionScreen(viewModel = viewModel)
                                "devoluciones" -> SalesReversalScreen(viewModel = viewModel)
                                "pedidos" -> OrdersScreen(viewModel = viewModel)
                                "inversion" -> ManagerInvestmentScreen(viewModel = viewModel)
                                "ajustes" -> SettingsScreen(
                                    viewModel = viewModel,
                                    onOpenAbout = { selectedStoreSection = "acerca_de" },
                                    onOpenLicensing = { selectedStoreSection = "licencia" }
                                )
                                "licencia" -> LicenseActivationScreen(onBack = { selectedStoreSection = "ajustes" })
                                "acerca_de" -> AboutSgniaScreen(onBack = { selectedStoreSection = "ajustes" })
                            }
                        }
                    }
                }
            }
        }
    }
}
