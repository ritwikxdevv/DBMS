package com.example.campuslostfound.data.firebase

import com.example.campuslostfound.data.model.FoundItem
import com.example.campuslostfound.data.repository.FoundItemRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseFoundItemRepository(
    private val firestore: FirebaseFirestore
) : FoundItemRepository {

    private val foundItemsCollection =
        firestore.collection("found_items")

    override suspend fun createFoundItem(
        item: FoundItem
    ): Result<String> {
        return try {
            val document = foundItemsCollection
                .add(item)
                .await()

            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFoundItem(
        id: String
    ): Result<FoundItem?> {
        return try {
            val snapshot = foundItemsCollection
                .document(id)
                .get()
                .await()

            val item = snapshot.toObject(FoundItem::class.java)

            Result.success(
                item?.copy(id = snapshot.id)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeFoundItems(): Flow<Result<List<FoundItem>>> =
        callbackFlow {

            val registration = foundItemsCollection
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
                            .toObject(FoundItem::class.java)
                            ?.copy(id = document.id)
                    }

                    trySend(Result.success(items))
                }

            awaitClose {
                registration.remove()
            }
        }

    override fun observeUserFoundItems(
        userId: String
    ): Flow<Result<List<FoundItem>>> =
        callbackFlow {

            val registration = foundItemsCollection
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
                            .toObject(FoundItem::class.java)
                            ?.copy(id = document.id)
                    }

                    trySend(Result.success(items))
                }

            awaitClose {
                registration.remove()
            }
        }

    override suspend fun updateFoundItem(
        item: FoundItem
    ): Result<Unit> {
        return try {
            require(item.id.isNotBlank()) {
                "Found item ID cannot be empty."
            }

            foundItemsCollection
                .document(item.id)
                .set(item)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFoundItem(
        id: String
    ): Result<Unit> {
        return try {
            require(id.isNotBlank()) {
                "Found item ID cannot be empty."
            }

            foundItemsCollection
                .document(id)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}