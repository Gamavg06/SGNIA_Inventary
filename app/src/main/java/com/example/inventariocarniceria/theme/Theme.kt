package com.example.inventariocarniceria.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.example.inventariocarniceria.ui.design.NoirColors

private val DarkColorScheme = darkColorScheme(
    primary = NoirColors.Cream,
    secondary = NoirColors.Gold,
    tertiary = NoirColors.Emerald,
    background = NoirColors.Background,
    surface = NoirColors.Surface
)

@Composable
fun InventarioCarniceriaTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
