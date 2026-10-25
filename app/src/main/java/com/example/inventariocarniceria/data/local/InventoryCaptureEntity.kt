package com.example.inventariocarniceria.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_captures")
data class InventoryCaptureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productCode: Int,
    val productName: String,
    val capturedWeight: Double,
    val maxStock: Double,
    val shortage: Double, // Shortage = maxStock - capturedWeight (0 if negative)
    val timestamp: Long = System.currentTimeMillis(),
    val orderedUnits: Double = 0.0, // Cantidad en cajas o piezas
    val unitType: String = "KG" // "KG", "CAJA", "PIEZA"
)
