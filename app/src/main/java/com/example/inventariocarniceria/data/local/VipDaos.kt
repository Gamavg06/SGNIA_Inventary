package com.example.inventariocarniceria.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalProductDao {
    @Query("SELECT * FROM local_products WHERE branchId = :branchId ORDER BY id DESC")
    fun getLocalProductsByBranch(branchId: String): Flow<List<LocalProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLocalProduct(product: LocalProductEntity): Long

    @Update
    fun updateLocalProduct(product: LocalProductEntity)

    @Delete
    fun deleteLocalProduct(product: LocalProductEntity)

    @Query("DELETE FROM local_products")
    suspend fun deleteAllLocalProducts()
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSupplier(supplier: SupplierEntity): Long

    @Delete
    fun deleteSupplier(supplier: SupplierEntity)
}

@Dao
interface BranchDao {
    @Query("SELECT * FROM branches ORDER BY id ASC")
    fun getAllBranches(): Flow<List<BranchEntity>>

    @Query("SELECT * FROM branches ORDER BY id ASC")
    suspend fun getAllBranchesList(): List<BranchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBranch(branch: BranchEntity): Long

    @Delete
    fun deleteBranch(branch: BranchEntity)

    @Query("DELETE FROM branches")
    suspend fun deleteAllBranches()
}
