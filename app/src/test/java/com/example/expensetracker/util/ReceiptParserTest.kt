package com.example.expensetracker.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ReceiptParserTest {

    // ── Merchant name ─────────────────────────────────────────────────────

    @Test
    fun `extracts first non-blank line as merchant name`() {

        val receipt = """
            Java House
            Tel: 0712345678
            Total: 450.00
        """.trimIndent()

        val result = ReceiptParser.parse(receipt)

        assertEquals("Java House", result.merchantName)
    }

    @Test
    fun `merchant name is null for empty text`() {

        val result = ReceiptParser.parse("")
        assertNull(result.merchantName)
    }

    // ── Amount extraction ─────────────────────────────────────────────────

    @Test
    fun `extracts total from keyword line`() {

        val receipt = """
            Quick Mart
            Bread        80.00
            Milk        120.00
            Total: 200.00
        """.trimIndent()

        val result = ReceiptParser.parse(receipt)

        assertEquals(20_000L, result.amountCents) // 200.00 → 20000 cents
    }

    @Test
    fun `extracts total with 'Amount Due' keyword`() {

        val receipt = """
            Coffee Shop
            Latte  350
            Amount Due: 350.00
        """.trimIndent()

        val result = ReceiptParser.parse(receipt)

        assertEquals(35_000L, result.amountCents)
    }

    @Test
    fun `falls back to largest number when no keyword found`() {

        val receipt = """
            ABC Store
            Item A  100
            Item B  250
            Item C  50
        """.trimIndent()

        val result = ReceiptParser.parse(receipt)

        assertEquals(25_000L, result.amountCents) // 250.00 is the largest
    }

    @Test
    fun `handles comma-separated amounts`() {

        val receipt = """
            Naivas Supermarket
            Grand Total: 1,234.56
        """.trimIndent()

        val result = ReceiptParser.parse(receipt)

        assertEquals(123_456L, result.amountCents)
    }

    @Test
    fun `amount is null when no numbers found`() {

        val result = ReceiptParser.parse("Store Name\nThank you for shopping!")
        assertNull(result.amountCents)
    }

    // ── Date extraction ───────────────────────────────────────────────────

    @Test
    fun `parses dd-MM-yyyy date format`() {

        val receipt = """
            Clinic Plus
            Date: 15-08-2025
            Total: 500
        """.trimIndent()

        val result = ReceiptParser.parse(receipt)

        assertNotNull(result.date)

        val fmt = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        fmt.isLenient = false
        val expected = fmt.parse("15-08-2025")!!.time
        assertEquals(expected, result.date)
    }

    @Test
    fun `parses dd MMM yyyy date format`() {

        val receipt = "Receipt\n20 Sep 2025\nTotal: 1200"

        val result = ReceiptParser.parse(receipt)

        assertNotNull(result.date)
    }

    @Test
    fun `date is null when no recognisable date found`() {

        val result = ReceiptParser.parse("Store\nNo date here\nTotal: 100")
        assertNull(result.date)
    }

    // ── Category suggestion ───────────────────────────────────────────────

    @Test
    fun `suggests Food for restaurant keywords`() {

        val receipt = "The Grillhouse Restaurant\nTotal: 1500"
        val result = ReceiptParser.parse(receipt)
        assertEquals("Food", result.suggestedCategory)
    }

    @Test
    fun `suggests Health for pharmacy keywords`() {

        val receipt = "City Pharmacy\nMedicine 200\nTotal: 200"
        val result = ReceiptParser.parse(receipt)
        assertEquals("Health", result.suggestedCategory)
    }

    @Test
    fun `suggests Transport for fuel keywords`() {

        val receipt = "Shell Petrol Station\nFuel: 3000\nTotal: 3000"
        val result = ReceiptParser.parse(receipt)
        assertEquals("Transport", result.suggestedCategory)
    }

    @Test
    fun `category is null when no keywords match`() {

        val result = ReceiptParser.parse("XYZ Ltd\nRef: 12345\nTotal: 999")
        assertNull(result.suggestedCategory)
    }

    // ── Full receipt round-trip ───────────────────────────────────────────

    @Test
    fun `parses a realistic receipt correctly`() {

        val receipt = """
            Java House Karen
            Date: 02/09/2025
            Cappuccino         350.00
            Club sandwich      850.00
            Grand Total:     1,200.00
            Thank you!
        """.trimIndent()

        val result = ReceiptParser.parse(receipt)

        assertEquals("Java House Karen", result.merchantName)
        assertEquals(120_000L, result.amountCents) // 1200.00
        assertNotNull(result.date)
        assertEquals("Food", result.suggestedCategory)
    }
}
