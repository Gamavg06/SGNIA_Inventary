package com.example.inventariocarniceria.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.R
import com.example.inventariocarniceria.data.local.BranchEntity
import com.example.inventariocarniceria.theme.SgniaThemeMode
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.BigMetric
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirKeypad
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirPinDots
import com.example.inventariocarniceria.ui.design.NoirPrimaryButton
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSegmented
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import com.example.inventariocarniceria.ui.viewmodel.InventoryViewModel

/**
 * Login SGNIA · rediseño "Instrumento de Precisión".
 *
 * La lógica de autenticación es idéntica a la versión anterior:
 * - Rol ADMIN por defecto, PIN de 4 dígitos (uiState.adminPin o "1234") o contraseña.
 * - Rol ENCARGADO: selección de sucursal + PIN/contraseña de esa sucursal (o "1234").
 * - Validación automática al completar 4 dígitos y con la tecla confirmar.
 */
@Composable
fun SecurityVaultScreen(
    viewModel: InventoryViewModel,
    themeMode: SgniaThemeMode,
    onToggleTheme: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val branches by viewModel.allBranches.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val isOwnerApp = context.packageName.endsWith(".admin")

    var selectedRole by remember { mutableStateOf(if (isOwnerApp) "ADMIN" else "ENCARGADO") }
    var authMode by remember { mutableStateOf("PIN") }

    // Estado Consola Dueño
    var adminEnteredPin by remember { mutableStateOf("") }
    var adminTextPassword by remember { mutableStateOf("") }
    var isAdminPinError by remember { mutableStateOf(false) }
    var isAdminPasswordVisible by remember { mutableStateOf(false) }
    var adminErrorMessage by remember { mutableStateOf<String?>(null) }

    // Estado Panel Tienda
    var selectedBranchEntity by remember(branches) { mutableStateOf(branches.firstOrNull()) }
    var storeEnteredPin by remember { mutableStateOf("") }
    var storeTextPassword by remember { mutableStateOf("") }
    var isStorePinError by remember { mutableStateOf(false) }
    var isStorePasswordVisible by remember { mutableStateOf(false) }
    var storeErrorMessage by remember { mutableStateOf<String?>(null) }
    var branchDropdownExpanded by remember { mutableStateOf(false) }

    // Estado Vinculación de Sucursal (para tablet POS independiente)
    val clipboardManager = LocalClipboardManager.current
    var isManualLinkDialogOpen by remember { mutableStateOf(false) }
    var linkQuickCode by remember { mutableStateOf("") }
    var linkBranchName by remember { mutableStateOf("") }
    var linkManagerName by remember { mutableStateOf("") }
    var linkPin by remember { mutableStateOf("") }
    var linkErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(branches) {
        if (selectedBranchEntity == null && branches.isNotEmpty()) {
            selectedBranchEntity = branches.first()
        }
    }

    // Estado Throttling y Bloqueo de PIN
    var failedAttempts by remember { mutableStateOf(0) }
    var lockoutUntil by remember { mutableStateOf(0L) }
    var lockoutSecondsLeft by remember { mutableStateOf(0) }

    LaunchedEffect(lockoutUntil) {
        while (System.currentTimeMillis() < lockoutUntil) {
            lockoutSecondsLeft = (((lockoutUntil - System.currentTimeMillis()) / 1000) + 1).toInt().coerceAtLeast(1)
            delay(500L)
        }
        lockoutSecondsLeft = 0
    }

    // Estado Cambio Obligatorio de PIN por Defecto
    var showForceChangePinDialog by remember { mutableStateOf(false) }
    var pendingRole by remember { mutableStateOf("") }
    var pendingBranchName by remember { mutableStateOf("") }
    var pendingBranchEntity by remember { mutableStateOf<com.example.inventariocarniceria.data.local.BranchEntity?>(null) }
    var newMandatoryPin by remember { mutableStateOf("") }
    var confirmMandatoryPin by remember { mutableStateOf("") }
    var mandatoryPinError by remember { mutableStateOf<String?>(null) }

    // ---------------------------------------------------------------
    // Validaciones con Protección contra Fuerza Bruta y Cambio Obligatorio de PIN
    // ---------------------------------------------------------------
    fun tryAdmin(input: String, fromPin: Boolean) {
        if (lockoutSecondsLeft > 0) {
            adminErrorMessage = "Terminal bloqueada por seguridad. Espera $lockoutSecondsLeft s."
            return
        }
        val validPin = uiState.adminPin.ifBlank { "1234" }
        if (input == validPin || input == "1234") {
            val isDefault = (input == "1234" || validPin == "1234")
            if (isDefault) {
                pendingRole = "ADMIN"
                pendingBranchName = "Matriz Central"
                newMandatoryPin = ""
                confirmMandatoryPin = ""
                mandatoryPinError = null
                showForceChangePinDialog = true
            } else {
                failedAttempts = 0
                viewModel.loginUser("Matriz Central", "ADMIN")
                onLoginSuccess()
            }
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            failedAttempts++
            if (failedAttempts >= 3) {
                lockoutUntil = System.currentTimeMillis() + 30_000L
                failedAttempts = 0
                adminErrorMessage = "Demasiados intentos fallidos. Terminal bloqueada por 30s."
            } else {
                val remaining = 3 - failedAttempts
                adminErrorMessage = "PIN incorrecto. $remaining intento(s) restante(s) antes del bloqueo temporal."
            }
            if (fromPin) {
                isAdminPinError = true
                adminEnteredPin = ""
            }
        }
    }

    fun tryStore(input: String, fromPin: Boolean) {
        if (lockoutSecondsLeft > 0) {
            storeErrorMessage = "Terminal bloqueada por seguridad. Espera $lockoutSecondsLeft s."
            return
        }
        val branch = selectedBranchEntity
        if (branch == null) {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            if (fromPin) {
                isStorePinError = true
                storeErrorMessage = "Selecciona una sucursal primero."
                storeEnteredPin = ""
            } else {
                storeErrorMessage = "Seleccione una sucursal para continuar"
            }
            return
        }
        val expected = branch.password.ifBlank { "1234" }
        if (input == expected || input == "1234") {
            val isDefault = (input == "1234" || expected == "1234")
            if (isDefault) {
                pendingRole = "ENCARGADO"
                pendingBranchName = branch.branchName
                pendingBranchEntity = branch
                newMandatoryPin = ""
                confirmMandatoryPin = ""
                mandatoryPinError = null
                showForceChangePinDialog = true
            } else {
                failedAttempts = 0
                viewModel.loginUser(branch.branchName, "ENCARGADO")
                onLoginSuccess()
            }
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            failedAttempts++
            if (failedAttempts >= 3) {
                lockoutUntil = System.currentTimeMillis() + 30_000L
                failedAttempts = 0
                storeErrorMessage = "Demasiados intentos fallidos. Terminal bloqueada por 30s."
            } else {
                val remaining = 3 - failedAttempts
                storeErrorMessage = "PIN incorrecto para ${branch.branchName}. $remaining intento(s) antes del bloqueo."
            }
            if (fromPin) {
                isStorePinError = true
                storeEnteredPin = ""
            }
        }
    }

    val isAdmin = isOwnerApp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NoirSpace.screen, vertical = NoirSpace.md),
            verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
        ) {
            // =======================================================
            // 1. PLACA DE MARCA
            // =======================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NoirColors.Surface)
                        .border(1.dp, NoirColors.Hairline, RoundedCornerShape(14.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.sglogo),
                        contentDescription = "Logo SGNIA"
                    )
                }
                Spacer(Modifier.width(NoirSpace.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SGNIA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        color = NoirColors.TextPrimary
                    )
                    NoirLabel("Carnicería · Balanza industrial", color = NoirColors.TextMuted)
                }
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, NoirColors.Hairline, RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        Icons.Default.Contrast,
                        contentDescription = "Cambiar tema (${themeMode.name})",
                        tint = NoirColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(NoirSpace.sm))

            // =======================================================
            // 2. TITULAR
            // =======================================================
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Identifícate", style = NoirType.display, color = NoirColors.TextPrimary)
                Text(
                    text = "Elige tu perfil y escribe tu PIN de acceso.",
                    style = NoirType.body,
                    color = NoirColors.TextSecondary
                )
            }

            Spacer(Modifier.height(NoirSpace.xxs))

            // =======================================================
            // 3. SELECCIÓN DE PERFIL (dos celdas bento)
            // =======================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
            ) {
                if (isOwnerApp) {
                    RoleTile(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Consola Ejecutiva del Dueño",
                        caption = "Unificación multi-tienda, sucursales y pedidos a rastro",
                        icon = Icons.Default.AdminPanelSettings,
                        selected = true,
                        onClick = { }
                    )
                } else {
                    RoleTile(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Mostrador de Tienda / Carnicería",
                        caption = "2 Básculas Systel en vivo, catálogo de cortes y despiece",
                        icon = Icons.Default.Storefront,
                        selected = true,
                        onClick = { }
                    )
                }
            }

            // =======================================================
            // 4. CREDENCIALES
            // =======================================================
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(NoirSpace.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        NoirLabel(if (isAdmin) "Acceso maestro" else "Ingreso a sucursal")
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isAdmin) "Consola del Dueño" else "Panel de Tienda",
                            style = NoirType.title,
                            color = NoirColors.TextPrimary
                        )
                    }
                    NoirSegmented(
                        options = listOf("PIN", "Clave"),
                        selectedIndex = if (authMode == "PIN") 0 else 1,
                        onSelect = { authMode = if (it == 0) "PIN" else "TEXT" }
                    )
                }

                if (lockoutSecondsLeft > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(NoirRadius.sm))
                            .background(NoirColors.DangerMuted)
                            .border(1.dp, NoirColors.Danger, RoundedCornerShape(NoirRadius.sm))
                            .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏳ Terminal bloqueada por seguridad. Reintenta en $lockoutSecondsLeft s.",
                            style = NoirType.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = NoirColors.Danger
                        )
                    }
                }

                if (isAdmin) {
                    // ---------------- DUEÑO ----------------
                    if (authMode == "PIN") {
                        PinBlock(
                            hint = "PIN de 4 dígitos",
                            pin = adminEnteredPin,
                            isError = isAdminPinError,
                            errorMessage = adminErrorMessage ?: "PIN incorrecto. Reintenta.",
                            onDigit = { digit ->
                                if (lockoutSecondsLeft <= 0 && adminEnteredPin.length < 4) {
                                    val next = adminEnteredPin + digit
                                    adminEnteredPin = next
                                    isAdminPinError = false
                                    if (next.length == 4) tryAdmin(next, fromPin = true)
                                }
                            },
                            onDelete = {
                                if (adminEnteredPin.isNotEmpty()) {
                                    adminEnteredPin = adminEnteredPin.dropLast(1)
                                    isAdminPinError = false
                                }
                            },
                            onConfirm = { tryAdmin(adminEnteredPin, fromPin = true) }
                        )
                    } else {
                        PasswordBlock(
                            label = "Contraseña de Dueño",
                            value = adminTextPassword,
                            onValueChange = {
                                adminTextPassword = it
                                adminErrorMessage = null
                            },
                            visible = isAdminPasswordVisible,
                            onToggleVisible = { isAdminPasswordVisible = !isAdminPasswordVisible },
                            errorMessage = adminErrorMessage,
                            buttonText = "Entrar a la consola",
                            onSubmit = { tryAdmin(adminTextPassword, fromPin = false) }
                        )
                    }
                } else {
                    // ---------------- TIENDA ----------------
                    if (branches.isEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NoirLabel("CONFIGURACIÓN DE TERMINAL", color = NoirColors.Cream)
                                StatusPill("NUEVA TERMINAL", tone = PillTone.Pending)
                            }

                            Text(
                                text = "Vincular Sucursal a esta Terminal",
                                style = NoirType.title,
                                color = NoirColors.TextPrimary
                            )
                            Text(
                                text = "Esta terminal de mostrador está activada. Asigna el nombre de la sucursal y el PIN de 4 dígitos creado por el dueño para habilitar las básculas y el mostrador de cobro.",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextSecondary
                            )

                            // 1. Vinculación Rápida con Código / Portapapeles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = linkQuickCode,
                                    onValueChange = {
                                        linkQuickCode = it
                                        linkErrorMessage = null
                                    },
                                    placeholder = { Text("Pega código del dueño...", color = NoirColors.TextMuted, fontSize = 12.sp) },
                                    label = { Text("Código de Vinculación", color = NoirColors.TextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(NoirRadius.sm),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NoirColors.Cream,
                                        unfocusedBorderColor = NoirColors.Hairline,
                                        focusedContainerColor = NoirColors.SurfaceSunken,
                                        unfocusedContainerColor = NoirColors.SurfaceSunken,
                                        focusedTextColor = NoirColors.TextPrimary,
                                        unfocusedTextColor = NoirColors.TextPrimary
                                    ),
                                    singleLine = true
                                )

                                Button(
                                    onClick = {
                                        val clipText = clipboardManager.getText()?.text ?: ""
                                        val rawToParse = if (linkQuickCode.isNotBlank()) linkQuickCode else clipText
                                        if (rawToParse.isNotBlank()) {
                                            val parsed = parseBranchQuickCode(rawToParse)
                                            if (parsed != null) {
                                                linkBranchName = parsed.branchName
                                                linkManagerName = parsed.managerName
                                                linkPin = parsed.pin
                                                if (parsed.ownerHwId.isNotBlank()) {
                                                    com.example.inventariocarniceria.data.sync.SupabaseSyncManager.setLinkedOwner(context, parsed.ownerHwId)
                                                }
                                                linkQuickCode = ""
                                                linkErrorMessage = null
                                                Toast.makeText(context, "Sucursal '${parsed.branchName}' vinculada a tu cadena.", Toast.LENGTH_SHORT).show()
                                            } else {
                                                linkErrorMessage = "Formato no válido. Escribe los datos manualmente abajo."
                                            }
                                        } else {
                                            linkErrorMessage = "El portapapeles está vacío."
                                        }
                                    },
                                    shape = RoundedCornerShape(NoirRadius.sm),
                                    colors = ButtonDefaults.buttonColors(containerColor = NoirColors.SurfaceRaised, contentColor = NoirColors.Cream),
                                    border = BorderStroke(1.dp, NoirColors.HairlineStrong),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = "Pegar", tint = NoirColors.Cream, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Pegar", style = NoirType.label, color = NoirColors.Cream)
                                }
                            }

                            // 2. Formulario Manual
                            OutlinedTextField(
                                value = linkBranchName,
                                onValueChange = {
                                    linkBranchName = it
                                    linkErrorMessage = null
                                },
                                label = { Text("Nombre de la Sucursal (ej. Sucursal Norte)", color = NoirColors.TextSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(NoirRadius.sm),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NoirColors.Cream,
                                    unfocusedBorderColor = NoirColors.Hairline,
                                    focusedContainerColor = NoirColors.SurfaceSunken,
                                    unfocusedContainerColor = NoirColors.SurfaceSunken,
                                    focusedTextColor = NoirColors.TextPrimary,
                                    unfocusedTextColor = NoirColors.TextPrimary
                                ),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                            ) {
                                OutlinedTextField(
                                    value = linkManagerName,
                                    onValueChange = { linkManagerName = it },
                                    label = { Text("Encargado (ej. Pedro)", color = NoirColors.TextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(NoirRadius.sm),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NoirColors.Cream,
                                        unfocusedBorderColor = NoirColors.Hairline,
                                        focusedContainerColor = NoirColors.SurfaceSunken,
                                        unfocusedContainerColor = NoirColors.SurfaceSunken,
                                        focusedTextColor = NoirColors.TextPrimary,
                                        unfocusedTextColor = NoirColors.TextPrimary
                                    ),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = linkPin,
                                    onValueChange = {
                                        if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                            linkPin = it
                                            linkErrorMessage = null
                                        }
                                    },
                                    label = { Text("PIN Mostrador (4 d.)", color = NoirColors.TextSecondary) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    shape = RoundedCornerShape(NoirRadius.sm),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NoirColors.Cream,
                                        unfocusedBorderColor = NoirColors.Hairline,
                                        focusedContainerColor = NoirColors.SurfaceSunken,
                                        unfocusedContainerColor = NoirColors.SurfaceSunken,
                                        focusedTextColor = NoirColors.TextPrimary,
                                        unfocusedTextColor = NoirColors.TextPrimary
                                    ),
                                    singleLine = true
                                )
                            }

                            if (linkErrorMessage != null) {
                                Text("⚠️ $linkErrorMessage", style = NoirType.bodySmall, color = NoirColors.Danger, fontWeight = FontWeight.Bold)
                            }

                            Spacer(Modifier.height(NoirSpace.xs))

                            NoirPrimaryButton(
                                text = "ACTIVAR SUCURSAL Y ABRIR MOSTRADOR",
                                icon = Icons.Default.CheckCircle,
                                enabled = linkBranchName.isNotBlank() && linkPin.length >= 4,
                                onClick = {
                                    val bName = linkBranchName.trim()
                                    val mName = linkManagerName.trim().ifBlank { "Encargado de Turno" }
                                    val pin = linkPin.trim()
                                    viewModel.addBranch(bName, mName, "", pin)
                                    viewModel.loginUser(bName, "ENCARGADO")
                                    onLoginSuccess()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        BranchSelector(
                            branches = branches,
                            selected = selectedBranchEntity,
                            expanded = branchDropdownExpanded,
                            onExpandedChange = { branchDropdownExpanded = it },
                            onSelect = { branch ->
                                selectedBranchEntity = branch
                                branchDropdownExpanded = false
                                storeErrorMessage = null
                                storeEnteredPin = ""
                                isStorePinError = false
                            }
                        )

                        if (authMode == "PIN") {
                            PinBlock(
                                hint = "PIN de ${selectedBranchEntity?.branchName ?: "la sucursal"}",
                                pin = storeEnteredPin,
                                isError = isStorePinError,
                                errorMessage = storeErrorMessage ?: "PIN incorrecto. Reintenta.",
                                onDigit = { digit ->
                                    if (lockoutSecondsLeft <= 0 && storeEnteredPin.length < 4) {
                                        val next = storeEnteredPin + digit
                                        storeEnteredPin = next
                                        isStorePinError = false
                                        if (next.length == 4) tryStore(next, fromPin = true)
                                    }
                                },
                                onDelete = {
                                    if (storeEnteredPin.isNotEmpty()) {
                                        storeEnteredPin = storeEnteredPin.dropLast(1)
                                        isStorePinError = false
                                    }
                                },
                                onConfirm = { tryStore(storeEnteredPin, fromPin = true) }
                            )
                        } else {
                            PasswordBlock(
                                label = "Contraseña de sucursal",
                                value = storeTextPassword,
                                onValueChange = {
                                    storeTextPassword = it
                                    storeErrorMessage = null
                                },
                                visible = isStorePasswordVisible,
                                onToggleVisible = { isStorePasswordVisible = !isStorePasswordVisible },
                                errorMessage = storeErrorMessage,
                                buttonText = "Entrar a básculas",
                                onSubmit = { tryStore(storeTextPassword, fromPin = false) }
                            )
                        }

                        // Opción para vincular otra sucursal si se desea
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = {
                                    linkBranchName = ""
                                    linkManagerName = ""
                                    linkPin = ""
                                    linkQuickCode = ""
                                    linkErrorMessage = null
                                    isManualLinkDialogOpen = true
                                }
                            ) {
                                Icon(Icons.Default.AddBusiness, contentDescription = null, tint = NoirColors.Cream, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Vincular otra sucursal a este equipo", style = NoirType.label, color = NoirColors.Cream)
                            }
                        }
                    }
                }
            }

            // =======================================================
            // 5. ESTADO DEL SISTEMA (dos métricas)
            // =======================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
            ) {
                BentoCard(modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(NoirSpace.md)) {
                    BigMetric(
                        value = "${allProducts.size}",
                        unit = "PLU",
                        label = "Catálogo local",
                        style = NoirType.metricM
                    )
                }
                BentoCard(modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(NoirSpace.md)) {
                    BigMetric(
                        value = "${branches.size}",
                        unit = if (branches.size == 1) "tienda" else "tiendas",
                        label = "Sucursales",
                        style = NoirType.metricM
                    )
                }
            }

            Text(
                text = "Básculas Wi-Fi 1 y 2 · Operación sin internet",
                style = NoirType.bodySmall,
                color = NoirColors.TextMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = NoirSpace.xxs, bottom = NoirSpace.md),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        if (showForceChangePinDialog) {
            AlertDialog(
                onDismissRequest = { /* No se permite cerrar sin establecer PIN seguro */ },
                containerColor = NoirColors.Surface,
                shape = RoundedCornerShape(NoirRadius.lg),
                title = {
                    Text(
                        "CAMBIO OBLIGATORIO DE PIN",
                        style = NoirType.title,
                        color = NoirColors.Amber
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Por política de seguridad estricta, la terminal no puede operar con el PIN por defecto ('1234'). Establece un nuevo PIN numérico personal (4 a 8 dígitos) para $pendingBranchName ($pendingRole).",
                            style = NoirType.bodySmall,
                            color = NoirColors.TextSecondary
                        )

                        OutlinedTextField(
                            value = newMandatoryPin,
                            onValueChange = {
                                if (it.length <= 8) newMandatoryPin = it.filter { c -> c.isDigit() }
                                mandatoryPinError = null
                            },
                            label = { Text("Nuevo PIN") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                            colors = noirFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = confirmMandatoryPin,
                            onValueChange = {
                                if (it.length <= 8) confirmMandatoryPin = it.filter { c -> c.isDigit() }
                                mandatoryPinError = null
                            },
                            label = { Text("Confirmar Nuevo PIN") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                            colors = noirFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        mandatoryPinError?.let { err ->
                            Text(
                                text = "⚠️ $err",
                                color = NoirColors.Danger,
                                style = NoirType.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newMandatoryPin == "1234") {
                                mandatoryPinError = "El nuevo PIN no puede ser el PIN por defecto '1234'."
                                return@Button
                            }
                            if (newMandatoryPin.length < 4) {
                                mandatoryPinError = "El PIN debe tener al menos 4 dígitos numéricos."
                                return@Button
                            }
                            if (newMandatoryPin != confirmMandatoryPin) {
                                mandatoryPinError = "Los PINs ingresados no coinciden."
                                return@Button
                            }

                            if (pendingRole == "ADMIN") {
                                viewModel.updateAdminPin(newMandatoryPin.trim())
                            } else if (pendingRole == "ENCARGADO") {
                                pendingBranchEntity?.let { branch ->
                                    viewModel.updateBranch(branch.copy(password = newMandatoryPin.trim()))
                                }
                            }

                            showForceChangePinDialog = false
                            viewModel.loginUser(pendingBranchName, pendingRole)
                            onLoginSuccess()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoirColors.Cream,
                            contentColor = NoirColors.OnCream
                        ),
                        shape = RoundedCornerShape(NoirRadius.sm),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("ESTABLECER PIN Y ENTRAR", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (isManualLinkDialogOpen) {
            AlertDialog(
                onDismissRequest = { isManualLinkDialogOpen = false },
                containerColor = NoirColors.Surface,
                shape = RoundedCornerShape(NoirRadius.lg),
                title = { Text("VINCULAR OTRA SUCURSAL", style = NoirType.title, color = NoirColors.TextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Pega el código generado por el Dueño o ingresa el nombre de la sucursal y su PIN de mostrador.",
                            style = NoirType.bodySmall,
                            color = NoirColors.TextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = linkQuickCode,
                                onValueChange = {
                                    linkQuickCode = it
                                    linkErrorMessage = null
                                },
                                placeholder = { Text("Código de sucursal...", color = NoirColors.TextMuted, fontSize = 12.sp) },
                                label = { Text("Código de Vinculación", color = NoirColors.TextSecondary) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(NoirRadius.sm),
                                colors = noirFieldColors(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    val clipText = clipboardManager.getText()?.text ?: ""
                                    val rawToParse = if (linkQuickCode.isNotBlank()) linkQuickCode else clipText
                                    if (rawToParse.isNotBlank()) {
                                        val parsed = parseBranchQuickCode(rawToParse)
                                        if (parsed != null) {
                                            linkBranchName = parsed.branchName
                                            linkManagerName = parsed.managerName
                                            linkPin = parsed.pin
                                            if (parsed.ownerHwId.isNotBlank()) {
                                                com.example.inventariocarniceria.data.sync.SupabaseSyncManager.setLinkedOwner(context, parsed.ownerHwId)
                                            }
                                            linkQuickCode = ""
                                            linkErrorMessage = null
                                            Toast.makeText(context, "Sucursal '${parsed.branchName}' vinculada a tu cadena.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            linkErrorMessage = "Formato no válido."
                                        }
                                    } else {
                                        linkErrorMessage = "Portapapeles vacío."
                                    }
                                },
                                shape = RoundedCornerShape(NoirRadius.sm),
                                colors = ButtonDefaults.buttonColors(containerColor = NoirColors.SurfaceRaised, contentColor = NoirColors.Cream),
                                border = BorderStroke(1.dp, NoirColors.HairlineStrong),
                                modifier = Modifier.height(54.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Pegar", tint = NoirColors.Cream, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Pegar", style = NoirType.label, color = NoirColors.Cream)
                            }
                        }

                        OutlinedTextField(
                            value = linkBranchName,
                            onValueChange = {
                                linkBranchName = it
                                linkErrorMessage = null
                            },
                            label = { Text("Nombre de la Sucursal", color = NoirColors.TextSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(NoirRadius.sm),
                            colors = noirFieldColors(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
                        ) {
                            OutlinedTextField(
                                value = linkManagerName,
                                onValueChange = { linkManagerName = it },
                                label = { Text("Encargado", color = NoirColors.TextSecondary) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(NoirRadius.sm),
                                colors = noirFieldColors(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = linkPin,
                                onValueChange = {
                                    if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                                        linkPin = it
                                        linkErrorMessage = null
                                    }
                                },
                                label = { Text("PIN (4 d.)", color = NoirColors.TextSecondary) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                shape = RoundedCornerShape(NoirRadius.sm),
                                colors = noirFieldColors(),
                                singleLine = true
                            )
                        }

                        if (linkErrorMessage != null) {
                            Text("⚠️ $linkErrorMessage", style = NoirType.bodySmall, color = NoirColors.Danger, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val bName = linkBranchName.trim()
                            val mName = linkManagerName.trim().ifBlank { "Encargado de Turno" }
                            val pin = linkPin.trim()
                            viewModel.addBranch(bName, mName, "", pin)
                            isManualLinkDialogOpen = false
                            Toast.makeText(context, "Sucursal '$bName' vinculada con éxito", Toast.LENGTH_SHORT).show()
                        },
                        enabled = linkBranchName.isNotBlank() && linkPin.length >= 4,
                        colors = ButtonDefaults.buttonColors(containerColor = NoirColors.Cream, contentColor = NoirColors.OnCream),
                        shape = RoundedCornerShape(NoirRadius.md)
                    ) {
                        Text("Vincular Sucursal", style = NoirType.button)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isManualLinkDialogOpen = false }) {
                        Text("Cancelar", color = NoirColors.Cream)
                    }
                }
            )
        }
    }
}

data class ParsedBranchLink(
    val branchName: String,
    val managerName: String,
    val pin: String,
    val ownerHwId: String = ""
)

private fun parseBranchQuickCode(raw: String): ParsedBranchLink? {
    val lines = raw.lines()
    val target = lines.firstOrNull { it.contains("SGNIA-TIENDA:") || it.contains("SGNIA-SUCURSAL|") } ?: raw
    val clean = target
        .replace("📋 Código de Vinculación:", "")
        .replace("Código de Vinculación:", "")
        .replace("SGNIA-TIENDA:", "")
        .replace("SGNIA-SUCURSAL|", "")
        .trim()
    val parts = clean.split("|")
    if (parts.size >= 4) {
        val branchName = parts[0].trim()
        val managerName = parts[1].trim()
        val pin = parts[2].trim()
        val ownerHwId = parts[3].trim()
        if (branchName.isNotBlank() && pin.length >= 4) {
            return ParsedBranchLink(branchName, managerName, pin, ownerHwId)
        }
    } else if (parts.size == 3) {
        val branchName = parts[0].trim()
        val managerName = parts[1].trim()
        val pin = parts[2].trim()
        if (branchName.isNotBlank() && pin.length >= 4) {
            return ParsedBranchLink(branchName, managerName, pin, "")
        }
    } else if (parts.size == 2) {
        val branchName = parts[0].trim()
        val pin = parts[1].trim()
        if (branchName.isNotBlank() && pin.length >= 4) {
            return ParsedBranchLink(branchName, "Encargado de Turno", pin, "")
        }
    }
    return null
}

// =====================================================================
// COMPONENTES PRIVADOS DEL LOGIN
// =====================================================================

@Composable
private fun RoleTile(
    title: String,
    caption: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier.heightIn(min = 132.dp),
        hero = selected,
        onClick = onClick,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(NoirSpace.md),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) NoirColors.OnCream else NoirColors.Cream,
                modifier = Modifier.size(26.dp)
            )
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(NoirRadius.pill))
                    .border(
                        1.5.dp,
                        if (selected) NoirColors.OnCream else NoirColors.HairlineStrong,
                        RoundedCornerShape(NoirRadius.pill)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(NoirRadius.pill))
                            .background(NoirColors.OnCream)
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f, fill = false))
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) NoirColors.OnCream else NoirColors.TextPrimary
        )
        Text(
            text = caption,
            style = NoirType.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
            color = if (selected) NoirColors.OnCreamSecondary else NoirColors.TextSecondary
        )
    }
}

@Composable
private fun PinBlock(
    hint: String,
    pin: String,
    isError: Boolean,
    errorMessage: String,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NoirSpace.md)
    ) {
        Text(hint, style = NoirType.bodySmall, color = NoirColors.TextSecondary)
        NoirPinDots(length = pin.length, maxDigits = 4, isError = isError)
        Box(modifier = Modifier.height(18.dp), contentAlignment = Alignment.Center) {
            if (isError) {
                Text(
                    text = errorMessage,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NoirColors.Danger
                )
            }
        }
        NoirKeypad(
            onDigit = onDigit,
            onDelete = onDelete,
            onConfirm = onConfirm,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PasswordBlock(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    errorMessage: String?,
    buttonText: String,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = NoirColors.Cream) },
            trailingIcon = {
                IconButton(onClick = onToggleVisible) {
                    Icon(
                        imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Ocultar" else "Mostrar",
                        tint = NoirColors.TextSecondary
                    )
                }
            },
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            textStyle = NoirType.body.copy(fontSize = 18.sp, color = NoirColors.TextPrimary),
            colors = noirFieldColors(),
            shape = RoundedCornerShape(NoirRadius.md),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = NoirSpace.touch)
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = NoirColors.Danger,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
        NoirPrimaryButton(text = buttonText, onClick = onSubmit)
    }
}

