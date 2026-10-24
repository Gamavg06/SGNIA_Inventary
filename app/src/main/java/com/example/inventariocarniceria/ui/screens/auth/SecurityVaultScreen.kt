package com.example.inventariocarniceria.ui.screens.auth

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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
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

    var selectedRole by remember { mutableStateOf("ADMIN") }
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

    // ---------------------------------------------------------------
    // Validaciones (misma lógica que la versión anterior)
    // ---------------------------------------------------------------
    fun tryAdmin(input: String, fromPin: Boolean) {
        val validPin = uiState.adminPin.ifBlank { "1234" }
        if (input == validPin || input == "1234") {
            viewModel.loginUser("Matriz Central", "ADMIN")
            onLoginSuccess()
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            if (fromPin) {
                isAdminPinError = true
                adminErrorMessage = "PIN incorrecto. Reintenta."
                adminEnteredPin = ""
            } else {
                adminErrorMessage = "Contraseña incorrecta."
            }
        }
    }

    fun tryStore(input: String, fromPin: Boolean) {
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
            viewModel.loginUser(branch.branchName, "ENCARGADO")
            onLoginSuccess()
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
            if (fromPin) {
                isStorePinError = true
                storeErrorMessage = "PIN incorrecto para ${branch.branchName}."
                storeEnteredPin = ""
            } else {
                storeErrorMessage = "Contraseña incorrecta para ${branch.branchName}"
            }
        }
    }

    val isAdmin = selectedRole == "ADMIN"

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
                RoleTile(
                    modifier = Modifier.weight(1f),
                    title = "Dueño",
                    caption = "Pedidos unificados, sucursales y proveedores",
                    icon = Icons.Default.AdminPanelSettings,
                    selected = isAdmin,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedRole = "ADMIN"
                        isAdminPinError = false
                        adminEnteredPin = ""
                        adminErrorMessage = null
                    }
                )
                RoleTile(
                    modifier = Modifier.weight(1f),
                    title = "Tienda",
                    caption = "Básculas en vivo, catálogo y mostrador",
                    icon = Icons.Default.Storefront,
                    selected = !isAdmin,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedRole = "ENCARGADO"
                        isStorePinError = false
                        storeEnteredPin = ""
                        storeErrorMessage = null
                    }
                )
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

                if (isAdmin) {
                    // ---------------- DUEÑO ----------------
                    if (authMode == "PIN") {
                        PinBlock(
                            hint = "PIN de 4 dígitos",
                            pin = adminEnteredPin,
                            isError = isAdminPinError,
                            errorMessage = adminErrorMessage ?: "PIN incorrecto. Reintenta.",
                            onDigit = { digit ->
                                if (adminEnteredPin.length < 4) {
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
                            StatusPill("Sin sucursales", PillTone.Pending)
                            Text(
                                text = "Aún no hay sucursales registradas. Entra primero a la Consola del Dueño (PIN 1234) para darlas de alta con su contraseña.",
                                style = NoirType.bodySmall,
                                color = NoirColors.TextSecondary
                            )
                            NoirPrimaryButton(
                                text = "Ir a Consola del Dueño",
                                onClick = {
                                    selectedRole = "ADMIN"
                                    authMode = "PIN"
                                }
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
                                    if (storeEnteredPin.length < 4) {
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
    }
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
