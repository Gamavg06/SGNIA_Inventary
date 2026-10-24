package com.example.inventariocarniceria.ui.screens.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventariocarniceria.R
import com.example.inventariocarniceria.ui.design.BentoCard
import com.example.inventariocarniceria.ui.design.NoirColors
import com.example.inventariocarniceria.ui.design.NoirLabel
import com.example.inventariocarniceria.ui.design.NoirPrimaryButton
import com.example.inventariocarniceria.ui.design.NoirRadius
import com.example.inventariocarniceria.ui.design.NoirSecondaryButton
import com.example.inventariocarniceria.ui.design.NoirSpace
import com.example.inventariocarniceria.ui.design.NoirType
import com.example.inventariocarniceria.ui.design.PillTone
import com.example.inventariocarniceria.ui.design.StatusPill
import com.example.inventariocarniceria.ui.screens.legal.DocumentReaderScreen
import com.example.inventariocarniceria.ui.screens.legal.DocumentType

/**
 * PANTALLA: "ACERCA DE SGNIA COMPANY"
 *
 * DECISIONES DE DISEÑO:
 * 1. Organización modular Bento Grid donde cada bloque (identidad, fundador, contacto, stack) tiene jerarquía propia e independiente.
 * 2. Zonas táctiles ampliadas (>= 48dp y 64dp) con alto contraste crema/negro mate para operarse cómodamente en baja luz o con guantes.
 * 3. Enlaces externos protegidos con runCatching que informan de forma explícita qué acciones requieren internet para no generar falsas expectativas offline.
 */
