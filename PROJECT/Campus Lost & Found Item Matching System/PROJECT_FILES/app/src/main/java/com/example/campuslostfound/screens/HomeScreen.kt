package com.example.campuslostfound.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Unified Data Model for Real Campus Items
private data class CampusFeedItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val date: String = "",
    val type: String = "", // "Lost" or "Found"
    val imageUrl: String? = null,
    val createdAt: Long = 0L
)

// Premium Visual Design Tokens
private val PrimaryBlue = Color(0xFF1E5AFF)
private val PrimaryBlueDark = Color(0xFF0F3BB3)
private val PrimaryBlueLight = Color(0xFFEFF4FF)
private val PrimaryBlueBorder = Color(0xFFDBE5FF)

private val LostAmber = Color(0xFFEA580C)
private val LostAmberDark = Color(0xFF9A3412)
private val LostAmberBg = Color(0xFFFFF7ED)
private val LostAmberBorder = Color(0xFFFFEDD5)

private val FoundEmerald = Color(0xFF059669)
private val FoundEmeraldDark = Color(0xFF065F46)
private val FoundEmeraldBg = Color(0xFFECFDF5)
private val FoundEmeraldBorder = Color(0xFFA7F3D0)

private val SlateDark = Color(0xFF0F172A)
private val SlateMedium = Color(0xFF475569)
private val SlateLight = Color(0xFF94A3B8)
private val ScreenBackground = Color(0xFFF8FAFC)
private val CardBackground = Color(0xFFFFFFFF)
private val CardBorderColor = Color(0xFFE2E8F0)

// Elegant Vector Canvas Icons (Zero external dependency required)
@Composable
private fun SearchVectorIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val center = Offset(size.width * 0.42f, size.height * 0.42f)
        val radius = size.width * 0.28f
        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
        val startHandle = Offset(center.x + radius * 0.707f, center.y + radius * 0.707f)
        val endHandle = Offset(size.width * 0.88f, size.height * 0.88f)
        drawLine(
            color = tint,
            start = startHandle,
            end = endHandle,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun HomeVectorIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.45f)
            lineTo(size.width * 0.5f, size.height * 0.16f)
            lineTo(size.width * 0.85f, size.height * 0.45f)
            lineTo(size.width * 0.85f, size.height * 0.85f)
            lineTo(size.width * 0.15f, size.height * 0.85f)
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        val doorPath = Path().apply {
            moveTo(size.width * 0.4f, size.height * 0.85f)
            lineTo(size.width * 0.4f, size.height * 0.58f)
            lineTo(size.width * 0.6f, size.height * 0.58f)
            lineTo(size.width * 0.6f, size.height * 0.85f)
        }
        drawPath(path = doorPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }
}

@Composable
private fun AddVectorIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.2.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.2f),
            end = Offset(size.width * 0.5f, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.2f, size.height * 0.5f),
            end = Offset(size.width * 0.8f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PersonVectorIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawCircle(
            color = tint,
            radius = size.width * 0.22f,
            center = Offset(size.width * 0.5f, size.height * 0.32f),
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.18f, size.height * 0.54f),
            size = Size(size.width * 0.64f, size.height * 0.42f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun BellVectorIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.9.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.72f)
            lineTo(size.width * 0.78f, size.height * 0.72f)
            lineTo(size.width * 0.72f, size.height * 0.58f)
            lineTo(size.width * 0.72f, size.height * 0.4f)
            cubicTo(
                size.width * 0.72f, size.height * 0.22f,
                size.width * 0.28f, size.height * 0.22f,
                size.width * 0.28f, size.height * 0.4f
            )
            lineTo(size.width * 0.28f, size.height * 0.58f)
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        drawCircle(
            color = tint,
            radius = size.width * 0.08f,
            center = Offset(size.width * 0.5f, size.height * 0.83f)
        )
    }
}