@Composable
private fun BranchSelector(
    branches: List<BranchEntity>,
    selected: BranchEntity?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (BranchEntity) -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = NoirSpace.touch)
                .clip(RoundedCornerShape(NoirRadius.md))
                .background(NoirColors.SurfaceSunken)
                .border(1.dp, NoirColors.HairlineStrong, RoundedCornerShape(NoirRadius.md))
                .clickable { onExpandedChange(!expanded) }
                .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Storefront, contentDescription = null, tint = NoirColors.Cream)
            Spacer(Modifier.width(NoirSpace.sm))
            Column(modifier = Modifier.weight(1f)) {
                NoirLabel("Sucursal", color = NoirColors.TextMuted)
                Text(
                    text = selected?.branchName ?: "Seleccione sucursal…",
                    style = NoirType.title.copy(fontSize = 17.sp),
                    color = NoirColors.TextPrimary
                )
                if (selected != null) {
                    Text(
                        text = "Encargado: ${selected.managerName}",
                        style = NoirType.bodySmall,
                        color = NoirColors.TextSecondary
                    )
                }
            }
            Icon(Icons.Default.ExpandMore, contentDescription = "Cambiar sucursal", tint = NoirColors.TextSecondary)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(NoirColors.SurfaceRaised)
        ) {
            branches.forEach { branch ->
                DropdownMenuItem(
                    text = {
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text(branch.branchName, fontWeight = FontWeight.Bold, color = NoirColors.TextPrimary, fontSize = 16.sp)
                            Text("Encargado: ${branch.managerName}", fontSize = 12.sp, color = NoirColors.TextSecondary)
                        }
                    },
                    onClick = { onSelect(branch) }
                )
            }
        }
    }
}

@Composable
private fun noirFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NoirColors.Cream,
    unfocusedBorderColor = NoirColors.HairlineStrong,
    focusedTextColor = NoirColors.TextPrimary,
    unfocusedTextColor = NoirColors.TextPrimary,
    focusedLabelColor = NoirColors.Cream,
    unfocusedLabelColor = NoirColors.TextSecondary,
    cursorColor = NoirColors.Cream,
    focusedContainerColor = NoirColors.SurfaceSunken,
    unfocusedContainerColor = NoirColors.SurfaceSunken
)
