package com.example.sgniakeygen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

// Paleta Oficial SGNIA Noir & Crema
val NoirBg = Color(0xFF0F0E0D)
val NoirSurface = Color(0xFF181715)
val NoirSurfaceRaised = Color(0xFF1F1D1A)
val NoirSurfaceSunken = Color(0xFF121110)
val NoirHairline = Color(0xFF2A2723)
val NoirHairlineStrong = Color(0xFF3A352F)
val NoirCream = Color(0xFFEEDDC8)
val NoirOnCream = Color(0xFF141312)
val NoirTextPrimary = Color(0xFFF3EFE6)
val NoirTextSecondary = Color(0xFFA8A196)
val NoirEmerald = Color(0xFF4ADE80)
val NoirAmber = Color(0xFFD9A441)
val NoirDanger = Color(0xFFE07A6F)

private const val HMAC_SECRET_SEED = "SGNIA_ENTERPRISE_KEY_2026_SECURITY_SALT_GAMAVEL_MEAT_SCALE"
private const val PREFS_KEYGEN = "sgnia_keygen_admin_database"
private const val KEY_OWNERS_JSON = "owners_portfolio_json"

private const val PREFS_PRICING = "sgnia_keygen_pricing_config"
private const val KEY_PRICE_MONTHLY = "price_monthly"
private const val KEY_PRICE_ANNUAL = "price_annual"
private const val KEY_PRICE_LIFETIME = "price_lifetime"
private const val KEY_PRICE_TRIAL = "price_trial"

enum class KeygenTier(val code: String, val label: String, val badge: String) {
    DEMO_10M("10M", "10 Minutos (Demo en Vivo)", "⏱️ DEMO 10 MIN"),
    TRIAL_7D("07D", "7 Días (Prueba Comercial)", "📅 PRUEBA 7 DÍAS"),
    RENTAL_1M("01M", "Renta Mensual (30 días)", "📆 RENTA MENSUAL"),
    RENTAL_1Y("01Y", "Renta Anual (365 días)", "🗓️ RENTA ANUAL"),
    LIFETIME("LIFE", "Licencia Perpetua / Venta Definitiva", "♾️ LICENCIA PERPETUA")
}

data class PricingConfig(
    val monthlyRentPerBranch: Double = 600.0,
    val annualRentPerBranch: Double = 5500.0,
    val lifetimePerBranch: Double = 12000.0,
    val trialPerBranch: Double = 0.0
)

data class BranchItem(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var hardwareId: String = "",
    var generatedKey: String = "",
    var status: String = "PENDIENTE"
)

data class OwnerRecord(
    val id: String = UUID.randomUUID().toString(),
    val ownerName: String,
    val companyName: String,
    val phoneWhatsApp: String,
    val email: String,
    val membershipTier: KeygenTier,
    val branches: List<BranchItem>,
    val pricePerBranch: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentStatus: String = "NO_PAGADO", // "PAGADO" o "NO_PAGADO"
    val paymentMethod: String = "Transferencia Bancaria",
    val createdAt: Long = System.currentTimeMillis()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = NoirBg) {
                KeygenAdminApp()
            }
        }
    }
}

