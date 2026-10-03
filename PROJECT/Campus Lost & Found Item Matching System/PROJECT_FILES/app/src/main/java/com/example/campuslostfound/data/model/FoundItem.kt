package com.example.campuslostfound.data.model

data class FoundItem(
    val id: String = "",
    val userId: String = "",
    val itemName: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val dateFound: String = "",
    val imageUri: String? = null,
    val imageUrl: String? = null,
    val imageLabels: List<String> = emptyList(),
    val status: String = "found",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)