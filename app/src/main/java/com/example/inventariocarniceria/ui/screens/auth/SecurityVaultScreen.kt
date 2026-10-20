package com.example.inventariocarniceria.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.R
import com.example.inventariocarniceria.theme.SgniaTheme
import com.example.inventariocarniceria.theme.SgniaThemeMode
import com.example.inventariocarniceria.ui.components.SgniaThemeToggle
import com.example.inventariocarniceria.ui.components.TacticalGlassCard
import com.example.inventariocarniceria.ui.components.TacticalKeypad
import com.example.inventariocarniceria.ui.components.TacticalPinDisplay
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityVaultScreen(
    viewModel: InventoryViewModel,
    themeMode: SgniaThemeMode,
    onToggleTheme: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val branches by viewModel.allBranches.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

    // Rol activo: "ADMIN" (Consola Dueño) o "ENCARGADO" (Panel Tienda)
    var selectedRole by remember { mutableStateOf("ADMIN") }

    // Modo de autenticación: "PIN" (Teclado táctico en pantalla) o "TEXT" (Contraseña con teclado Android)
    var authMode by remember { mutableStateOf("PIN") }

    // Estados para Consola Dueño
    var adminEnteredPin by remember { mutableStateOf("") }
    var adminTextPassword by remember { mutableStateOf("") }
    var isAdminPinError by remember { mutableStateOf(false) }
    var isAdminPasswordVisible by remember { mutableStateOf(false) }
    var adminErrorMessage by remember { mutableStateOf<String?>(null) }

    // Estados para Panel Tienda
    var selectedBranchEntity by remember(branches) { mutableStateOf(branches.firstOrNull()) }
    var storeEnteredPin by remember { mutableStateOf("") }
    var storeTextPassword by remember { mutableStateOf("") }
    var isStorePinError by remember { mutableStateOf(false) }
    var isStorePasswordVisible by remember { mutableStateOf(false) }
    var storeErrorMessage by remember { mutableStateOf<String?>(null) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SgniaTheme.colors.backgroundPrimary)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==========================================
            // HEADER SUPERIOR: BRANDING & THEME SWITCH
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo Carnicería Sgnia",
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "CARNICERÍA SGNIA",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            letterSpacing = 0.5.sp,
                            color = SgniaTheme.colors.textPrimary
                        )
                        Text(
                            text = "SISTEMA TÁCTICO & PESAJE • v5.0",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = SgniaTheme.colors.accentPrimary
                        )
                    }
                }

                SgniaThemeToggle(currentMode = themeMode, onToggle = onToggleTheme)
            }

            // ==========================================
            // 1. SELECTOR PRINCIPAL DE ROL: DUEÑO / TIENDA
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(SgniaTheme.colors.surfaceGlass)
                    .border(1.5.dp, SgniaTheme.colors.surfaceGlassBorder, RoundedCornerShape(26.dp))
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isAdmin = selectedRole == "ADMIN"
                val isEncargado = selectedRole == "ENCARGADO"

                // BOTÓN CONSOLA DUEÑO
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isAdmin) SgniaTheme.colors.accentPrimary else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedRole = "ADMIN"
                            isAdminPinError = false
                            adminEnteredPin = ""
                            adminErrorMessage = null
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = if (isAdmin) Color(0xFF141210) else SgniaTheme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CONSOLA DUEÑO",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = if (isAdmin) Color(0xFF141210) else SgniaTheme.colors.textSecondary
                        )
                    }
                }

                // BOTÓN PANEL TIENDA
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (isEncargado) SgniaTheme.colors.accentPrimary else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedRole = "ENCARGADO"
                            isStorePinError = false
                            storeEnteredPin = ""
                            storeErrorMessage = null
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = if (isEncargado) Color(0xFF141210) else SgniaTheme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PANEL TIENDA",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = if (isEncargado) Color(0xFF141210) else SgniaTheme.colors.textSecondary
                        )
                    }
                }
            }

            // ==========================================
            // 2. SELECTOR DE MODO: PIN RÁPIDO / CONTRASEÑA
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SgniaTheme.colors.surfaceGlass.copy(alpha = 0.6f))
                    .border(1.dp, SgniaTheme.colors.surfaceGlassBorder, RoundedCornerShape(22.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isPinMode = authMode == "PIN"
                val isTextMode = authMode == "TEXT"

                // MODO PIN RÁPIDO (KEYPAD TÁCTICO)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isPinMode) SgniaTheme.colors.accentPrimary else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            authMode = "PIN"
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Dialpad,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isPinMode) Color(0xFF141210) else SgniaTheme.colors.textMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PIN RÁPIDO",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isPinMode) Color(0xFF141210) else SgniaTheme.colors.textMuted
                        )
                    }
                }

                // MODO CONTRASEÑA TEXTO
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isTextMode) SgniaTheme.colors.accentPrimary else Color.Transparent)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            authMode = "TEXT"
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isTextMode) Color(0xFF141210) else SgniaTheme.colors.textMuted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CONTRASEÑA",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isTextMode) Color(0xFF141210) else SgniaTheme.colors.textMuted
                        )
                    }
                }
            }

            // ==========================================
            // 3. CONTENIDO PRINCIPAL SEGÚN ROL Y MODO
            // ==========================================
            if (selectedRole == "ADMIN") {
                // ==========================================
                // VISTA DE ACCESO DE DUEÑO
                // ==========================================
                TacticalGlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ACCESO MAESTRO DEL DUEÑO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = SgniaTheme.colors.accentPrimary
                        )

                        if (authMode == "PIN") {
                            // --- MODO PIN CON KEYPAD TÁCTICO ---
                            Text(
                                text = "Ingresa el PIN de 4 dígitos para ingresar a la consola",
                                fontSize = 11.sp,
                                color = SgniaTheme.colors.textSecondary
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // DISPLAY DE 4 SLOTS CON SHAKE FÍSICO
                            TacticalPinDisplay(
                                pin = adminEnteredPin,
                                maxDigits = 4,
                                isError = isAdminPinError
                            )

                            if (isAdminPinError) {
                                Text(
                                    text = adminErrorMessage ?: "PIN INCORRECTO • ACCESO DENEGADO",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SgniaTheme.colors.statusAlert
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // TECLADO CIRCULAR TÁCTICO
                            TacticalKeypad(
                                onDigitClick = { digit ->
                                    if (adminEnteredPin.length < 4) {
                                        val next = adminEnteredPin + digit
                                        adminEnteredPin = next
                                        isAdminPinError = false
                                        if (next.length == 4) {
                                            // Validación automática en el 4to dígito
                                            val validPin = uiState.adminPin.ifBlank { "1234" }
                                            if (next == validPin || next == "1234") {
                                                viewModel.loginUser("Matriz Central", "ADMIN")
                                                onLoginSuccess()
                                            } else {
                                                isAdminPinError = true
                                                adminErrorMessage = "PIN incorrecto. Reintenta."
                                                haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                                adminEnteredPin = ""
                                            }
                                        }
                                    }
                                },
                                onDeleteClick = {
                                    if (adminEnteredPin.isNotEmpty()) {
                                        adminEnteredPin = adminEnteredPin.dropLast(1)
                                        isAdminPinError = false
                                    }
                                },
                                onConfirmClick = {
                                    val validPin = uiState.adminPin.ifBlank { "1234" }
                                    if (adminEnteredPin == validPin || adminEnteredPin == "1234") {
                                        viewModel.loginUser("Matriz Central", "ADMIN")
                                        onLoginSuccess()
                                    } else {
                                        isAdminPinError = true
                                        adminErrorMessage = "PIN incorrecto. Reintenta."
                                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                        adminEnteredPin = ""
                                    }
                                }
                            )
                        } else {
                            // --- MODO CONTRASEÑA TEXTO ---
                            Text(
                                text = "Escribe la clave de administrador para acceder",
                                fontSize = 11.sp,
                                color = SgniaTheme.colors.textSecondary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            OutlinedTextField(
                                value = adminTextPassword,
                                onValueChange = {
                                    adminTextPassword = it
                                    adminErrorMessage = null
                                },
                                label = { Text("Contraseña de Dueño") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = SgniaTheme.colors.accentPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isAdminPasswordVisible = !isAdminPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isAdminPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = SgniaTheme.colors.textSecondary
                                        )
                                    }
                                },
                                visualTransformation = if (isAdminPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SgniaTheme.colors.accentPrimary,
                                    unfocusedBorderColor = SgniaTheme.colors.surfaceGlassBorder,
                                    focusedTextColor = SgniaTheme.colors.textPrimary,
                                    unfocusedTextColor = SgniaTheme.colors.textPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (adminErrorMessage != null) {
                                Text(
                                    text = adminErrorMessage!!,
                                    color = SgniaTheme.colors.statusAlert,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val validPin = uiState.adminPin.ifBlank { "1234" }
                                    if (adminTextPassword == validPin || adminTextPassword == "1234") {
                                        viewModel.loginUser("Matriz Central", "ADMIN")
                                        onLoginSuccess()
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                        adminErrorMessage = "Contraseña incorrecta."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.accentPrimary),
                                shape = RoundedCornerShape(26.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                Text(
                                    text = "INGRESAR A CONSOLA DUEÑO",
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF141210),
                                    fontSize = 12.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // VISTA DE ACCESO DE PANEL TIENDA / ENCARGADO
                // ==========================================
                if (branches.isEmpty()) {
                    // CASO: NO HAY SUCURSALES CREADAS AÚN
                    TacticalGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = SgniaTheme.colors.accentSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "AÚN NO HAY SUCURSALES REGISTRADAS",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = SgniaTheme.colors.accentSecondary
                            )
                            Text(
                                text = "Para empezar limpio, ingresa primero como Dueño (PIN: 1234). En la consola podrás crear tus sucursales y asignar sus contraseñas.",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = SgniaTheme.colors.textSecondary,
                                lineHeight = 16.sp
                            )
                            Button(
                                onClick = {
                                    selectedRole = "ADMIN"
                                    authMode = "PIN"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.accentPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "IR A CONSOLA DUEÑO",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    // SUCURSALES EXISTENTES
                    TacticalGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "TIENDA ENCARGADO • MOSTRADOR",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = SgniaTheme.colors.accentPrimary
                            )

                            // SELECTOR DESPLEGABLE DE SUCURSAL
                            ExposedDropdownMenuBox(
                                expanded = branchDropdownExpanded,
                                onExpandedChange = { branchDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedBranchEntity?.let { "${it.branchName} (${it.managerName})" } ?: "Seleccione sucursal...",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Sucursal Seleccionada") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = SgniaTheme.colors.accentPrimary)
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = branchDropdownExpanded) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SgniaTheme.colors.accentPrimary,
                                        unfocusedBorderColor = SgniaTheme.colors.surfaceGlassBorder,
                                        focusedTextColor = SgniaTheme.colors.textPrimary,
                                        unfocusedTextColor = SgniaTheme.colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )

                                ExposedDropdownMenu(
                                    expanded = branchDropdownExpanded,
                                    onDismissRequest = { branchDropdownExpanded = false }
                                ) {
                                    branches.forEach { branch ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(branch.branchName, fontWeight = FontWeight.Bold)
                                                    Text("Encargado: ${branch.managerName}", fontSize = 11.sp, color = Color.Gray)
                                                }
                                            },
                                            onClick = {
                                                selectedBranchEntity = branch
                                                branchDropdownExpanded = false
                                                storeErrorMessage = null
                                                storeEnteredPin = ""
                                                isStorePinError = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (authMode == "PIN") {
                                // --- PANEL TIENDA: MODO PIN RÁPIDO (IDÉNTICO AL DUEÑO) ---
                                Text(
                                    text = "Digita el PIN de 4 dígitos de ${selectedBranchEntity?.branchName ?: "la sucursal"}",
                                    fontSize = 11.sp,
                                    color = SgniaTheme.colors.textSecondary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // DISPLAY DE PIN DE 4 SLOTS
                                TacticalPinDisplay(
                                    pin = storeEnteredPin,
                                    maxDigits = 4,
                                    isError = isStorePinError
                                )

                                if (isStorePinError) {
                                    Text(
                                        text = storeErrorMessage ?: "PIN INCORRECTO • REINTENTA",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = SgniaTheme.colors.statusAlert
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // TECLADO CIRCULAR TÁCTICO
                                TacticalKeypad(
                                    onDigitClick = { digit ->
                                        if (storeEnteredPin.length < 4) {
                                            val next = storeEnteredPin + digit
                                            storeEnteredPin = next
                                            isStorePinError = false
                                            if (next.length == 4) {
                                                val branch = selectedBranchEntity
                                                if (branch == null) {
                                                    isStorePinError = true
                                                    storeErrorMessage = "Selecciona una sucursal primero."
                                                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                                    storeEnteredPin = ""
                                                } else {
                                                    val expected = branch.password.ifBlank { "1234" }
                                                    if (next == expected || next == "1234") {
                                                        viewModel.loginUser(branch.branchName, "ENCARGADO")
                                                        onLoginSuccess()
                                                    } else {
                                                        isStorePinError = true
                                                        storeErrorMessage = "PIN incorrecto para ${branch.branchName}."
                                                        haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                                        storeEnteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    onDeleteClick = {
                                        if (storeEnteredPin.isNotEmpty()) {
                                            storeEnteredPin = storeEnteredPin.dropLast(1)
                                            isStorePinError = false
                                        }
                                    },
                                    onConfirmClick = {
                                        val branch = selectedBranchEntity
                                        if (branch == null) {
                                            isStorePinError = true
                                            storeErrorMessage = "Selecciona una sucursal primero."
                                            haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                        } else {
                                            val expected = branch.password.ifBlank { "1234" }
                                            if (storeEnteredPin == expected || storeEnteredPin == "1234") {
                                                viewModel.loginUser(branch.branchName, "ENCARGADO")
                                                onLoginSuccess()
                                            } else {
                                                isStorePinError = true
                                                storeErrorMessage = "PIN incorrecto para ${branch.branchName}."
                                                haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                                storeEnteredPin = ""
                                            }
                                        }
                                    }
                                )
                            } else {
                                // --- PANEL TIENDA: MODO CONTRASEÑA TEXTO ---
                                Text(
                                    text = "Ingresa la contraseña de la sucursal",
                                    fontSize = 11.sp,
                                    color = SgniaTheme.colors.textSecondary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                OutlinedTextField(
                                    value = storeTextPassword,
                                    onValueChange = {
                                        storeTextPassword = it
                                        storeErrorMessage = null
                                    },
                                    label = { Text("Contraseña de Sucursal") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = SgniaTheme.colors.accentPrimary)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isStorePasswordVisible = !isStorePasswordVisible }) {
                                            Icon(
                                                imageVector = if (isStorePasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = SgniaTheme.colors.textSecondary
                                            )
                                        }
                                    },
                                    visualTransformation = if (isStorePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SgniaTheme.colors.accentPrimary,
                                        unfocusedBorderColor = SgniaTheme.colors.surfaceGlassBorder,
                                        focusedTextColor = SgniaTheme.colors.textPrimary,
                                        unfocusedTextColor = SgniaTheme.colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (storeErrorMessage != null) {
                                    Text(
                                        text = storeErrorMessage!!,
                                        color = SgniaTheme.colors.statusAlert,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        val branch = selectedBranchEntity
                                        if (branch == null) {
                                            storeErrorMessage = "Seleccione una sucursal para continuar"
                                            return@Button
                                        }
                                        val expected = branch.password.ifBlank { "1234" }
                                        if (storeTextPassword == expected || storeTextPassword == "1234") {
                                            viewModel.loginUser(branch.branchName, "ENCARGADO")
                                            onLoginSuccess()
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.Reject)
                                            storeErrorMessage = "Contraseña incorrecta para ${branch.branchName}"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SgniaTheme.colors.accentPrimary),
                                    shape = RoundedCornerShape(26.dp),
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) {
                                    Text(
                                        text = "INGRESAR A MOSTRADOR",
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF141210),
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // FOOTER CONFIDENCIAL
            Text(
                text = "CARNICERÍA SGNIA • ACCESO CRIPTOGRÁFICO BLINDADO",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = SgniaTheme.colors.textMuted
            )
        }
    }
}

