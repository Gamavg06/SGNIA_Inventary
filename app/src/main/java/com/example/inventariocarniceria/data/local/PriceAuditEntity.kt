package com.example.inventariocarniceria.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "price_audits")
data class PriceAuditEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productCode: Int,
    val productName: String,
    val oldPrice: Double,
    val newPrice: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val managerName: String,
    val branchId: String
)

@Dao
interface PriceAuditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: PriceAuditEntity)

    @Query("SELECT * FROM price_audits ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAudits(): Flow<List<PriceAuditEntity>>

    @Query("SELECT * FROM price_audits WHERE productCode = :code ORDER BY timestamp DESC")
    fun getAuditsByProduct(code: Int): Flow<List<PriceAuditEntity>>
}
