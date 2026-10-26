package com.example.inventariocarniceria.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleTicketDao {
    @Query("SELECT * FROM sale_tickets ORDER BY id DESC")
    fun getAllTickets(): Flow<List<SaleTicketEntity>>

    @Query("SELECT * FROM sale_tickets WHERE status = 'COMPLETADA' ORDER BY id DESC")
    fun getActiveSales(): Flow<List<SaleTicketEntity>>

    @Query("SELECT * FROM sale_tickets WHERE operationNumber = :query OR ticketBarcode = :query LIMIT 1")
    suspend fun findExactTicket(query: String): SaleTicketEntity?

    @Query("SELECT * FROM sale_tickets WHERE operationNumber LIKE '%' || :query || '%' OR ticketBarcode LIKE '%' || :query || '%' OR productName LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchTickets(query: String): Flow<List<SaleTicketEntity>>

    @Query("SELECT * FROM sale_tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: Long): SaleTicketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SaleTicketEntity): Long

    @Update
    suspend fun updateTicket(ticket: SaleTicketEntity)

    @Query("DELETE FROM sale_tickets WHERE id = :id")
    suspend fun deleteTicket(id: Long)

    @Query("DELETE FROM sale_tickets")
    suspend fun clearAllTickets()
}
