package com.example.inventariocarniceria.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_products")
data class LocalProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val branchId: String,
    val code: Int = 85, // Código para venta rápida por unidades (ej. 85)
    val investmentName: String = "Botanas SGNIA", // Nombre de la inversión editable (ej. "Botanas SGNIA", "Ropa", etc.)
    val name: String, // Nombre del producto / botana (ej. "Manguito Enchilado", "Panditas")
    val category: String = "Botanas", // Botanas, Ropa, Abarrotes, Otros
    val piecesPerBolsita: Int = 6, // Piezas que lleva cada bolsita (ej. 6 manguitos, 11 panditas)
    val quantityToMake: Int = 0, // Bolsitas / unidades por hacer (ej. 10 bolsitas)
    val quantityInStock: Int = 0, // Bolsitas / unidades terminadas en existencia
    val costPrice: Double = 0.0,
    val salePrice: Double = 15.0 // Precio de venta por bolsita / unidad
) {
    val totalPiecesToMake: Int get() = piecesPerBolsita * quantityToMake
    val totalPiecesInStock: Int get() = piecesPerBolsita * quantityInStock
    val totalValueToMake: Double get() = quantityToMake * salePrice
    val totalValueInStock: Double get() = quantityInStock * salePrice
}