@Composable
fun KeygenAdminApp() {
    val context = LocalContext.current
    var ownersList by remember { mutableStateOf(loadOwnersFromPrefs(context)) }
    var pricingConfig by remember { mutableStateOf(loadPricingConfig(context)) }
    var isShowingPricingDialog by remember { mutableStateOf(false) }

    // Estados de navegación interna
    var selectedOwnerForView by remember { mutableStateOf<OwnerRecord?>(null) }
    var isCreatingOwner by remember { mutableStateOf(false) }

    fun refreshOwners() {
        ownersList = loadOwnersFromPrefs(context)
    }

    if (isShowingPricingDialog) {
        PricingConfigDialog(
            currentConfig = pricingConfig,
            onDismiss = { isShowingPricingDialog = false },
            onSave = { updatedConfig ->
                savePricingConfig(context, updatedConfig)
                pricingConfig = updatedConfig
                isShowingPricingDialog = false
                Toast.makeText(context, "Tarifario maestro actualizado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (isCreatingOwner) {
        CreateOrEditOwnerScreen(
            existingOwner = null,
            pricingConfig = pricingConfig,
            onDismiss = { isCreatingOwner = false },
            onSave = { newOwner ->
                saveOwnerToPrefs(context, newOwner)
                refreshOwners()
                isCreatingOwner = false
            }
        )
    } else if (selectedOwnerForView != null) {
        OwnerDetailScreen(
            owner = selectedOwnerForView!!,
            pricingConfig = pricingConfig,
            onBack = { selectedOwnerForView = null },
            onUpdateOwner = { updated ->
                saveOwnerToPrefs(context, updated)
                refreshOwners()
                selectedOwnerForView = updated
            },
            onDeleteOwner = { toDelete ->
                deleteOwnerFromPrefs(context, toDelete.id)
                refreshOwners()
                selectedOwnerForView = null
            }
        )
    } else {
        OwnerPortfolioScreen(
            owners = ownersList,
            onSelectOwner = { selectedOwnerForView = it },
            onCreateNewOwner = { isCreatingOwner = true },
            onOpenPricing = { isShowingPricingDialog = true }
        )
    }
}

// =========================================================================
// PANTALLA 1: LISTA PRINCIPAL DE CLIENTES / PROPIETARIOS
// =========================================================================
@Composable
fun OwnerPortfolioScreen(
    owners: List<OwnerRecord>,
    onSelectOwner: (OwnerRecord) -> Unit,
    onCreateNewOwner: () -> Unit,
    onOpenPricing: () -> Unit
) {
    Scaffold(
        containerColor = NoirBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNewOwner,
                containerColor = NoirCream,
                contentColor = NoirOnCream,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NoirOnCream)
                    Text("Nuevo Cliente", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NoirSurfaceRaised)
                                .border(1.dp, NoirHairline, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = NoirCream, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "SGNIA COMPANY",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NoirCream
                            )
                            Text(
                                text = "Cartera de Clientes",
                                style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NoirTextPrimary)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón de Tarifario Maestro
                        IconButton(
                            onClick = onOpenPricing,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NoirSurfaceRaised)
                                .border(1.dp, NoirHairline, RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Tarifario Maestro", tint = NoirCream, modifier = Modifier.size(20.dp))
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(NoirEmerald.copy(alpha = 0.15f))
                                .border(1.dp, NoirEmerald.copy(alpha = 0.4f), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("${owners.size} CLIENTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NoirEmerald)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = NoirHairline)
            }

            if (owners.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NoirSurfaceSunken)
                            .border(1.dp, NoirHairline, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = NoirTextSecondary, modifier = Modifier.size(44.dp))
                            Text("Aún no tienes clientes registrados", fontWeight = FontWeight.Bold, color = NoirTextPrimary, fontSize = 14.sp)
                            Text("Pulsa '+ Nuevo Cliente' para registrar al dueño de una carnicería, cotizar sus sucursales y generar comprobantes.", fontSize = 12.sp, color = NoirTextSecondary)
                        }
                    }
                }
            } else {
                items(owners) { owner ->
                    OwnerCardSummary(owner = owner, onClick = { onSelectOwner(owner) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun OwnerCardSummary(
    owner: OwnerRecord,
    onClick: () -> Unit
) {
    val isPaid = owner.paymentStatus == "PAGADO"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = NoirSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, NoirHairline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = owner.ownerName,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = NoirTextPrimary
                    )
                    Text(
                        text = owner.companyName.ifBlank { "Sin razón social" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NoirCream
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Badge de Membresía
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                when (owner.membershipTier) {
                                    KeygenTier.LIFETIME -> NoirEmerald.copy(alpha = 0.15f)
                                    KeygenTier.RENTAL_1M, KeygenTier.RENTAL_1Y -> NoirCream.copy(alpha = 0.15f)
                                    else -> NoirAmber.copy(alpha = 0.15f)
                                }
                            )
                            .border(
                                1.dp,
                                when (owner.membershipTier) {
                                    KeygenTier.LIFETIME -> NoirEmerald
                                    KeygenTier.RENTAL_1M, KeygenTier.RENTAL_1Y -> NoirCream
                                    else -> NoirAmber
                                },
                                RoundedCornerShape(100.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = owner.membershipTier.badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (owner.membershipTier) {
                                KeygenTier.LIFETIME -> NoirEmerald
                                KeygenTier.RENTAL_1M, KeygenTier.RENTAL_1Y -> NoirCream
                                else -> NoirAmber
                            }
                        )
                    }

                    // Badge de Pago
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isPaid) NoirEmerald.copy(alpha = 0.15f) else NoirDanger.copy(alpha = 0.15f))
                            .border(1.dp, if (isPaid) NoirEmerald else NoirDanger, RoundedCornerShape(100.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isPaid) "PAGADO" else "PENDIENTE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isPaid) NoirEmerald else NoirDanger
                        )
                    }
                }
            }

            HorizontalDivider(color = NoirHairline, modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏬 ${owner.branches.size} Sucursales",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = NoirTextPrimary
                )

                Text(
                    text = "Total: $${String.format(Locale.US, "%,.2f", owner.totalAmount)} MXN",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = if (isPaid) NoirEmerald else NoirAmber
                )
            }
        }
    }
}

