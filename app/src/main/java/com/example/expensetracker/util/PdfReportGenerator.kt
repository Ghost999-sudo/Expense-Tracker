package com.example.expensetracker.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.expensetracker.viewmodel.ReportUiState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportGenerator(
    private val context: Context
) {

    private val pageWidth = 595
    private val pageHeight = 842
    private val marginLeft = 40f
    private val marginRight = 555f
    private val footerY = 810f
    private val maxContentY = 800f

    fun generateReport(
        report: ReportUiState,
        reportTitle: String = "Expense Report"
    ): File {

        val document = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(
            pageWidth, pageHeight, 1
        ).create()

        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = 50f

        val basePaint = Paint().apply {
            isAntiAlias = true
        }

        val titlePaint = Paint(basePaint).apply {
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
        }

        val headingPaint = Paint(basePaint).apply {
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }

        val normalPaint = Paint(basePaint).apply {
            textSize = 12f
        }

        val smallPaint = Paint(basePaint).apply {
            textSize = 10f
        }

        val amountPaint = Paint(normalPaint).apply {
            textAlign = Paint.Align.RIGHT
        }

        val dividerPaint = Paint(basePaint).apply {
            strokeWidth = 1f
        }

        fun drawFooter() {

            canvas.drawText(
                "Expense Tracker", marginLeft, footerY,
                smallPaint
            )

            canvas.drawText(
                "Generated automatically", marginRight,
                footerY, amountPaint
            )
        }

        fun requireSpace(needed: Float) {

            if (y + needed > maxContentY) {

                drawFooter()
                document.finishPage(page)

                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }
        }

        val dateFormat = SimpleDateFormat(
            "dd MMM yyyy, HH:mm", Locale.getDefault()
        )

        val rangeFormat = SimpleDateFormat(
            "dd MMM yyyy", Locale.getDefault()
        )

        canvas.drawText(
            reportTitle, marginLeft, y, titlePaint
        )

        y += 30f

        canvas.drawText(
            "Generated: ${dateFormat.format(Date())}",
            marginLeft, y, smallPaint
        )

        y += 18f

        canvas.drawText(
            "Period: ${report.period.label}" +
                " (${rangeFormat.format(Date(report.startDate))} - " +
                "${rangeFormat.format(Date(report.endDate))})",
            marginLeft, y, smallPaint
        )

        y += 22f

        canvas.drawLine(
            marginLeft, y, marginRight, y, dividerPaint
        )

        y += 25f

        requireSpace(80f)

        canvas.drawText(
            "Summary", marginLeft, y, headingPaint
        )

        y += 26f

        canvas.drawText(
            "Total Spending: ${formatAmount(report.total)}",
            marginLeft, y, normalPaint
        )

        y += 22f

        canvas.drawText(
            "Transactions: ${report.transactionCount}",
            marginLeft, y, normalPaint
        )

        y += 22f

        canvas.drawText(
            "Average Expense: " +
                formatAmount(report.averageExpense.toLong()),
            marginLeft, y, normalPaint
        )

        y += 25f

        requireSpace(60f)

        canvas.drawLine(
            marginLeft, y, marginRight, y, dividerPaint
        )

        y += 25f

        canvas.drawText(
            "Spending by Category",
            marginLeft, y, headingPaint
        )

        y += 24f

        canvas.drawText(
            "Category", marginLeft + 4f, y, normalPaint
        )

        canvas.drawText(
            "Amount", marginRight, y, amountPaint
        )

        y += 6f

        canvas.drawLine(
            marginLeft, y, marginRight, y, dividerPaint
        )

        y += 16f

        if (report.categories.isEmpty()) {

            requireSpace(30f)

            canvas.drawText(
                "No spending recorded for this period.",
                marginLeft + 4f, y, normalPaint
            )

            y += 24f

        } else {

            report.categories.forEach { categoryTotal ->

                requireSpace(24f)

                canvas.drawText(
                    categoryTotal.category,
                    marginLeft + 4f, y, normalPaint
                )

                canvas.drawText(
                    formatAmount(categoryTotal.total),
                    marginRight, y, amountPaint
                )

                y += 22f
            }

            requireSpace(24f)
            canvas.drawLine(
                marginLeft, y, marginRight, y, dividerPaint
            )
            y += 22f

            canvas.drawText(
                "Total", marginLeft + 4f, y, headingPaint
            )

            canvas.drawText(
                formatAmount(report.total),
                marginRight, y, amountPaint
            )
        }

        drawFooter()
        document.finishPage(page)

        val reportsDirectory = File(
            context.cacheDir, "expense_reports"
        )

        if (!reportsDirectory.exists()) {
            reportsDirectory.mkdirs()
        }

        val timestamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(Date())

        val file = File(
            reportsDirectory,
            "Expense_Report_$timestamp.pdf"
        )

        FileOutputStream(file).use { stream ->
            document.writeTo(stream)
        }

        document.close()

        return file
    }

    private fun formatAmount(
        amount: Long
    ): String {

        return String.format(
            Locale.US,
            "KSh %,.2f",
            amount / 100.0
        )
    }
}