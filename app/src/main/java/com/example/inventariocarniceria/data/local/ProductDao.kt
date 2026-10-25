package com.example.inventariocarniceria.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY localCode ASC, code ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY localCode ASC, code ASC")
    suspend fun getAllProductsList(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE code = :targetCode OR localCode = :targetCode LIMIT 1")
    fun getProductByCode(targetCode: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE CAST(code AS TEXT) LIKE '%' || :query || '%' OR CAST(localCode AS TEXT) LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' OR synonyms LIKE '%' || :query || '%' ORDER BY localCode ASC")
    fun searchProductsHybrid(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE parentProductCode = :parentCode ORDER BY code ASC")
    fun getDerivatives(parentCode: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE parentProductCode = :parentCode ORDER BY code ASC")
    suspend fun getDerivativesList(parentCode: Int): List<ProductEntity>

    @Query("UPDATE products SET parentProductCode = :parentCode WHERE code = :childCode")
    suspend fun updateParentCode(childCode: Int, parentCode: Int?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProducts(products: List<ProductEntity>)

    @Update
    fun updateProduct(product: ProductEntity)

    @Delete
    fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
