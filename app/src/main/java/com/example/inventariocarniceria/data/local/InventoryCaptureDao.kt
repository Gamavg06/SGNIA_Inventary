package com.example.inventariocarniceria.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryCaptureDao {
    @Query("SELECT * FROM inventory_captures ORDER BY id DESC")
    fun getAllCaptures(): Flow<List<InventoryCaptureEntity>>

    @Query("SELECT * FROM inventory_captures ORDER BY id DESC LIMIT 3")
    fun getRecentCaptures(): Flow<List<InventoryCaptureEntity>>

    @Query("SELECT * FROM inventory_captures WHERE shortage > 0 ORDER BY productCode ASC")
    fun getPendingOrders(): Flow<List<InventoryCaptureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCapture(capture: InventoryCaptureEntity): Long

    @Query("DELETE FROM inventory_captures WHERE productCode = :code")
    fun deleteCapturesByCode(code: Int): Int

    @Query("DELETE FROM inventory_captures")
    fun clearAllCaptures(): Int
}
