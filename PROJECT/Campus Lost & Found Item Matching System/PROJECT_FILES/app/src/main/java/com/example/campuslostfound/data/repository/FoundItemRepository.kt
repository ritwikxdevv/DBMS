package com.example.campuslostfound.data.repository

import com.example.campuslostfound.data.model.FoundItem
import kotlinx.coroutines.flow.Flow

interface FoundItemRepository {

    suspend fun createFoundItem(item: FoundItem): Result<String>

    suspend fun getFoundItem(id: String): Result<FoundItem?>

    fun observeFoundItems(): Flow<Result<List<FoundItem>>>

    fun observeUserFoundItems(userId: String): Flow<Result<List<FoundItem>>>

    suspend fun updateFoundItem(item: FoundItem): Result<Unit>

    suspend fun deleteFoundItem(id: String): Result<Unit>
}