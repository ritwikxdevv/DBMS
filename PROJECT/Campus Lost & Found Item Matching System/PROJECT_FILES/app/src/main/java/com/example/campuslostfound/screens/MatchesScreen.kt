package com.example.campuslostfound.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campuslostfound.data.model.FoundItem
import com.example.campuslostfound.data.model.LostItem
import com.example.campuslostfound.data.model.MatchResult
import com.example.campuslostfound.domain.matcher.ItemMatchingEngine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun MatchesScreen(
    onBack: () -> Unit
) {
    var matches by remember { mutableStateOf<List<MatchResult>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    // Cache instances — avoid repeated getInstance() calls
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    LaunchedEffect(Unit) {
        val user = auth.currentUser

        if (user == null) {
            message = "Please login first."
            loading = false
            return@LaunchedEffect
        }

        try {
            // Load user's lost items
            val lostSnapshot = db.collection("lost_items")
                .whereEqualTo("userId", user.uid)
                .get()
                .await()

            if (lostSnapshot.isEmpty) {
                message = "No lost item reports found."
                loading = false
                return@LaunchedEffect
            }

            val lostItems = lostSnapshot.documents.map { doc ->
                doc.toObject(LostItem::class.java)?.copy(id = doc.id)
                    ?: LostItem(
                        id = doc.id,
                        itemName = doc.getString("itemName") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        location = doc.getString("location") ?: "",
                        dateLost = doc.getString("dateLost") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
            }

            if (lostItems.isEmpty()) {
                message = "Could not load lost items."
                loading = false
                return@LaunchedEffect
            }

            // Load all found items
            val foundSnapshot = db.collection("found_items")
                .get()
                .await()

            val foundItems = foundSnapshot.documents.map { doc ->
                doc.toObject(FoundItem::class.java)?.copy(id = doc.id)
                    ?: FoundItem(
                        id = doc.id,
                        itemName = doc.getString("itemName") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        location = doc.getString("location") ?: "",
                        dateFound = doc.getString("dateFound") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
            }

            // Move CPU-heavy 5-factor scoring to Dispatchers.Default (off UI thread)
            val results = withContext(Dispatchers.Default) {
                ItemMatchingEngine.matchAll(lostItems, foundItems)
            }

            matches = results
            loading = false

        } catch (e: Exception) {
            message = e.message ?: "Failed to load items."
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Possible Matches")

        when {
            loading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Finding possible matches...")
                }
            }

            message.isNotBlank() -> {
                Text(message)
            }

            matches.isEmpty() -> {
                Text("No matching items found.")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = matches,
                        key = { index, match -> "match_${match.foundItemId}_${match.lostItemId}_$index" }
                    ) { _, match ->

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Found: ${match.itemName}")
                                Text("Category: ${match.category}")
                                Text("Location: ${match.location}")
                                Text("Description: ${match.description}")
                                Text("Match Score: ${match.score}% [${match.confidence} Confidence]")

                                if (match.matchedFactors.isNotEmpty()) {
                                    Text("Matched Factors: ${match.matchedFactors.joinToString(", ")}")
                                }

                                if (match.explanation.isNotBlank()) {
                                    Text("Explanation: ${match.explanation}")
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}