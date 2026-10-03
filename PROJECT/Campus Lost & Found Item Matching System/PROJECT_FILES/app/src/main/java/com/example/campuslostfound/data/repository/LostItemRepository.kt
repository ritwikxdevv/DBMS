package com.example.campuslostfound.data.repository

import com.example.campuslostfound.data.model.LostItem
import kotlinx.coroutines.flow.Flow

interface LostItemRepository {

    suspend fun createLostItem(item: LostItem): Result<String>

    suspend fun getLostItem(id: String): Result<LostItem?>

    fun observeUserLostItems(userId: String): Flow<Result<List<LostItem>>>

    suspend fun updateLostItem(item: LostItem): Result<Unit>

    suspend fun deleteLostItem(id: String): Result<Unit>
}