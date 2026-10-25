package com.example.inventariocarniceria.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "despiece_transformaciones")
data class DespieceTransformacionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val branchId: String,
    val parentProductCode: Int,
    val parentProductName: String,
    val initialWeightKg: Double,
    val totalDerivedWeightKg: Double,
    val totalWasteKg: Double,
    val wastePercentage: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val carniceroName: String,
    val notes: String? = null
)

@Entity(
    tableName = "despiece_detalles",
    foreignKeys = [
        ForeignKey(
            entity = DespieceTransformacionEntity::class,
            parentColumns = ["id"],
            childColumns = ["despieceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("despieceId")]
)
data class DespieceDetalleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val despieceId: Long,
    val productCode: Int,
    val productName: String,
    val weightKg: Double,
    val yieldPercentage: Double,
    val isWaste: Boolean = false
)

data class DespieceConDetalles(
    val transformacion: DespieceTransformacionEntity,
    val detalles: List<DespieceDetalleEntity>
)

@Dao
interface DespieceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransformacion(transformacion: DespieceTransformacionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetalles(detalles: List<DespieceDetalleEntity>)

    @Transaction
    suspend fun insertDespieceCompleto(
        transformacion: DespieceTransformacionEntity,
        detalles: List<DespieceDetalleEntity>
    ): Long {
        val id = insertTransformacion(transformacion)
        val detallesConId = detalles.map { it.copy(despieceId = id) }
        insertDetalles(detallesConId)
        return id
    }

    @Query("SELECT * FROM despiece_transformaciones ORDER BY timestamp DESC")
    fun getAllTransformaciones(): Flow<List<DespieceTransformacionEntity>>

    @Query("SELECT * FROM despiece_detalles WHERE despieceId = :despieceId")
    suspend fun getDetallesForDespiece(despieceId: Long): List<DespieceDetalleEntity>
}
