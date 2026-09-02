package com.example.expensetracker.util

import com.example.expensetracker.viewmodel.ParsedReceipt
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Pure, stateless parser for OCR text extracted from a receipt image.
 *
 * All methods are intentionally free of Android dependencies so they can be
 * covered fully by JVM unit tests.
 */
object ReceiptParser {

    // ── Category keyword map ──────────────────────────────────────────────

    private val categoryKeywords: Map<String, List<String>> = mapOf(
        "Food" to listOf(
            "restaurant", "cafe", "coffee", "pizza", "burger", "sushi",
            "food", "bakery", "grill", "bar", "hotel", "eat", "java",
            "cappuccino", "sandwich", "din"
        ),
        "Transport" to listOf(
            "petrol", "fuel", "parking", "uber", "bolt", "taxi",
            "matatu", "bus", "stage", "travel", "transport", "vehicle"
        ),
        "Health" to listOf(
            "pharmacy", "clinic", "hospital", "chemist", "medical",
            "dental", "optical", "doctor", "lab", "health"
        ),
        "Shopping" to listOf(
            "supermarket", "mall", "store", "shop", "retail",
            "market", "fashion", "clothing", "shoes"
        ),
        "Bills" to listOf(
            "electricity", "water", "rent", "internet", "wifi",
            "bill", "utility", "power", "kplc", "safaricom"
        ),
        "Entertainment" to listOf(
            "cinema", "movie", "concert", "event", "ticket",
            "netflix", "spotify", "game", "entertainment"
        ),
        "Education" to listOf(
            "school", "college", "university", "tuition", "fee",
            "book", "stationery", "exam", "course", "training"
        )
    )

    // ── Date format patterns ──────────────────────────────────────────────

    private val dateFormats = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()),
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
        SimpleDateFormat("MMM dd yyyy", Locale.getDefault()),
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    ).also { list -> list.forEach { it.isLenient = false } }

    // Date regex: matches common date-like token sequences
    private val dateRegex = Regex(
        """\b(\d{1,2}[/\-\.]\d{1,2}[/\-\.]\d{2,4}|\d{4}[/\-\.]\d{2}[/\-\.]\d{2}|""" +
            """\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+\d{2,4}|""" +
            """(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\.?\s+\d{1,2},?\s+\d{4})\b""",
        RegexOption.IGNORE_CASE
    )

    // Total line regex — matches "total: 1,234.56" or "total 1234" etc.
    private val totalLineRegex = Regex(
        """(?i)(?:total|amount\s*due|balance\s*due|grand\s*total|net\s*total)""" +
            """\s*[:\-]?\s*(?:ksh\.?|kes\.?|ugx\.?|tzs\.?)?\s*([\d,]+(?:\.\d{1,2})?)"""
    )

    // Any numeric value on a line
    private val numericRegex = Regex(
        """(?:ksh\.?|kes\.?)?\s*([\d,]+(?:\.\d{1,2})?)"""
    )

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Parse OCR [rawText] and return a best-effort [ParsedReceipt].
     * Never throws; all fields are nullable.
     */
    fun parse(rawText: String): ParsedReceipt {

        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val merchantName = extractMerchantName(lines)
        val amountCents = extractAmount(lines)
        val date = extractDate(lines)
        val suggestedCategory = suggestCategory(rawText)

        return ParsedReceipt(
            amountCents = amountCents,
            merchantName = merchantName,
            date = date,
            suggestedCategory = suggestedCategory
        )
    }

    // ── Private helpers ───────────────────────────────────────────────────

    internal fun extractMerchantName(lines: List<String>): String? =
        lines.firstOrNull()?.takeIf { it.length >= 2 }

    internal fun extractAmount(lines: List<String>): Long? {

        // 1. Try keyword-based extraction first (most reliable)
        for (line in lines) {

            val match = totalLineRegex.find(line) ?: continue

            val parsed = parseDecimalToCents(match.groupValues[1])

            if (parsed != null && parsed > 0) return parsed
        }

        // 2. Fallback: largest number in the entire text
        var largest = 0L

        for (line in lines) {

            numericRegex.findAll(line).forEach { match ->

                val value = parseDecimalToCents(match.groupValues[1]) ?: 0L

                if (value > largest) largest = value
            }
        }

        return if (largest > 0) largest else null
    }

    internal fun extractDate(lines: List<String>): Long? {

        val fullText = lines.joinToString(" ")

        val candidates = dateRegex.findAll(fullText)
            .map { it.value.trim() }
            .toList()

        for (candidate in candidates) {

            for (fmt in dateFormats) {

                try {
                    val parsed = fmt.parse(candidate)
                    if (parsed != null) return parsed.time
                } catch (_: Exception) { /* try next format */ }
            }
        }

        return null
    }

    internal fun suggestCategory(rawText: String): String? {

        val lower = rawText.lowercase()

        return categoryKeywords.entries
            .maxByOrNull { (_, keywords) ->
                keywords.count { lower.contains(it) }
            }
            ?.takeIf { (_, keywords) ->
                keywords.any { lower.contains(it) }
            }
            ?.key
    }

    // Converts "1,234.56" or "1234" to Long cents (multiply by 100)
    private fun parseDecimalToCents(raw: String): Long? {

        val cleaned = raw.replace(",", "").trim()

        val value = cleaned.toDoubleOrNull() ?: return null

        return (value * 100).toLong()
    }
}
