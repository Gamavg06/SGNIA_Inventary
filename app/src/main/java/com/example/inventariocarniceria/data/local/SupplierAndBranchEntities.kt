package com.example.inventariocarniceria.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val contactName: String = "",
    val suppliedCategories: String = "Res, Cerdo"
)

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val branchName: String,
    val managerName: String,
    val phone: String = "",
    val address: String = "",
    val password: String = "1234",
    val isActive: Boolean = true
)