@Composable
private fun LocationPinVectorIcon(tint: Color, modifier: Modifier = Modifier.size(13.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.9f)
            cubicTo(
                size.width * 0.3f, size.height * 0.6f,
                size.width * 0.15f, size.height * 0.45f,
                size.width * 0.15f, size.height * 0.35f
            )
            cubicTo(
                size.width * 0.15f, size.height * 0.16f,
                size.width * 0.85f, size.height * 0.16f,
                size.width * 0.85f, size.height * 0.35f
            )
            cubicTo(
                size.width * 0.85f, size.height * 0.45f,
                size.width * 0.7f, size.height * 0.6f,
                size.width * 0.5f, size.height * 0.9f
            )
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        drawCircle(
            color = tint,
            radius = size.width * 0.14f,
            center = Offset(size.width * 0.5f, size.height * 0.36f)
        )
    }
}

@Composable
private fun CalendarVectorIcon(tint: Color, modifier: Modifier = Modifier.size(13.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.7.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.15f, size.height * 0.25f),
            size = Size(size.width * 0.7f, size.height * 0.65f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.15f, size.height * 0.45f),
            end = Offset(size.width * 0.85f, size.height * 0.45f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.32f, size.height * 0.12f),
            end = Offset(size.width * 0.32f, size.height * 0.27f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.68f, size.height * 0.12f),
            end = Offset(size.width * 0.68f, size.height * 0.27f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun WarningVectorIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.14f)
            lineTo(size.width * 0.88f, size.height * 0.84f)
            lineTo(size.width * 0.12f, size.height * 0.84f)
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.38f),
            end = Offset(size.width * 0.5f, size.height * 0.6f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = tint,
            radius = size.width * 0.05f,
            center = Offset(size.width * 0.5f, size.height * 0.72f)
        )
    }
}

@Composable
private fun CloseVectorIcon(tint: Color, modifier: Modifier = Modifier.size(16.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(size.width * 0.25f, size.height * 0.25f),
            end = Offset(size.width * 0.75f, size.height * 0.75f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.75f, size.height * 0.25f),
            end = Offset(size.width * 0.25f, size.height * 0.75f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun LogoutVectorIcon(tint: Color, modifier: Modifier = Modifier.size(16.dp)) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.9.dp.toPx()
        val bracket = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.2f)
            lineTo(size.width * 0.2f, size.height * 0.2f)
            lineTo(size.width * 0.2f, size.height * 0.8f)
            lineTo(size.width * 0.45f, size.height * 0.8f)
        }
        drawPath(path = bracket, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(
            color = tint,
            start = Offset(size.width * 0.4f, size.height * 0.5f),
            end = Offset(size.width * 0.84f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        val arrowHead = Path().apply {
            moveTo(size.width * 0.65f, size.height * 0.32f)
            lineTo(size.width * 0.84f, size.height * 0.5f)
            lineTo(size.width * 0.65f, size.height * 0.68f)
        }
        drawPath(path = arrowHead, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun TagVectorIcon(tint: Color, modifier: Modifier = Modifier.size(14.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.5f)
            lineTo(size.width * 0.5f, size.height * 0.15f)
            lineTo(size.width * 0.85f, size.height * 0.15f)
            lineTo(size.width * 0.85f, size.height * 0.5f)
            lineTo(size.width * 0.5f, size.height * 0.85f)
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = stroke, join = StrokeJoin.Round))
        drawCircle(
            color = tint,
            radius = size.width * 0.08f,
            center = Offset(size.width * 0.7f, size.height * 0.3f)
        )
    }
}

// Helpers for Firestore item mapping and category icons
private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0L) return ""
    return try {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) {
        ""
    }
}

private fun mapLostDocument(doc: DocumentSnapshot): CampusFeedItem {
    val dateStr = doc.getString("dateLost")?.ifBlank { null }
        ?: doc.getString("date")?.ifBlank { null }
        ?: formatTimestamp(doc.getLong("createdAt"))
    return CampusFeedItem(
        id = doc.id,
        name = doc.getString("itemName") ?: doc.getString("name") ?: "Unnamed Lost Item",
        description = doc.getString("description") ?: "",
        category = doc.getString("category") ?: "General",
        location = doc.getString("location") ?: "Campus",
        date = dateStr,
        type = "Lost",
        imageUrl = doc.getString("imageUri") ?: doc.getString("imageUrl"),
        createdAt = doc.getLong("createdAt") ?: 0L
    )
}