// =========================================================================
// DIÁLOGO: TARIFARIO MAESTRO (CONFIGURACIÓN DE PRECIOS BASE)
// =========================================================================
@Composable
fun PricingConfigDialog(
    currentConfig: PricingConfig,
    onDismiss: () -> Unit,
    onSave: (PricingConfig) -> Unit
) {
    var monthlyStr by remember { mutableStateOf(currentConfig.monthlyRentPerBranch.toString()) }
    var annualStr by remember { mutableStateOf(currentConfig.annualRentPerBranch.toString()) }
    var lifetimeStr by remember { mutableStateOf(currentConfig.lifetimePerBranch.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = NoirSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, NoirCream.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TARIFARIO MAESTRO SGNIA", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NoirCream, fontWeight = FontWeight.Bold)
                        Text("Precios Base por Sucursal", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NoirTextPrimary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = NoirTextSecondary)
                    }
                }

                Text(
                    text = "Configura los precios sugeridos al crear contratos con nuevos dueños. Siempre podrás editarlos individualmente.",
                    fontSize = 12.sp,
                    color = NoirTextSecondary
                )

                OutlinedTextField(
                    value = monthlyStr,
                    onValueChange = { monthlyStr = it },
                    label = { Text("Renta Mensual por Sucursal (MXN)", color = NoirTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = outlinedColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = annualStr,
                    onValueChange = { annualStr = it },
                    label = { Text("Renta Anual por Sucursal (MXN)", color = NoirTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = outlinedColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lifetimeStr,
                    onValueChange = { lifetimeStr = it },
                    label = { Text("Licencia Perpetua / Venta Definitiva (MXN)", color = NoirTextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = outlinedColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, NoirHairline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirTextSecondary)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val newConfig = PricingConfig(
                                monthlyRentPerBranch = monthlyStr.toDoubleOrNull() ?: 600.0,
                                annualRentPerBranch = annualStr.toDoubleOrNull() ?: 5500.0,
                                lifetimePerBranch = lifetimeStr.toDoubleOrNull() ?: 12000.0,
                                trialPerBranch = 0.0
                            )
                            onSave(newConfig)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Guardar Tarifas", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// PANTALLA 2: REGISTRO DE CLIENTE, SUCURSALES Y COTIZACIÓN COMERCIAL
// =========================================================================
@Composable
fun CreateOrEditOwnerScreen(
    existingOwner: OwnerRecord?,
    pricingConfig: PricingConfig,
    onDismiss: () -> Unit,
    onSave: (OwnerRecord) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var ownerName by remember { mutableStateOf(existingOwner?.ownerName ?: "") }
    var companyName by remember { mutableStateOf(existingOwner?.companyName ?: "") }
    var phoneWhatsApp by remember { mutableStateOf(existingOwner?.phoneWhatsApp ?: "") }
    var email by remember { mutableStateOf(existingOwner?.email ?: "") }
    var selectedTier by remember { mutableStateOf(existingOwner?.membershipTier ?: KeygenTier.LIFETIME) }

    val branches = remember {
        mutableStateListOf<BranchItem>().apply {
            if (existingOwner != null && existingOwner.branches.isNotEmpty()) {
                addAll(existingOwner.branches)
            } else {
                add(BranchItem(name = "Sucursal Matriz - Centro"))
            }
        }
    }

    // Campos de precios y estado de pago
    val defaultUnitPrice = when (selectedTier) {
        KeygenTier.RENTAL_1M -> pricingConfig.monthlyRentPerBranch
        KeygenTier.RENTAL_1Y -> pricingConfig.annualRentPerBranch
        KeygenTier.LIFETIME -> pricingConfig.lifetimePerBranch
        KeygenTier.DEMO_10M, KeygenTier.TRIAL_7D -> 0.0
    }

    var pricePerBranchStr by remember {
        mutableStateOf(
            existingOwner?.let { String.format(Locale.US, "%.2f", it.pricePerBranch) }
                ?: String.format(Locale.US, "%.2f", defaultUnitPrice)
        )
    }

    var totalAmountStr by remember {
        mutableStateOf(
            existingOwner?.let { String.format(Locale.US, "%.2f", it.totalAmount) }
                ?: String.format(Locale.US, "%.2f", defaultUnitPrice * branches.size)
        )
    }

    var isPaid by remember { mutableStateOf(existingOwner?.paymentStatus == "PAGADO") }
    var paymentMethod by remember { mutableStateOf(existingOwner?.paymentMethod ?: "Transferencia Bancaria") }

    fun recalculateTotals(tier: KeygenTier, count: Int) {
        val unit = when (tier) {
            KeygenTier.RENTAL_1M -> pricingConfig.monthlyRentPerBranch
            KeygenTier.RENTAL_1Y -> pricingConfig.annualRentPerBranch
            KeygenTier.LIFETIME -> pricingConfig.lifetimePerBranch
            KeygenTier.DEMO_10M, KeygenTier.TRIAL_7D -> 0.0
        }
        pricePerBranchStr = String.format(Locale.US, "%.2f", unit)
        totalAmountStr = String.format(Locale.US, "%.2f", unit * count)
    }

    fun generateKeyForBranch(index: Int) {
        val branch = branches[index]
        val cleanHw = branch.hardwareId.trim().uppercase(Locale.US)
            .replace("SGN", "")
            .replace("-", "")
            .replace(" ", "")

        if (cleanHw.length < 8) {
            Toast.makeText(context, "El Hardware ID de '${branch.name.ifBlank { "Sucursal" }}' debe tener al menos 8 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }

        val chars = "0123456789ABCDEF"
        val token = (1..4).map { chars.random() }.joinToString("")
        val payload = "$cleanHw:${selectedTier.code}:$token"
        val hmac = calculateHmacSha256(payload, HMAC_SECRET_SEED).take(8).uppercase(Locale.US)
        val hmacPart1 = hmac.substring(0, 4)
        val hmacPart2 = hmac.substring(4, 8)

        branch.generatedKey = "SGN-${selectedTier.code}-$token-$hmacPart1-$hmacPart2"
        branch.status = "ACTIVADA (${selectedTier.code})"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // CABECERA
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NoirSurfaceRaised)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = NoirCream)
                }

                Column {
                    Text(
                        text = "REGISTRO DE CLIENTE Y LICENCIAMIENTO",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirCream
                    )
                    Text(
                        text = if (existingOwner == null) "Nuevo Contrato Multitienda" else "Editar Contrato",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoirTextPrimary
                    )
                }
            }
            HorizontalDivider(color = NoirHairline, modifier = Modifier.padding(vertical = 8.dp))
        }

        // BLOQUE 1: DATOS DEL DUEÑO (PROPIETARIO)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NoirHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. DATOS DEL DUEÑO (PROPIETARIO)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NoirCream
                    )

                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre del Dueño (ej. Gamaliel Velázquez Galicia)", color = NoirTextSecondary) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = outlinedColors()
                    )

                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Empresa / Marca (ej. Carnicería La Bendición)", color = NoirTextSecondary) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = outlinedColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = phoneWhatsApp,
                            onValueChange = { phoneWhatsApp = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Teléfono WhatsApp", color = NoirTextSecondary) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = outlinedColors()
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Correo Electrónico", color = NoirTextSecondary) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = outlinedColors()
                        )
                    }
                }
            }
        }

        // BLOQUE 2: SELECCIÓN DE MEMBRESÍA
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NoirHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "2. SELECCIÓN DE MEMBRESÍA",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = NoirCream
                        )
                        Text(
                            text = "${branches.size} Sucursales",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoirEmerald
                        )
                    }

                    KeygenTier.entries.forEach { tier ->
                        val isSelected = selectedTier == tier
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NoirCream else NoirSurfaceRaised)
                                .border(1.dp, if (isSelected) NoirCream else NoirHairline, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedTier = tier
                                    recalculateTotals(tier, branches.size)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tier.label,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) NoirOnCream else NoirTextPrimary
                            )

                            Text(
                                text = tier.badge,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF4A4640) else NoirTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // BLOQUE 3: PRECIOS, FACTURACIÓN Y ESTADO DE PAGO
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NoirHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "3. CONDICIONES COMERCIALES Y COBRO",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = NoirCream
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pricePerBranchStr,
                            onValueChange = {
                                pricePerBranchStr = it
                                val unit = it.toDoubleOrNull() ?: 0.0
                                totalAmountStr = String.format(Locale.US, "%.2f", unit * branches.size)
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Precio x Sucursal (MXN)", color = NoirTextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = outlinedColors()
                        )

                        OutlinedTextField(
                            value = totalAmountStr,
                            onValueChange = { totalAmountStr = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Total a Cobrar (MXN)", color = NoirTextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = outlinedColors()
                        )
                    }

                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Método de Pago (ej. Transferencia, Efectivo)", color = NoirTextSecondary) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = outlinedColors()
                    )

                    // Estado de Pago: Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estado del Pago:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoirTextPrimary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Botón Pagado
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isPaid) NoirEmerald else NoirSurfaceRaised)
                                    .border(1.dp, if (isPaid) NoirEmerald else NoirHairline, RoundedCornerShape(10.dp))
                                    .clickable { isPaid = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "🟢 PAGADA",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = if (isPaid) Color.Black else NoirTextSecondary
                                )
                            }

                            // Botón No Pagado
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isPaid) NoirDanger else NoirSurfaceRaised)
                                    .border(1.dp, if (!isPaid) NoirDanger else NoirHairline, RoundedCornerShape(10.dp))
                                    .clickable { isPaid = false }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "🔴 NO PAGADA",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = if (!isPaid) Color.White else NoirTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // BLOQUE 4: REGISTRO DE SUCURSALES (Vincular Hardware ID & Claves)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "4. REGISTRO DE SUCURSALES (${branches.size})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = NoirCream
                )

                Button(
                    onClick = {
                        val num = branches.size + 1
                        branches.add(BranchItem(name = "Sucursal $num"))
                        val unit = pricePerBranchStr.toDoubleOrNull() ?: 0.0
                        totalAmountStr = String.format(Locale.US, "%.2f", unit * branches.size)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirSurfaceRaised, contentColor = NoirCream),
                    border = BorderStroke(1.dp, NoirCream.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("＋ Agregar Otra Sucursal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(branches.indices.toList()) { index ->
            val branch = branches[index]
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NoirHairlineStrong)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "┌── SUCURSAL ${index + 1} ─────────────────────────────",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = NoirCream
                        )

                        if (branches.size > 1) {
                            IconButton(onClick = {
                                branches.removeAt(index)
                                val unit = pricePerBranchStr.toDoubleOrNull() ?: 0.0
                                totalAmountStr = String.format(Locale.US, "%.2f", unit * branches.size)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = NoirDanger, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    OutlinedTextField(
                        value = branch.name,
                        onValueChange = { branch.name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre Sucursal (ej. Sucursal Norte)", color = NoirTextSecondary) },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = outlinedColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = branch.hardwareId,
                            onValueChange = { branch.hardwareId = it.uppercase(Locale.US) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Hardware ID (ej. SGN-8F2A-3C91)", color = NoirTextSecondary) },
                            placeholder = { Text("SGN-XXXX-XXXX", color = NoirTextSecondary) },
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NoirTextPrimary),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = outlinedColors()
                        )

                        OutlinedButton(
                            onClick = {
                                val text = clipboardManager.getText()?.text
                                if (!text.isNullOrBlank()) {
                                    branch.hardwareId = text.trim().uppercase(Locale.US)
                                } else {
                                    Toast.makeText(context, "Portapapeles vacío", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NoirHairline),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirCream),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, tint = NoirCream, modifier = Modifier.size(16.dp))
                        }

                        Button(
                            onClick = { generateKeyForBranch(index) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = NoirOnCream, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generar", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }

                    if (branch.generatedKey.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(NoirSurfaceSunken)
                                .border(1.dp, NoirEmerald.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Clave: ${branch.generatedKey}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NoirCream)
                                    Text("Estado: 🟢 ${branch.status}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NoirEmerald)
                                }

                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(branch.generatedKey))
                                    Toast.makeText(context, "Clave copiada", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NoirCream, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // BOTONES DE ACCIÓN: CANCELAR Y GUARDAR
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, NoirHairline),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NoirTextSecondary),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        if (ownerName.isBlank()) {
                            Toast.makeText(context, "Ingresa el nombre del Dueño.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Auto-generar claves pendientes si tienen hardware ID
                        branches.forEachIndexed { i, b ->
                            if (b.generatedKey.isBlank() && b.hardwareId.isNotBlank()) {
                                generateKeyForBranch(i)
                            }
                        }

                        val record = OwnerRecord(
                            id = existingOwner?.id ?: UUID.randomUUID().toString(),
                            ownerName = ownerName.trim(),
                            companyName = companyName.trim(),
                            phoneWhatsApp = phoneWhatsApp.trim(),
                            email = email.trim(),
                            membershipTier = selectedTier,
                            branches = branches.toList(),
                            pricePerBranch = pricePerBranchStr.toDoubleOrNull() ?: 0.0,
                            totalAmount = totalAmountStr.toDoubleOrNull() ?: 0.0,
                            paymentStatus = if (isPaid) "PAGADO" else "NO_PAGADO",
                            paymentMethod = paymentMethod.trim().ifBlank { "Transferencia Bancaria" },
                            createdAt = existingOwner?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(record)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream),
                    modifier = Modifier.weight(1.5f).height(50.dp)
                ) {
                    Text("💾 Guardar Registro", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }
    }
}

// =========================================================================
// PANTALLA 3: DETALLE DEL DUEÑO, COBRO Y GENERACIÓN DE COMPROBANTE OFICIAL
// =========================================================================
@Composable
fun OwnerDetailScreen(
    owner: OwnerRecord,
    pricingConfig: PricingConfig,
    onBack: () -> Unit,
    onUpdateOwner: (OwnerRecord) -> Unit,
    onDeleteOwner: (OwnerRecord) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        CreateOrEditOwnerScreen(
            existingOwner = owner,
            pricingConfig = pricingConfig,
            onDismiss = { isEditing = false },
            onSave = { updated ->
                onUpdateOwner(updated)
                isEditing = false
            }
        )
        return
    }

    val isPaid = owner.paymentStatus == "PAGADO"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NoirSurfaceRaised)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = NoirCream)
                    }

                    Column {
                        Text(owner.ownerName, fontWeight = FontWeight.Black, fontSize = 17.sp, color = NoirTextPrimary)
                        Text(owner.companyName, fontSize = 12.sp, color = NoirCream)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = NoirCream)
                    }
                    IconButton(onClick = { onDeleteOwner(owner) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = NoirDanger)
                    }
                }
            }
            HorizontalDivider(color = NoirHairline, modifier = Modifier.padding(vertical = 6.dp))
        }

        // TARJETA FINANCIERA Y CONTROL DE COBRO
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isPaid) NoirEmerald.copy(alpha = 0.5f) else NoirDanger.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ESTADO DE COBRO Y CONTRATO", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NoirCream, fontWeight = FontWeight.Bold)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isPaid) NoirEmerald.copy(alpha = 0.15f) else NoirDanger.copy(alpha = 0.15f))
                                .border(1.dp, if (isPaid) NoirEmerald else NoirDanger, RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPaid) "● PAGADO" else "● PENDIENTE DE COBRO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isPaid) NoirEmerald else NoirDanger
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Precio x Sucursal:",
                                fontSize = 11.sp,
                                color = NoirTextSecondary
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", owner.pricePerBranch)} MXN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NoirTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "TOTAL DEL CONTRATO (${owner.branches.size} suc.):",
                                fontSize = 11.sp,
                                color = NoirTextSecondary
                            )
                            Text(
                                text = "$${String.format(Locale.US, "%,.2f", owner.totalAmount)} MXN",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = if (isPaid) NoirEmerald else NoirAmber
                            )
                        }
                    }

                    Text(
                        text = "Método: ${owner.paymentMethod} • Plan: ${owner.membershipTier.label}",
                        fontSize = 11.sp,
                        color = NoirTextSecondary
                    )

                    // Botón para alternar Pagado / No pagado
                    Button(
                        onClick = {
                            val newStatus = if (isPaid) "NO_PAGADO" else "PAGADO"
                            val updated = owner.copy(paymentStatus = newStatus)
                            onUpdateOwner(updated)
                            val msg = if (newStatus == "PAGADO") "Contrato marcado como PAGADO 🟢" else "Contrato marcado como PENDIENTE 🔴"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaid) NoirSurfaceRaised else NoirEmerald,
                            contentColor = if (isPaid) NoirCream else Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isPaid) NoirHairlineStrong else NoirEmerald),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text(
                            text = if (isPaid) "Cambiar a PENDIENTE DE COBRO 🔴" else "Marcar como PAGADA 🟢",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // BOTONES DE COMPROBANTES: PDF OFICIAL Y WHATSAPP
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 1. GENERAR PDF OFICIAL CON LOGOS
                Button(
                    onClick = {
                        try {
                            val pdfFile = PdfReceiptGenerator.generateReceiptPdf(context, owner)
                            PdfReceiptGenerator.shareReceiptPdf(context, pdfFile)
                            Toast.makeText(context, "Comprobante PDF generado exitosamente", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error al generar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NoirCream, contentColor = NoirOnCream),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = NoirOnCream, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📄 Generar y Compartir Comprobante Oficial (PDF)", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                // 2. ENVIAR RESUMEN POR WHATSAPP
                Button(
                    onClick = {
                        val sb = StringBuilder()
                        sb.append("🥩 *SGNIA COMPANY · COMPROBANTE DE LICENCIAMIENTO*\n")
                        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                        sb.append("Cliente: *${owner.ownerName}*\n")
                        sb.append("Empresa: *${owner.companyName.ifBlank { "Carnicería" }}*\n")
                        sb.append("Plan: *${owner.membershipTier.label}*\n")
                        sb.append("Sucursales activas: *${owner.branches.size}*\n")
                        sb.append("Importe Total: *$${String.format(Locale.US, "%,.2f", owner.totalAmount)} MXN*\n")
                        sb.append("Estado de Pago: *${if (isPaid) "🟢 PAGADO" else "🔴 PENDIENTE"}*\n")
                        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
                        sb.append("🔑 *CLAVES DE ACTIVACIÓN INDUSTRIAL:*\n\n")

                        owner.branches.forEachIndexed { i, b ->
                            sb.append("🏬 *${i + 1}. ${b.name}*\n")
                            sb.append("   📱 Hardware ID: `${b.hardwareId}`\n")
                            sb.append("   🔐 Clave: `${b.generatedKey}`\n\n")
                        }

                        sb.append("*Instrucciones de Activación:*\n")
                        sb.append("1. Abre la app en la tablet de cada tienda.\n")
                        sb.append("2. Ve a 'Licencia de Operación'.\n")
                        sb.append("3. Ingresa la clave correspondiente y pulsa 'ACTIVAR'.\n\n")
                        sb.append("_Gamaliel Velazquez Galicia • Liderazgo Técnico_\n")
                        sb.append("_SGNIA COMPANY • Software que Protege y Hace Crecer tu Negocio_\n")
                        sb.append("https://sgnia.xyz • WhatsApp: +52 276 109 5688")

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, sb.toString())
                        }
                        context.startActivity(Intent.createChooser(intent, "Enviar Comprobante a Dueño"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("💬 Enviar Comprobante y Claves por WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                }
            }
        }

        item {
            Text("SUCURSALES VINCULADAS (${owner.branches.size})", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NoirCream)
        }

        items(owner.branches) { branch ->
            Card(
                colors = CardDefaults.cardColors(containerColor = NoirSurfaceRaised),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NoirHairline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(branch.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NoirTextPrimary)
                        Text(branch.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NoirEmerald)
                    }

                    Text("📱 Hardware ID: ${branch.hardwareId}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NoirTextSecondary)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NoirSurfaceSunken)
                            .border(1.dp, NoirHairlineStrong, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(branch.generatedKey, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NoirCream)
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(branch.generatedKey))
                                Toast.makeText(context, "Clave copiada: ${branch.name}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NoirCream, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NoirCream,
    unfocusedBorderColor = NoirHairline,
    focusedContainerColor = NoirSurfaceSunken,
    unfocusedContainerColor = NoirSurfaceSunken,
    focusedTextColor = NoirTextPrimary,
    unfocusedTextColor = NoirTextPrimary
)

// =========================================================================
// PERSISTENCIA LOCAL EN SHARED PREFERENCES
// =========================================================================
private fun loadPricingConfig(context: Context): PricingConfig {
    val prefs = context.getSharedPreferences(PREFS_PRICING, Context.MODE_PRIVATE)
    return PricingConfig(
        monthlyRentPerBranch = prefs.getFloat(KEY_PRICE_MONTHLY, 600f).toDouble(),
        annualRentPerBranch = prefs.getFloat(KEY_PRICE_ANNUAL, 5500f).toDouble(),
        lifetimePerBranch = prefs.getFloat(KEY_PRICE_LIFETIME, 12000f).toDouble(),
        trialPerBranch = prefs.getFloat(KEY_PRICE_TRIAL, 0f).toDouble()
    )
}

private fun savePricingConfig(context: Context, config: PricingConfig) {
    val prefs = context.getSharedPreferences(PREFS_PRICING, Context.MODE_PRIVATE)
    prefs.edit()
        .putFloat(KEY_PRICE_MONTHLY, config.monthlyRentPerBranch.toFloat())
        .putFloat(KEY_PRICE_ANNUAL, config.annualRentPerBranch.toFloat())
        .putFloat(KEY_PRICE_LIFETIME, config.lifetimePerBranch.toFloat())
        .putFloat(KEY_PRICE_TRIAL, config.trialPerBranch.toFloat())
        .apply()
}

private fun loadOwnersFromPrefs(context: Context): List<OwnerRecord> {
    val prefs = context.getSharedPreferences(PREFS_KEYGEN, Context.MODE_PRIVATE)
    val jsonStr = prefs.getString(KEY_OWNERS_JSON, null) ?: return emptyList()

    return try {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<OwnerRecord>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val tier = KeygenTier.valueOf(obj.optString("tier", KeygenTier.LIFETIME.name))
            val branchesArray = obj.optJSONArray("branches") ?: JSONArray()
            val branchList = mutableListOf<BranchItem>()
            for (j in 0 until branchesArray.length()) {
                val bObj = branchesArray.getJSONObject(j)
                branchList.add(
                    BranchItem(
                        id = bObj.optString("id", UUID.randomUUID().toString()),
                        name = bObj.optString("name", "Sucursal"),
                        hardwareId = bObj.optString("hardwareId", ""),
                        generatedKey = bObj.optString("generatedKey", ""),
                        status = bObj.optString("status", "ACTIVADA")
                    )
                )
            }
            list.add(
                OwnerRecord(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    ownerName = obj.optString("ownerName", ""),
                    companyName = obj.optString("companyName", ""),
                    phoneWhatsApp = obj.optString("phoneWhatsApp", ""),
                    email = obj.optString("email", ""),
                    membershipTier = tier,
                    branches = branchList,
                    pricePerBranch = obj.optDouble("pricePerBranch", 0.0),
                    totalAmount = obj.optDouble("totalAmount", 0.0),
                    paymentStatus = obj.optString("paymentStatus", "NO_PAGADO"),
                    paymentMethod = obj.optString("paymentMethod", "Transferencia Bancaria"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}

private fun saveOwnerToPrefs(context: Context, owner: OwnerRecord) {
    val list = loadOwnersFromPrefs(context).toMutableList()
    val index = list.indexOfFirst { it.id == owner.id }
    if (index >= 0) {
        list[index] = owner
    } else {
        list.add(0, owner)
    }
    saveAllOwners(context, list)
}

private fun deleteOwnerFromPrefs(context: Context, ownerId: String) {
    val list = loadOwnersFromPrefs(context).filter { it.id != ownerId }
    saveAllOwners(context, list)
}

private fun saveAllOwners(context: Context, list: List<OwnerRecord>) {
    val array = JSONArray()
    list.forEach { o ->
        val obj = JSONObject()
        obj.put("id", o.id)
        obj.put("ownerName", o.ownerName)
        obj.put("companyName", o.companyName)
        obj.put("phoneWhatsApp", o.phoneWhatsApp)
        obj.put("email", o.email)
        obj.put("tier", o.membershipTier.name)
        obj.put("pricePerBranch", o.pricePerBranch)
        obj.put("totalAmount", o.totalAmount)
        obj.put("paymentStatus", o.paymentStatus)
        obj.put("paymentMethod", o.paymentMethod)
        obj.put("createdAt", o.createdAt)

        val bArray = JSONArray()
        o.branches.forEach { b ->
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("name", b.name)
            bObj.put("hardwareId", b.hardwareId)
            bObj.put("generatedKey", b.generatedKey)
            bObj.put("status", b.status)
            bArray.put(bObj)
        }
        obj.put("branches", bArray)
        array.put(obj)
    }
    val prefs = context.getSharedPreferences(PREFS_KEYGEN, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_OWNERS_JSON, array.toString()).apply()
}

private fun calculateHmacSha256(data: String, key: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
    mac.init(secretKey)
    val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02X".format(it) }
}
