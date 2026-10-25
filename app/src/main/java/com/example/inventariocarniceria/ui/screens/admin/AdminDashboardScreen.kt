package com.example.inventariocarniceria.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.ui.components.SgniaThemeToggle
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: InventoryViewModel,
    onLogout: () -> Unit,
    onBackToVipConsole: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var adminTab by remember { mutableIntStateOf(0) }
    val darkRed = Color(0xFFB71C1C)

    Column(modifier = modifier.fillMaxSize()) {
        // TOP APP BAR DEDICADA AL ADMIN
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CONSOLA DE ADMINISTRADOR",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                        Text(
                            text = "Carnicería SGNIA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }
            },
            actions = {
                SgniaThemeToggle(
                    onToggle = { viewModel.toggleTheme() }
                )
                Spacer(modifier = Modifier.width(4.dp))
                if (onBackToVipConsole != null) {
                    IconButton(onClick = onBackToVipConsole) {
                        Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Consola VIP", tint = Color(0xFFD4AF37))
                    }
                }
                IconButton(onClick = onLogout) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar Sesión", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = darkRed)
        )

        // TAB ROW EXCLUSIVO DE ADMIN
        ScrollableTabRow(selectedTabIndex = adminTab, edgePadding = 0.dp) {
            Tab(
                selected = adminTab == 0,
                onClick = { adminTab = 0 },
                text = { Text("⚡ Unificación Pedidos", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)) }
            )
            Tab(
                selected = adminTab == 1,
                onClick = { adminTab = 1 },
                text = { Text("📋 Catálogo Maestro", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = adminTab == 2,
                onClick = { adminTab = 2 },
                text = { Text("🏢 Sucursales & Usuarios", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = adminTab == 3,
                onClick = { adminTab = 3 },
                text = { Text("🚚 Proveedores", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = adminTab == 4,
                onClick = { adminTab = 4 },
                text = { Text("🔒 Seguridad PIN", fontWeight = FontWeight.Bold) }
            )
        }

        when (adminTab) {
            0 -> MasterOrderUnificationScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            1 -> AdminCatalogScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            2 -> AdminBranchesScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            3 -> AdminSuppliersScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            4 -> AdminSecurityScreen(viewModel = viewModel, modifier = Modifier.fillMaxSize())
        }
    }
}
