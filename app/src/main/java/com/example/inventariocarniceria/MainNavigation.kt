package com.example.inventariocarniceria

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.NavigationBarItemDefaults
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.ui.screens.admin.AdminDashboardScreen
import com.example.inventariocarniceria.ui.screens.auth.SecurityVaultScreen
import com.example.inventariocarniceria.ui.screens.dashboard.ExecutiveOwnerDashboardScreen
import com.example.inventariocarniceria.ui.screens.manager.ManagerInvestmentScreen
import com.example.inventariocarniceria.ui.screens.orders.OrdersScreen
import com.example.inventariocarniceria.ui.screens.reception.StockReceptionScreen
import com.example.inventariocarniceria.ui.screens.scale.IndustrialScaleVisorScreen
import com.example.inventariocarniceria.ui.screens.settings.SettingsScreen
import com.example.inventariocarniceria.ui.screens.splash.SplashScreen
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@Composable
fun MainNavigation(viewModel: InventoryViewModel) {
    var showSplash by remember { mutableStateOf(true) }
    val uiState by viewModel.uiState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    SgniaTheme(themeMode = themeMode) {
        if (showSplash) {
            SplashScreen(
                onSplashFinished = { showSplash = false }
            )
        } else if (!uiState.isLoggedIn) {
            // BÓVEDA MILITAR DE ACCESO (PIN DUEÑO / TIENDA ENCARGADO)
            SecurityVaultScreen(
                viewModel = viewModel,
                themeMode = themeMode,
                onToggleTheme = { viewModel.toggleTheme() },
                onLoginSuccess = { }
            )
        } else if (uiState.currentUserRole == "ADMIN") {
            // CONSOLA EJECUTIVA DEL DUEÑO (VIP) CON SWITCH A GESTIÓN
            var showManagementConsole by remember { mutableStateOf(false) }

            if (showManagementConsole) {
                AdminDashboardScreen(
                    viewModel = viewModel,
                    onLogout = { viewModel.logout() },
                    onBackToVipConsole = { showManagementConsole = false }
                )
            } else {
                ExecutiveOwnerDashboardScreen(
                    viewModel = viewModel,
                    themeMode = themeMode,
                    onToggleTheme = { viewModel.toggleTheme() },
                    onLogout = { viewModel.logout() },
                    onOpenAdminConsole = { showManagementConsole = true }
                )
            }
        } else {
            // TERMINAL DE MOSTRADOR PARA ENCARGADO DE SUCURSAL
            var selectedTab by remember { mutableIntStateOf(0) }
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.snackbarMessage) {
                uiState.snackbarMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    viewModel.clearSnackbarMessage()
                }
            }

            Scaffold(
                containerColor = SgniaTheme.colors.backgroundPrimary,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    NavigationBar(
                        containerColor = SgniaTheme.colors.backgroundSecondary,
                        contentColor = SgniaTheme.colors.textPrimary,
                        modifier = Modifier.clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    ) {
                        val navColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF141210),
                            selectedTextColor = SgniaTheme.colors.accentPrimary,
                            indicatorColor = SgniaTheme.colors.accentPrimary,
                            unselectedIconColor = SgniaTheme.colors.textMuted,
                            unselectedTextColor = SgniaTheme.colors.textMuted
                        )
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.Default.Scale, contentDescription = "Báscula Industrial") },
                            label = { Text("Báscula", fontWeight = if (selectedTab == 0) androidx.compose.ui.text.font.FontWeight.Black else androidx.compose.ui.text.font.FontWeight.Normal) },
                            colors = navColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.AddBusiness, contentDescription = "Recepción de Stock") },
                            label = { Text("Recepción", fontWeight = if (selectedTab == 1) androidx.compose.ui.text.font.FontWeight.Black else androidx.compose.ui.text.font.FontWeight.Normal) },
                            colors = navColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Pedidos Faltantes") },
                            label = { Text("Pedidos", fontWeight = if (selectedTab == 2) androidx.compose.ui.text.font.FontWeight.Black else androidx.compose.ui.text.font.FontWeight.Normal) },
                            colors = navColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = { Icon(Icons.Default.CardGiftcard, contentDescription = "Mi Inversión") },
                            label = { Text("Mi Inversión", fontWeight = if (selectedTab == 3) androidx.compose.ui.text.font.FontWeight.Black else androidx.compose.ui.text.font.FontWeight.Normal) },
                            colors = navColors
                        )
                        NavigationBarItem(
                            selected = selectedTab == 4,
                            onClick = { selectedTab = 4 },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes Sistema") },
                            label = { Text("Ajustes", fontWeight = if (selectedTab == 4) androidx.compose.ui.text.font.FontWeight.Black else androidx.compose.ui.text.font.FontWeight.Normal) },
                            colors = navColors
                        )
                    }
                }
            ) { innerPadding ->
                when (selectedTab) {
                    0 -> IndustrialScaleVisorScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                    1 -> StockReceptionScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                    2 -> OrdersScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                    3 -> ManagerInvestmentScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                    4 -> SettingsScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
