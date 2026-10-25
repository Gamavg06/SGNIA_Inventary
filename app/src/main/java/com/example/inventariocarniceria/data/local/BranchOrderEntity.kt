package com.example.inventariocarniceria.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branch_orders")
data class BranchOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val branchName: String,
    val productCode: Int,
    val productName: String,
    val shortageKg: Double,
    val pricePerKg: Double,
    val estimatedCost: Double = shortageKg * pricePerKg,
    val timestamp: Long = System.currentTimeMillis()
)
