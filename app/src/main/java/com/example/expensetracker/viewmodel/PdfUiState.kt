package com.example.expensetracker.viewmodel

import java.io.File

data class PdfUiState(
    val isGenerating: Boolean = false,
    val file: File? = null,
    val error: String? = null
)