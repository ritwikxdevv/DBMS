package com.example.campuslostfound.data.firebase

import com.example.campuslostfound.data.model.LostItem
import com.example.campuslostfound.data.repository.LostItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseLostItemRepository(
    private val firestore: FirebaseFirestore
) : LostItemRepository {

    private val lostItemsCollection =
        firestore.collection("lost_items")

    override suspend fun createLostItem(
        item: LostItem
    ): Result<String> {
        return try {
            val document = lostItemsCollection
                .add(item)
                .await()

            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLostItem(
        id: String
    ): Result<LostItem?> {
        return try {
            val snapshot = lostItemsCollection
                .document(id)
                .get()
                .await()

            val item = snapshot.toObject(LostItem::class.java)

            Result.success(
                item?.copy(id = snapshot.id)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUserLostItems(
        userId: String
    ): Flow<Result<List<LostItem>>> = callbackFlow {

        val registration = lostItemsCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(
                        Result.failure(
                            IllegalStateException(
                                "Firestore returned no snapshot."
                            )
                        )
                    )
                    return@addSnapshotListener
                }

                val items = snapshot.documents.mapNotNull { document ->
                    document
                        .toObject(LostItem::class.java)
                        ?.copy(id = document.id)
                }

                trySend(Result.success(items))
            }

        awaitClose {
            registration.remove()
        }
    }

    override suspend fun updateLostItem(
        item: LostItem
    ): Result<Unit> {
        return try {
            require(item.id.isNotBlank()) {
                "Lost item ID cannot be empty."
            }

            lostItemsCollection
                .document(item.id)
                .set(item)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLostItem(
        id: String
    ): Result<Unit> {
        return try {
            require(id.isNotBlank()) {
                "Lost item ID cannot be empty."
            }

            lostItemsCollection
                .document(id)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}