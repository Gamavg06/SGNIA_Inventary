package com.example.inventariocarniceria.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_entries")
data class StockEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val branchId: String = "Sucursal Principal SGNIA",
    val productCode: Int,
    val productName: String,
    val kilosReceived: Double,
    val costPerKg: Double = 0.0,
    val supplierNotes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
