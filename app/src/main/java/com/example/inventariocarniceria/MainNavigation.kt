package com.example.inventariocarniceria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.ui.components.SgniaCerhanSidebar
import com.example.inventariocarniceria.ui.components.SgniaCerhanTopBar
import com.example.inventariocarniceria.ui.components.SgniaSidebarItem
import com.example.inventariocarniceria.ui.screens.admin.AdminBranchesScreen
import com.example.inventariocarniceria.ui.screens.admin.AdminCatalogScreen
import com.example.inventariocarniceria.ui.screens.admin.AdminSecurityScreen
import com.example.inventariocarniceria.ui.screens.admin.AdminSuppliersScreen
import com.example.inventariocarniceria.ui.screens.admin.MasterOrderUnificationScreen
import com.example.inventariocarniceria.ui.screens.auth.SecurityVaultScreen
import com.example.inventariocarniceria.ui.screens.dashboard.ExecutiveOwnerDashboardScreen
import com.example.inventariocarniceria.ui.screens.manager.ManagerInvestmentScreen
import com.example.inventariocarniceria.ui.screens.orders.OrdersScreen
import com.example.inventariocarniceria.ui.screens.reception.StockReceptionScreen
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

    SgniaTheme(themeMode = themeMode) {
        if (showSplash) {
            SplashScreen(
                onSplashFinished = { showSplash = false }
            )
        } else if (!uiState.isLoggedIn) {
            // LOGIN EJECUTIVO ADAPTADO A LA PLANTILLA CERHAN (NEGRO MATE Y CREMA)
            SecurityVaultScreen(
                viewModel = viewModel,
                themeMode = themeMode,
                onToggleTheme = { viewModel.toggleTheme() },
                onLoginSuccess = { }
            )
        } else if (uiState.currentUserRole == "ADMIN") {
            // =========================================================================
            // CONSOLA DEL DUEÑO: MENÚ DESPLEGABLE AL COSTADO (SIN BOTONES ARRIBA)
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

            val ownerSidebarItems = remember(allProducts.size, pendingOrders.size) {
                listOf(
                    SgniaSidebarItem("home", "Inicio / Resumen", Icons.Default.Home),
                    SgniaSidebarItem("catalogo", "Catálogo de Cortes", Icons.Default.Inventory2, "${allProducts.size}"),
                    SgniaSidebarItem("unificacion", "Unificación Pedidos", Icons.Default.MergeType, if (pendingOrders.isNotEmpty()) "${pendingOrders.size}" else null),
                    SgniaSidebarItem("sucursales", "Sucursales y Tiendas", Icons.Default.Business),
                    SgniaSidebarItem("proveedores", "Proveedores", Icons.Default.LocalShipping),
                    SgniaSidebarItem("seguridad", "Seguridad y PIN", Icons.Default.Lock)
                )
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    SgniaCerhanSidebar(
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
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SgniaTheme.colors.backgroundPrimary)
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        SgniaCerhanTopBar(
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            searchPlaceholder = "Buscar en catálogo o sucursal...",
                            onSearchClick = { selectedOwnerSection = "catalogo" },
                            onToggleTheme = { viewModel.toggleTheme() }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedOwnerSection) {
                                "home" -> ExecutiveOwnerDashboardScreen(
                                    viewModel = viewModel,
                                    themeMode = themeMode,
                                    onToggleTheme = { viewModel.toggleTheme() },
                                    onLogout = { viewModel.logout() },
                                    onOpenAdminConsole = { selectedOwnerSection = "catalogo" },
                                    onNavigateToSection = { section -> selectedOwnerSection = section }
                                )
                                "catalogo" -> AdminCatalogScreen(viewModel = viewModel)
                                "unificacion" -> MasterOrderUnificationScreen(viewModel = viewModel)
                                "sucursales" -> AdminBranchesScreen(viewModel = viewModel)
                                "proveedores" -> AdminSuppliersScreen(viewModel = viewModel)
                                "seguridad" -> AdminSecurityScreen(viewModel = viewModel)
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

            val storeSidebarItems = remember(pendingOrders.size) {
                listOf(
                    SgniaSidebarItem("basculas", "Básculas (Principal)", Icons.Default.Scale),
                    SgniaSidebarItem("recepcion", "Recepción Mercancía", Icons.Default.AddBusiness),
                    SgniaSidebarItem("pedidos", "Pedidos de Surtido", Icons.AutoMirrored.Filled.ReceiptLong, if (pendingOrders.isNotEmpty()) "${pendingOrders.size}" else null),
                    SgniaSidebarItem("inversion", "Mi Inversión Local", Icons.Default.CardGiftcard),
                    SgniaSidebarItem("ajustes", "Ajustes de Sistema", Icons.Default.Settings)
                )
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    SgniaCerhanSidebar(
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
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SgniaTheme.colors.backgroundPrimary)
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        SgniaCerhanTopBar(
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            searchPlaceholder = "Buscar corte o código...",
                            onSearchClick = { selectedStoreSection = "basculas" },
                            onToggleTheme = { viewModel.toggleTheme() }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedStoreSection) {
                                "basculas" -> IndustrialScaleVisorScreen(viewModel = viewModel)
                                "recepcion" -> StockReceptionScreen(viewModel = viewModel)
                                "pedidos" -> OrdersScreen(viewModel = viewModel)
                                "inversion" -> ManagerInvestmentScreen(viewModel = viewModel)
                                "ajustes" -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
