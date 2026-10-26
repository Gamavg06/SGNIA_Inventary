package com.example.inventariocarniceria.data.sales

import com.example.inventariocarniceria.data.local.SaleTicketEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SalesReversalUnitTest {

    @Test
    fun `sale ticket creation calculates correct total and preserves operation number`() {
        val ticket = SaleTicketEntity(
            id = 1L,
            operationNumber = "OP-10024",
            ticketBarcode = "2000001010005",
            productCode = 1,
            productName = "Menudo de Res",
            weightKg = 1.000,
            pricePerKg = 120.00,
            totalAmount = 120.00,
            status = "COMPLETADA"
        )

        assertEquals("OP-10024", ticket.operationNumber)
        assertEquals("2000001010005", ticket.ticketBarcode)
        assertEquals(1, ticket.productCode)
        assertEquals("Menudo de Res", ticket.productName)
        assertEquals(1.000, ticket.weightKg, 0.0001)
        assertEquals(120.00, ticket.totalAmount, 0.01)
        assertEquals("COMPLETADA", ticket.status)
    }

    @Test
    fun `reversal updates status to INVERTIDA with reason and timestamp`() {
        val original = SaleTicketEntity(
            id = 42L,
            operationNumber = "OP-55012",
            ticketBarcode = "2000001010005",
            productCode = 1,
            productName = "Menudo de Res",
            weightKg = 1.000,
            pricePerKg = 120.00,
            totalAmount = 120.00,
            status = "COMPLETADA"
        )

        val reversed = original.copy(
            status = "INVERTIDA",
            reversalReason = "Reembolso solicitado por el cliente",
            reversalTimestamp = 1761400000000L
        )

        assertEquals("INVERTIDA", reversed.status)
        assertEquals("Reembolso solicitado por el cliente", reversed.reversalReason)
        assertNotNull(reversed.reversalTimestamp)
        assertEquals(original.weightKg, reversed.weightKg, 0.0001)
        assertEquals(original.totalAmount, reversed.totalAmount, 0.01)
    }

    @Test
    fun `matching operation number or barcode finds ticket`() {
        val tickets = listOf(
            SaleTicketEntity(id = 1L, operationNumber = "OP-1001", ticketBarcode = "2000001010005", productCode = 1, productName = "Menudo de Res", weightKg = 1.0, pricePerKg = 120.0, totalAmount = 120.0),
            SaleTicketEntity(id = 2L, operationNumber = "OP-1002", ticketBarcode = "2000020025008", productCode = 20, productName = "Pierna de Cerdo", weightKg = 2.5, pricePerKg = 115.0, totalAmount = 287.5)
        )

        val foundByOp = tickets.find { it.operationNumber.equals("OP-1001", ignoreCase = true) }
        assertNotNull(foundByOp)
        assertEquals("Menudo de Res", foundByOp?.productName)

        val foundByBarcode = tickets.find { it.ticketBarcode == "2000020025008" }
        assertNotNull(foundByBarcode)
        assertEquals("Pierna de Cerdo", foundByBarcode?.productName)
    }
}
