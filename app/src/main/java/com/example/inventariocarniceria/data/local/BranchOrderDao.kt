package com.example.inventariocarniceria.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BranchOrderDao {
    @Query("SELECT * FROM branch_orders ORDER BY branchName ASC, productCode ASC")
    fun getAllBranchOrders(): Flow<List<BranchOrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBranchOrders(orders: List<BranchOrderEntity>)

    @Query("DELETE FROM branch_orders")
    fun clearAllBranchOrders()
}
