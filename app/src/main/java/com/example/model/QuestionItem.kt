package com.example.model

data class QuestionItem(
    val id: String,
    val questionText: String,
    val answer: String, // "OUI" or "NON"
    val askedByTeamIndex: Int, // 0 for Team/Player A, 1 for Team/Player B / Robot
    val askedByTeamName: String,
    val validationStatus: ValidationStatus = ValidationStatus.UNKNOWN,
    val validationNote: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
