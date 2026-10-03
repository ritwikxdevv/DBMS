package com.example.campuslostfound.domain.usecase

import com.example.campuslostfound.data.model.LostItem
import com.example.campuslostfound.data.repository.LostItemRepository

class CreateLostItemUseCase(
    private val repository: LostItemRepository
) {

    suspend operator fun invoke(
        item: LostItem
    ): Result<String> {
        if (item.itemName.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Item name is required.")
            )
        }

        if (item.description.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Description is required.")
            )
        }

        if (item.category.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Category is required.")
            )
        }

        if (item.location.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Location is required.")
            )
        }

        if (item.dateLost.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Date lost is required.")
            )
        }

        return repository.createLostItem(item)
    }
}