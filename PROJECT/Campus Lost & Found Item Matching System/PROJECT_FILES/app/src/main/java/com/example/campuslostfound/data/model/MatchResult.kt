package com.example.campuslostfound.data.model

data class MatchResult(
    val lostItemId: String = "",
    val foundItemId: String = "",
    val itemName: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val score: Int = 0,
    val confidence: String = "LOW", // "HIGH", "MEDIUM", "LOW"
    val matchedFactors: List<String> = emptyList(),
    val explanation: String = "",
    val lostItemName: String = "",
    val date: String = ""
)