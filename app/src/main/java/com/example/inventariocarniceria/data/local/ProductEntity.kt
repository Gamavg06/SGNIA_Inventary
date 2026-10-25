package com.example.inventariocarniceria.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val code: Int,
    val localCode: Int = code,
    val name: String,
    val synonyms: String = "", // Sinónimos separados por coma (ej. "quesillo, hebra, fundir")
    val maxStock: Double = 20.00,
    val price: Double = 150.00,
    val category: String = "Res", // Res, Cerdo, Pollo, Embutidos, Quesos / Lácteos, Otros
    val imageUri: String? = null,
    val orderUnitType: String = "KG", // "KG", "CAJA", "PIEZA"
    val weightPerBoxKg: Double = 0.0, // Peso promedio por caja (ej. Menudo: 27.24 kg, Pierna: 32.0 kg)
    val unitsPerBox: Int = 1, // Piezas por caja (ej. Pierna: 3 piernas por caja)
    val parentProductCode: Int? = null, // Código del producto padre si es derivado (ej. 20 para Pierna sin Hueso)
    val currentStockUnits: Double = 0.0 // Existencia en cajas o unidades
)

object ProductCategories {
    val ALL = listOf("Todas", "Res", "Cerdo", "Pollo", "Embutidos", "Quesos / Lácteos", "Otros")
    val FOR_PRODUCT_CREATION = listOf("Res", "Cerdo", "Pollo", "Embutidos", "Quesos / Lácteos", "Otros")
}