@Composable
fun AboutSgniaScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var readingLegalDoc by remember { mutableStateOf<DocumentType?>(null) }

    if (readingLegalDoc != null) {
        DocumentReaderScreen(
            documentType = readingLegalDoc!!,
            onBack = { readingLegalDoc = null }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NoirColors.Background)
            .padding(horizontal = NoirSpace.md, vertical = NoirSpace.sm),
        verticalArrangement = Arrangement.spacedBy(NoirSpace.gutter)
    ) {
        // BARRA SUPERIOR / REGRESO
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(NoirRadius.sm))
                            .background(NoirColors.SurfaceRaised)
                            .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = NoirColors.Cream
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                }

                Column {
                    NoirLabel("SISTEMA & DESARROLLADOR", color = NoirColors.Gold)
                    Text(
                        text = "Acerca de la App",
                        style = NoirType.display.copy(fontSize = 22.sp),
                        color = NoirColors.TextPrimary
                    )
                }
            }
        }

        // 1. CABECERA: LOGO, NOMBRE, LEMA Y VERSIÓN
        item {
            BentoCard(
                hero = false,
                contentPadding = PaddingValues(NoirSpace.lg),
                verticalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Escudo / Logotipo
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(NoirRadius.md))
                            .background(NoirColors.SurfaceRaised)
                            .border(1.5.dp, NoirColors.HairlineStrong, RoundedCornerShape(NoirRadius.md)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.sglogo),
                            contentDescription = "SGNIA COMPANY Logo",
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    StatusPill(
                        text = "v1.0.0",
                        tone = PillTone.Gold,
                        showDot = true
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "SGNIA COMPANY",
                    style = NoirType.title.copy(fontSize = 22.sp, letterSpacing = 1.sp),
                    color = NoirColors.TextPrimary
                )

                Text(
                    text = "Software & Innovación Digital",
                    style = NoirType.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = NoirColors.Cream
                )

                Text(
                    text = "“Software que Protege y Hace Crecer tu Negocio”",
                    style = NoirType.bodySmall.copy(fontFamily = FontFamily.Serif),
                    color = NoirColors.TextMuted
                )
            }
        }

        // 2. QUIÉNES SOMOS
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NoirLabel("QUIÉNES SOMOS", color = NoirColors.Cream)
                Text(
                    text = "Desarrollo de software profesional: plataformas web, aplicaciones móviles y soluciones de datos a la medida, desde el análisis técnico hasta la entrega en producción.",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextPrimary,
                    lineHeight = 20.sp
                )
            }
        }

        // 3. EL NOMBRE SGNIA
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NoirLabel("EL NOMBRE SGNIA", color = NoirColors.Gold)
                Text(
                    text = "Nace de la unión de Sonia y Gamaliel: una marca, dos personas, software que protege y hace crecer tu negocio.",
                    style = NoirType.bodySmall,
                    color = NoirColors.TextPrimary,
                    lineHeight = 20.sp
                )
            }
        }

        // 4. FUNDADOR & STACK
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NoirLabel("DIRECCIÓN & INGENIERÍA", color = NoirColors.Cream)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Monograma GV
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(NoirColors.Cream)
                            .border(1.5.dp, NoirColors.Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GV",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NoirColors.OnCream
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Gamaliel Velazquez Galicia",
                            style = NoirType.title.copy(fontSize = 15.sp),
                            color = NoirColors.TextPrimary
                        )
                        Text(
                            text = "Fundador de SGNIA COMPANY",
                            style = NoirType.bodySmall,
                            color = NoirColors.TextMuted
                        )
                    }
                }

                // Chips de Tecnologías
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val techs = listOf("C# & .NET", "Python", "Kotlin", "SQL")
                    techs.forEach { tech ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(NoirRadius.sm))
                                .background(NoirColors.SurfaceRaised)
                                .border(1.dp, NoirColors.Hairline, RoundedCornerShape(NoirRadius.sm))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tech,
                                style = NoirType.label.copy(fontSize = 10.sp),
                                color = NoirColors.Cream
                            )
                        }
                    }
                }
            }
        }

        // 5. CONTACTO DIRECTO (FILAS >= 48DP)
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NoirLabel("CONTACTO DIRECTO · SOPORTE TÉCNICO", color = NoirColors.Gold)

                // WhatsApp
                ContactRow(
                    icon = Icons.Default.Phone,
                    title = "WhatsApp de Soporte",
                    detail = "+52 276 109 5688",
                    note = "Atención directa",
                    onClick = {
                        openUrl(context, "https://wa.me/522761095688")
                    }
                )

                HorizontalDivider(color = NoirColors.Hairline, thickness = 0.5.dp)

                // Correo
                ContactRow(
                    icon = Icons.Default.Email,
                    title = "Correo Electrónico",
                    detail = "gmlvega0025@gmail.com",
                    note = "Consultas & cotizaciones",
                    onClick = {
                        openEmail(context, "gmlvega0025@gmail.com")
                    }
                )

                HorizontalDivider(color = NoirColors.Hairline, thickness = 0.5.dp)

                // Ubicación
                ContactRow(
                    icon = Icons.Default.LocationOn,
                    title = "Ubicación Matriz",
                    detail = "Juan Jiménez 405, Centro, 75120 Nopalucan de la Granja, Puebla, México",
                    note = "Abrir mapa",
                    onClick = {
                        openUrl(context, "https://maps.google.com/?q=Juan+Jimenez+405,+Centro,+75120+Nopalucan+de+la+Granja,+Puebla")
                    }
                )
            }
        }

        // 6. REDES SOCIALES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NoirSpace.sm)
            ) {
                NoirSecondaryButton(
                    text = "TikTok @sgn_ia",
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    height = 50.dp,
                    onClick = { openUrl(context, "https://www.tiktok.com/@sgn_ia") },
                    modifier = Modifier.weight(1f)
                )

                NoirSecondaryButton(
                    text = "GitHub @Gamavg06",
                    icon = Icons.Default.Code,
                    height = 50.dp,
                    onClick = { openUrl(context, "https://github.com/Gamavg06") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 7. SITIO WEB OFICIAL
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NoirLabel("PLATAFORMA CORPORATIVA", color = NoirColors.Cream)
                    Text(
                        text = "Requiere internet",
                        fontSize = 11.sp,
                        color = NoirColors.TextMuted
                    )
                }

                NoirPrimaryButton(
                    text = "Visitar sgnia.xyz",
                    icon = Icons.Default.Language,
                    height = 54.dp,
                    onClick = { openUrl(context, "https://sgnia.xyz") }
                )
            }
        }

        // 8. SOBRE ESTA APP (ESPECIFICACIONES INDUSTRIALES)
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NoirLabel("SOBRE ESTA APP · ARQUITECTURA", color = NoirColors.Gold)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPill(
                        text = "Modo Offline-First activo",
                        tone = PillTone.Positive,
                        showDot = true
                    )
                    StatusPill(
                        text = "Base de datos local",
                        tone = PillTone.Positive,
                        showDot = true
                    )
                    StatusPill(
                        text = "Báscula Systel Cuora compatible",
                        tone = PillTone.Positive,
                        showDot = true
                    )
                }
            }
        }

        // 9. DOCUMENTOS LEGALES (TÉRMINOS & PRIVACIDAD)
        item {
            BentoCard(
                contentPadding = PaddingValues(NoirSpace.md),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NoirLabel("DOCUMENTOS LEGALES & LICENCIA", color = NoirColors.Cream)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoirSecondaryButton(
                        text = "Términos",
                        icon = Icons.Default.Description,
                        height = 48.dp,
                        onClick = { readingLegalDoc = DocumentType.TERMS },
                        modifier = Modifier.weight(1f)
                    )

                    NoirSecondaryButton(
                        text = "Privacidad",
                        icon = Icons.Default.PrivacyTip,
                        height = 48.dp,
                        onClick = { readingLegalDoc = DocumentType.PRIVACY },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 10. PIE DE PÁGINA
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = NoirSpace.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HorizontalDivider(color = NoirColors.Hairline, thickness = 1.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "© 2026 SGNIA COMPANY. Todos los derechos reservados.",
                    style = NoirType.bodySmall.copy(fontSize = 11.sp),
                    color = NoirColors.TextMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "sgnia.xyz",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = NoirColors.Cream,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { openUrl(context, "https://sgnia.xyz") }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    note: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(NoirRadius.sm))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(NoirRadius.sm))
                .background(NoirColors.SurfaceRaised),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NoirColors.Cream,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = NoirType.label.copy(fontSize = 10.sp),
                color = NoirColors.TextMuted
            )
            Text(
                text = detail,
                style = NoirType.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = NoirColors.TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(NoirRadius.pill))
                .background(NoirColors.SurfaceRaised)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = note,
                fontSize = 10.sp,
                color = NoirColors.Gold
            )
        }
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "No se pudo abrir el enlace (Requiere internet)", Toast.LENGTH_SHORT).show()
    }
}

private fun openEmail(context: Context, email: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "No hay cliente de correo disponible", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0E0D)
@Composable
fun AboutSgniaScreenPreview() {
    AboutSgniaScreen(onBack = {})
}
