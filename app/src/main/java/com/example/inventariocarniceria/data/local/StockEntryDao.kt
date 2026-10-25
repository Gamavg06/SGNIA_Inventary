package com.example.inventariocarniceria.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StockEntryDao {
    @Query("SELECT * FROM stock_entries ORDER BY id DESC")
    fun getAllEntries(): Flow<List<StockEntryEntity>>

    @Query("SELECT * FROM stock_entries ORDER BY id DESC LIMIT 5")
    fun getRecentEntries(): Flow<List<StockEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStockEntry(entry: StockEntryEntity): Long

    @Query("DELETE FROM stock_entries")
    fun clearAllStockEntries()
}
