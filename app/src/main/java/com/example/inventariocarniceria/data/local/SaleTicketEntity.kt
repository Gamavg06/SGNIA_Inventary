package com.example.inventariocarniceria.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_tickets",
    indices = [
        Index(value = ["operationNumber"]),
        Index(value = ["ticketBarcode"])
    ]
)
data class SaleTicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationNumber: String,
    val ticketBarcode: String,
    val branchId: String = "Sucursal Principal SGNIA",
    val productCode: Int,
    val productName: String,
    val weightKg: Double,
    val pricePerKg: Double,
    val totalAmount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "COMPLETADA", // "COMPLETADA", "INVERTIDA", "REEMBOLSADA"
    val reversalReason: String? = null,
    val reversalTimestamp: Long? = null,
    val cashierName: String = "Encargado de Turno"
)
