package com.example.expensetracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.util.DateUtils
import com.example.expensetracker.util.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    val expenses: StateFlow<List<ExpenseEntity>> =
        repository.allExpenses.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _searchQuery =
        MutableStateFlow("")

    val searchQuery:
        StateFlow<String> =
        _searchQuery.asStateFlow()

    fun updateSearchQuery(
        query: String
    ) {

        _searchQuery.value = query
    }

    val filteredExpenses: StateFlow<List<ExpenseEntity>> =
        combine(
            expenses,
            _searchQuery
        ) { expenseList, query ->

            if (query.isBlank()) {

                expenseList

            } else {

                expenseList.filter { expense ->

                    expense.category
                        .contains(
                            query,
                            ignoreCase = true
                        ) ||

                    expense.description
                        .contains(
                            query,
                            ignoreCase = true
                        )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val totalExpenses: StateFlow<Long> =
        repository.totalExpenses
            .map { it ?: 0L }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0L
            )

    val todayExpenses: StateFlow<Long> =
        repository.getExpensesBetweenDates(
            startDate = DateUtils.startOfDay(),
            endDate = DateUtils.endOfDay()
        )
            .map { it ?: 0L }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0L
            )

    private val _selectedPeriod =
        MutableStateFlow(ReportPeriod.default)

    val selectedPeriod:
        StateFlow<ReportPeriod> =
        _selectedPeriod.asStateFlow()

    fun selectPeriod(
        period: ReportPeriod
    ) {

        _selectedPeriod.value = period
    }

    private val _customStartDate =
        MutableStateFlow(DateUtils.startOfMonth())

    private val _customEndDate =
        MutableStateFlow(DateUtils.endOfDay())

    fun setCustomRange(
        startDate: Long,
        endDate: Long
    ) {

        _customStartDate.value = startDate

        _customEndDate.value = endDate

        _selectedPeriod.value = ReportPeriod.CUSTOM
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val reportUiState: StateFlow<ReportUiState> =
        combine(
            _selectedPeriod,
            _customStartDate,
            _customEndDate
        ) { period, customStart, customEnd ->

            Triple(period, customStart, customEnd)

        }.flatMapLatest { (period, customStart, customEnd) ->

            val range =
                resolveRange(period, customStart, customEnd)

            combine(
                repository.getExpensesBetweenDates(range.first, range.second),
                repository.getExpenseCount(range.first, range.second),
                repository.getAverageExpense(range.first, range.second),
                repository.getCategoryTotals(range.first, range.second)
            ) { total, count, average, categories ->

                ReportUiState(
                    period = period,
                    startDate = range.first,
                    endDate = range.second,
                    total = total ?: 0L,
                    transactionCount = count,
                    averageExpense = average ?: 0.0,
                    categories = categories,
                    isLoading = false
                )
            }

        }.catch { throwable ->

            emit(
                ReportUiState(
                    error = throwable.message
                        ?: "Failed to load report"
                )
            )

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReportUiState(isLoading = true)
        )

    fun addExpense(
        amount: Long,
        category: String,
        description: String,
        date: Long
    ) {
        val expense = ExpenseEntity(
            amount = amount,
            category = category,
            description = description,
            date = date
        )

        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    suspend fun getExpenseById(id: Int): ExpenseEntity? {
        return repository.getExpenseById(id)
    }

    private val _pdfUiState =
        MutableStateFlow(PdfUiState())

    val pdfUiState: StateFlow<PdfUiState> =
        _pdfUiState.asStateFlow()

    fun generatePdfReport(
        context: Context,
        report: ReportUiState,
        reportTitle: String = "Expense Report"
    ) {

        viewModelScope.launch {

            _pdfUiState.value =
                PdfUiState(
                    isGenerating = true
                )

            try {

                val file =
                    withContext(Dispatchers.IO) {

                        val generator =
                            PdfReportGenerator(context)

                        generator.generateReport(
                            report = report,
                            reportTitle = reportTitle
                        )
                    }

                _pdfUiState.value =
                    PdfUiState(
                        isGenerating = false,
                        file = file
                    )

            } catch (exception: Exception) {

                exception.printStackTrace()

                _pdfUiState.value =
                    PdfUiState(
                        isGenerating = false,
                        error =
                            exception.message
                                ?: "Unable to generate PDF"
                    )
            }
        }
    }

    fun clearPdfState() {

        _pdfUiState.value = PdfUiState()
    }

    private fun resolveRange(
        period: ReportPeriod,
        customStart: Long,
        customEnd: Long
    ): Pair<Long, Long> {

        return when (period) {

            ReportPeriod.TODAY ->
                DateUtils.startOfDay() to DateUtils.endOfDay()

            ReportPeriod.WEEK ->
                DateUtils.startOfWeek() to DateUtils.endOfWeek()

            ReportPeriod.MONTH ->
                DateUtils.startOfMonth() to DateUtils.endOfMonth()

            ReportPeriod.YEAR ->
                DateUtils.startOfYear() to DateUtils.endOfYear()

            ReportPeriod.CUSTOM ->
                customStart to customEnd
        }
    }
}