private fun mapFoundDocument(doc: DocumentSnapshot): CampusFeedItem {
    val dateStr = doc.getString("dateFound")?.ifBlank { null }
        ?: doc.getString("date")?.ifBlank { null }
        ?: formatTimestamp(doc.getLong("createdAt"))
    return CampusFeedItem(
        id = doc.id,
        name = doc.getString("itemName") ?: doc.getString("name") ?: "Unnamed Found Item",
        description = doc.getString("description") ?: "",
        category = doc.getString("category") ?: "General",
        location = doc.getString("location") ?: "Campus",
        date = dateStr,
        type = "Found",
        imageUrl = doc.getString("imageUri") ?: doc.getString("imageUrl"),
        createdAt = doc.getLong("createdAt") ?: 0L
    )
}

private fun getCategoryEmoji(category: String): String {
    val cat = category.lowercase().trim()
    return when {
        cat.contains("wallet") || cat.contains("purse") -> "👛"
        cat.contains("umbrella") -> "☂️"
        cat.contains("id") || cat.contains("card") || cat.contains("badge") -> "🪪"
        cat.contains("book") || cat.contains("notebook") -> "📚"
        cat.contains("phone") || cat.contains("mobile") -> "📱"
        cat.contains("laptop") || cat.contains("macbook") -> "💻"
        cat.contains("headphone") || cat.contains("earbud") || cat.contains("airpod") -> "🎧"
        cat.contains("key") -> "🔑"
        cat.contains("bag") || cat.contains("backpack") -> "🎒"
        cat.contains("bottle") || cat.contains("flask") -> "🍶"
        cat.contains("watch") -> "⌚"
        cat.contains("cloth") || cat.contains("jacket") || cat.contains("hoodie") -> "🧥"
        else -> "📦"
    }
}

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onReportLost: () -> Unit,
    onReportFound: () -> Unit,
    onFindMatches: () -> Unit
) {
    // Cache Firebase instances
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // Real Firestore Feed State
    var lostItems by remember { mutableStateOf<List<CampusFeedItem>>(emptyList()) }
    var foundItems by remember { mutableStateOf<List<CampusFeedItem>>(emptyList()) }
    var isLoadingFeed by remember { mutableStateOf(true) }

    // Live Snapshot Listeners to existing Firestore collections
    DisposableEffect(Unit) {
        val lostRegistration = db.collection("lost_items")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    lostItems = snapshot.documents.map { mapLostDocument(it) }
                }
                isLoadingFeed = false
            }

        val foundRegistration = db.collection("found_items")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    foundItems = snapshot.documents.map { mapFoundDocument(it) }
                }
                isLoadingFeed = false
            }

        onDispose {
            lostRegistration.remove()
            foundRegistration.remove()
        }
    }

    // Combined real dataset sorted newest first
    val allItems = remember(lostItems, foundItems) {
        (lostItems + foundItems).sortedByDescending { it.createdAt }
    }

    // Filter and Search State
    // Status filter: "All", "Lost", "Found"
    var selectedTypeFilter by remember { mutableStateOf("All") }
    // Category filter: "All", or specific category name
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    // Search query
    var searchQuery by remember { mutableStateOf("") }

    // Dynamic categories extracted from existing Firebase reports
    val availableCategories = remember(allItems) {
        val fromData = allItems.map { it.category.trim() }
            .filter { it.isNotBlank() && !it.equals("General", ignoreCase = true) }
            .distinct()
            .sorted()

        if (fromData.isEmpty()) {
            listOf("Electronics", "Wallet", "Keys", "ID Card", "Bags")
        } else {
            fromData
        }
    }

    // Real Search & Filter algorithm matching: item name, description, category, location
    val displayedItems = remember(allItems, searchQuery, selectedTypeFilter, selectedCategoryFilter) {
        val query = searchQuery.trim().lowercase()
        allItems.filter { item ->
            // 1. Status Filter: All, Lost, Found
            val matchesType = when (selectedTypeFilter) {
                "Lost" -> item.type.equals("Lost", ignoreCase = true)
                "Found" -> item.type.equals("Found", ignoreCase = true)
                else -> true
            }

            // 2. Category Filter: All or specific category
            val matchesCategory = if (selectedCategoryFilter.equals("All", ignoreCase = true)) {
                true
            } else {
                item.category.trim().equals(selectedCategoryFilter.trim(), ignoreCase = true)
            }

            // 3. Real Search across Name, Description, Category, Location
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                item.name.lowercase().contains(query) ||
                item.description.lowercase().contains(query) ||
                item.category.lowercase().contains(query) ||
                item.location.lowercase().contains(query)
            }

            matchesType && matchesCategory && matchesQuery
        }
    }

    val isFilterActive = searchQuery.isNotBlank() || selectedTypeFilter != "All" || selectedCategoryFilter != "All"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- Header ----------
            item(key = "header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PrimaryBlueLight,
                            border = BorderStroke(1.dp, PrimaryBlueBorder),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🔎", fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Campus",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SlateDark
                                )
                                Text(
                                    text = "Find",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue
                                )
                            }
                            Text(
                                text = "Lost something? Find it here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMedium
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Notification Bell
                        Box(contentAlignment = Alignment.TopEnd) {
                            Surface(
                                shape = CircleShape,
                                color = CardBackground,
                                border = BorderStroke(1.dp, CardBorderColor),
                                shadowElevation = 1.dp,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    BellVectorIcon(tint = SlateMedium, modifier = Modifier.size(20.dp))
                                }
                            }
                            Surface(
                                shape = CircleShape,
                                color = LostAmber,
                                modifier = Modifier
                                    .padding(top = 2.dp, end = 2.dp)
                                    .size(9.dp)
                            ) {}
                        }

                        // Quick Scan Button
                        Button(
                            onClick = onFindMatches,
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SearchVectorIcon(tint = Color.White, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "Scan",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // ---------- Hero Match Banner ----------
            item(key = "hero_banner") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF1E3A8A),
                                        Color(0xFF1E5AFF),
                                        Color(0xFF3B82F6)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "⚡ AUTOMATED MATCHING",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Lost or Found an item?",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Our campus algorithm scans recent reports to help you find owners or retrieve items quickly.",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = onFindMatches,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = PrimaryBlueDark
                                ),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Check Matches Now →",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // ---------- Real Search Bar ----------
            item(key = "search") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CardBackground,
                    border = BorderStroke(1.dp, CardBorderColor),
                    shadowElevation = 2.dp
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Search by item name, description, category, location...",
                                color = SlateLight,
                                fontSize = 13.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            SearchVectorIcon(
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = searchQuery.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    CloseVectorIcon(
                                        tint = SlateLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground
                        )
                    )
                }
            }

            // ---------- Filter Chips (Status: All, Lost, Found) ----------
            item(key = "status_filters") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: All / Lost / Found Primary Type Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // All Chip
                        val isAllSelected = selectedTypeFilter == "All"
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = if (isAllSelected) PrimaryBlue else CardBackground,
                            border = BorderStroke(
                                1.dp,
                                if (isAllSelected) PrimaryBlue else CardBorderColor
                            ),
                            shadowElevation = if (isAllSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50.dp))
                                .clickable { selectedTypeFilter = "All" }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "All",
                                    fontSize = 13.sp,
                                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAllSelected) Color.White else SlateMedium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = if (isAllSelected) Color.White.copy(alpha = 0.25f) else PrimaryBlueLight
                                ) {
                                    Text(
                                        text = "${allItems.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAllSelected) Color.White else PrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Lost Chip
                        val isLostSelected = selectedTypeFilter == "Lost"
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = if (isLostSelected) LostAmber else CardBackground,
                            border = BorderStroke(
                                1.dp,
                                if (isLostSelected) LostAmber else CardBorderColor
                            ),
                            shadowElevation = if (isLostSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50.dp))
                                .clickable {
                                    selectedTypeFilter = if (isLostSelected) "All" else "Lost"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lost",
                                    fontSize = 13.sp,
                                    fontWeight = if (isLostSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isLostSelected) Color.White else LostAmberDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = if (isLostSelected) Color.White.copy(alpha = 0.25f) else LostAmberBg
                                ) {
                                    Text(
                                        text = "${lostItems.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLostSelected) Color.White else LostAmberDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Found Chip
                        val isFoundSelected = selectedTypeFilter == "Found"
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = if (isFoundSelected) FoundEmerald else CardBackground,
                            border = BorderStroke(
                                1.dp,
                                if (isFoundSelected) FoundEmerald else CardBorderColor
                            ),
                            shadowElevation = if (isFoundSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50.dp))
                                .clickable {
                                    selectedTypeFilter = if (isFoundSelected) "All" else "Found"
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 9.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Found",
                                    fontSize = 13.sp,
                                    fontWeight = if (isFoundSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isFoundSelected) Color.White else FoundEmeraldDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = if (isFoundSelected) Color.White.copy(alpha = 0.25f) else FoundEmeraldBg
                                ) {
                                    Text(
                                        text = "${foundItems.size}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFoundSelected) Color.White else FoundEmeraldDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Row 2: Category-Based Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // All Categories Chip
                        item {
                            val isAllCatSelected = selectedCategoryFilter.equals("All", ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = if (isAllCatSelected) PrimaryBlueLight else CardBackground,
                                border = BorderStroke(
                                    1.dp,
                                    if (isAllCatSelected) PrimaryBlue else CardBorderColor
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .clickable { selectedCategoryFilter = "All" }
                            ) {
                                Text(
                                    text = "All Categories",
                                    fontSize = 12.sp,
                                    fontWeight = if (isAllCatSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isAllCatSelected) PrimaryBlue else SlateMedium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }

                        // Dynamic Category Chips
                        items(availableCategories) { category ->
                            val isSelected = selectedCategoryFilter.equals(category, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(50.dp),
                                color = if (isSelected) PrimaryBlue else CardBackground,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) PrimaryBlue else CardBorderColor
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .clickable {
                                        selectedCategoryFilter = if (isSelected) "All" else category
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = getCategoryEmoji(category),
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = category,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SlateMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ---------- Report an Item Section ----------
            item(key = "report_title") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Report an Item",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    Text(
                        text = "Fast campus submission",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateLight
                    )
                }
            }

            item(key = "report_buttons") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Lost Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp)),
                        color = LostAmberBg,
                        border = BorderStroke(1.dp, LostAmberBorder),
                        shadowElevation = 1.dp,
                        onClick = onReportLost
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    WarningVectorIcon(
                                        tint = LostAmber,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Report Lost",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = LostAmberDark
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Lost an item? Post it so finders reach you",
                                style = MaterialTheme.typography.bodySmall,
                                color = LostAmberDark.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                maxLines = 2
                            )
                        }
                    }

                    // Found Button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp)),
                        color = PrimaryBlueLight,
                        border = BorderStroke(1.dp, PrimaryBlueBorder),
                        shadowElevation = 1.dp,
                        onClick = onReportFound
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AddVectorIcon(
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Report Found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = PrimaryBlueDark
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Found an item? Help return to owner",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryBlueDark.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // ---------- Recent Reports Header ----------
            item(key = "recent_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Campus Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = PrimaryBlueLight
                        ) {
                            Text(
                                text = "${displayedItems.size}",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (isFilterActive) {
                        TextButton(onClick = {
                            searchQuery = ""
                            selectedTypeFilter = "All"
                            selectedCategoryFilter = "All"
                        }) {
                            Text(
                                text = "Clear Filters ✕",
                                color = LostAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        TextButton(onClick = onFindMatches) {
                            Text(
                                text = "View Matches →",
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ---------- Loading Feed State ----------
            if (isLoadingFeed && allItems.isEmpty()) {
                item(key = "loading_state") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading campus reports from Firebase...",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMedium
                            )
                        }
                    }
                }
            }

            // ---------- Empty State ----------
            if (!isLoadingFeed && displayedItems.isEmpty()) {
                item(key = "empty_state") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorderColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlueLight,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    SearchVectorIcon(
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = if (isFilterActive) "No matching items found" else "No reports yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (isFilterActive) {
                                    "No items match your search query or filter chips. Try broadening your terms or reset the filters."
                                } else {
                                    "There are currently no lost or found items recorded in the campus database."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMedium,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isFilterActive) {
                                OutlinedButton(
                                    onClick = {
                                        selectedTypeFilter = "All"
                                        selectedCategoryFilter = "All"
                                        searchQuery = ""
                                    },
                                    shape = RoundedCornerShape(50.dp),
                                    border = BorderStroke(1.dp, PrimaryBlue),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                                ) {
                                    Text("Reset Filters", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // ---------- Real Firestore Item Cards ----------
            itemsIndexed(
                items = displayedItems,
                key = { index, item -> "feed_${item.type}_${item.id.ifBlank { item.name }}_$index" }
            ) { _, item ->
                val isLost = item.type.equals("Lost", ignoreCase = true)
                val badgeBg = if (isLost) LostAmberBg else FoundEmeraldBg
                val badgeBorder = if (isLost) LostAmberBorder else FoundEmeraldBorder
                val badgeText = if (isLost) LostAmberDark else FoundEmeraldDark
                val thumbBg = if (isLost) LostAmberBg else PrimaryBlueLight

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                color = thumbBg
                            ) {
                                if (!item.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = getCategoryEmoji(item.category),
                                            fontSize = 28.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.category.ifBlank { "Item" },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = SlateMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateDark,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(50.dp),
                                        color = badgeBg,
                                        border = BorderStroke(1.dp, badgeBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(badgeText, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = item.type.uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = badgeText
                                            )
                                        }
                                    }
                                }

                                if (item.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 17.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    if (item.location.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            LocationPinVectorIcon(
                                                tint = PrimaryBlue,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = item.location,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SlateMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (item.date.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CalendarVectorIcon(
                                                tint = SlateLight,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = item.date,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SlateMedium,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            color = CardBorderColor.copy(alpha = 0.6f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TagVectorIcon(tint = SlateLight, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = item.category.ifBlank { "General" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateLight,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = onFindMatches,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlueLight,
                                    contentColor = PrimaryBlue
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) {
                                Text(
                                    text = "View Match →",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // ---------- Logout Card ----------
            item(key = "logout") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlueLight,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    PersonVectorIcon(
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = auth.currentUser?.email ?: "Campus Student",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = SlateDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Signed In",
                                    fontSize = 11.sp,
                                    color = SlateLight
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                auth.signOut()
                                onLogout()
                            },
                            shape = RoundedCornerShape(50.dp),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFDC2626)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            LogoutVectorIcon(
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Logout",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // ---------- Modern Bottom Navigation Bar ----------
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBackground,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        HomeVectorIcon(
                            tint = PrimaryBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Home",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlueLight,
                        unselectedIconColor = SlateLight,
                        unselectedTextColor = SlateLight
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onFindMatches,
                    icon = {
                        SearchVectorIcon(
                            tint = SlateLight,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Matches",
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlueLight,
                        unselectedIconColor = SlateLight,
                        unselectedTextColor = SlateLight
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onReportFound,
                    icon = {
                        AddVectorIcon(
                            tint = SlateLight,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Report",
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlueLight,
                        unselectedIconColor = SlateLight,
                        unselectedTextColor = SlateLight
                    )
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {
                        auth.signOut()
                        onLogout()
                    },
                    icon = {
                        PersonVectorIcon(
                            tint = SlateLight,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Profile",
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlueLight,
                        unselectedIconColor = SlateLight,
                        unselectedTextColor = SlateLight
                    )
                )
            }
        }
    }
}