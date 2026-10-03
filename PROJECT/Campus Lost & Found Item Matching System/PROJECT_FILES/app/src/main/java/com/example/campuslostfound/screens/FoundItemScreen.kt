package com.example.campuslostfound.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Modern Campus Theme Design Tokens
private val PrimaryBlue = Color(0xFF1E5AFF)
private val PrimaryBlueDark = Color(0xFF0F3BB3)
private val PrimaryBlueLight = Color(0xFFEFF4FF)
private val PrimaryBlueBorder = Color(0xFFDBE5FF)
private val TextDark = Color(0xFF0F172A)
private val TextMedium = Color(0xFF475569)
private val TextMuted = Color(0xFF64748B)
private val ScreenBg = Color(0xFFF8FAFC)
private val CardBorderColor = Color(0xFFE2E8F0)
private val AsteriskRed = Color(0xFFEF4444)
private val ErrorBg = Color(0xFFFEF2F2)
private val ErrorBorder = Color(0xFFFECACA)
private val ErrorText = Color(0xFFDC2626)
private val SuccessEmerald = Color(0xFF059669)
private val SuccessBg = Color(0xFFECFDF5)
private val SuccessBorder = Color(0xFFA7F3D0)

@Composable
fun FoundItemScreen(
    onSubmitted: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dateFound by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Cache Firebase instances to avoid repeated getInstance() calls
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScreenBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                // Category Pill Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryBlueLight,
                    border = BorderStroke(1.dp, PrimaryBlueBorder),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(PrimaryBlue, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CAMPUS RECOVERY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Text(
                    text = "Report Found Item",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Provide details of the item you discovered to help reconnect it with its rightful owner.",
                    fontSize = 14.sp,
                    color = TextMedium,
                    lineHeight = 20.sp
                )
            }

            // SECTION 1: Item Details Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionHeader(
                        title = "Item Details",
                        subtitle = "Key identifiers and physical attributes",
                        icon = { ItemDetailsVectorIcon(tint = PrimaryBlue) }
                    )

                    // Item Name
                    Column {
                        FieldLabel(label = "Item Name", isRequired = true)
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Blue Hydro Flask, Apple AirPods Case", color = TextMuted, fontSize = 14.sp) },
                            leadingIcon = { TagVectorIcon(tint = PrimaryBlue, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = modernTextFieldColors(),
                            enabled = !isSubmitting
                        )
                        FieldHelperText("Specify the item type, brand, or dominant color")
                    }

                    // Category
                    Column {
                        FieldLabel(label = "Category", isRequired = true)
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Electronics, Bags, Keys, IDs", color = TextMuted, fontSize = 14.sp) },
                            leadingIcon = { CategoryVectorIcon(tint = PrimaryBlue, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = modernTextFieldColors(),
                            enabled = !isSubmitting
                        )
                        FieldHelperText("Helps classify the found item for matching searches")
                    }

                    // Description
                    Column {
                        FieldLabel(label = "Description", isRequired = true)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Mention visible scratches, stickers, condition, where it was placed...", color = TextMuted, fontSize = 14.sp) },
                            leadingIcon = { DescriptionVectorIcon(tint = PrimaryBlue, modifier = Modifier.size(18.dp)) },
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(14.dp),
                            colors = modernTextFieldColors(),
                            enabled = !isSubmitting
                        )
                        FieldHelperText("Avoid sensitive identifiers that only the true owner would know")
                    }
                }
            }

            // SECTION 2: Location & Date Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionHeader(
                        title = "Location & Date",
                        subtitle = "Where and when the item was discovered",
                        icon = { LocationPinVectorIcon(tint = PrimaryBlue) }
                    )

                    // Location Found
                    Column {
                        FieldLabel(label = "Location Found", isRequired = true)
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Science Complex 2nd Floor, Room 204", color = TextMuted, fontSize = 14.sp) },
                            leadingIcon = { LocationPinVectorIcon(tint = PrimaryBlue, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = modernTextFieldColors(),
                            enabled = !isSubmitting
                        )
                        FieldHelperText("Campus building, classroom, bench, or specific area found")
                    }

                    // Date Found
                    Column {
                        FieldLabel(label = "Date Found", isRequired = true)
                        OutlinedTextField(
                            value = dateFound,
                            onValueChange = { dateFound = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("e.g. Today ~11:00 AM, or Oct 24, 2026", color = TextMuted, fontSize = 14.sp) },
                            leadingIcon = { CalendarVectorIcon(tint = PrimaryBlue, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = modernTextFieldColors(),
                            enabled = !isSubmitting
                        )
                        FieldHelperText("Approximate date and time you discovered or picked up the item")
                    }
                }
            }

            // SECTION 3: Photo Upload & Preview Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionHeader(
                        title = "Item Photo",
                        subtitle = "Add a photo to drastically speed up recognition",
                        icon = { CameraVectorIcon(tint = PrimaryBlue) }
                    )

                    if (selectedImageUri != null) {
                        // Image Preview Box
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(ScreenBg)
                            ) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Selected found item photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Photo Tag Pill
                                Surface(
                                    color = Color(0xCC0F172A),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Photo Selected",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { imagePicker.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, PrimaryBlueBorder),
                                    enabled = !isSubmitting
                                ) {
                                    Text("Change Photo", color = PrimaryBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }

                                TextButton(
                                    onClick = { selectedImageUri = null },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isSubmitting
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CloseVectorIcon(tint = ErrorText, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Remove", color = ErrorText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty Photo Upload Area
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PrimaryBlueLight,
                            border = BorderStroke(1.5.dp, PrimaryBlueBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isSubmitting) {
                                    imagePicker.launch("image/*")
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(Color.White, CircleShape)
                                        .border(BorderStroke(1.dp, PrimaryBlueBorder), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    UploadVectorIcon(tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Upload Item Photo",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "PNG, JPG or WEBP from gallery (Optional)",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedButton(
                                    onClick = { imagePicker.launch("image/*") },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, PrimaryBlue),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                    enabled = !isSubmitting
                                ) {
                                    Text(
                                        text = "Browse Device",
                                        color = PrimaryBlue,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: Contact Information Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SectionHeader(
                        title = "Contact Information",
                        subtitle = "How students or campus staff can reach you",
                        icon = { PersonVectorIcon(tint = PrimaryBlue) }
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ScreenBg,
                        border = BorderStroke(1.dp, CardBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PrimaryBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                PersonVectorIcon(tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = auth.currentUser?.email ?: "Campus Student Account",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(SuccessEmerald, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Verified Campus Member",
                                        fontSize = 11.sp,
                                        color = SuccessEmerald,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        InfoVectorIcon(
                            tint = TextMuted,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your contact info will be shared securely with owners or campus lost & found staff to coordinate returns safely.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Message Banner (Feedback / Error / Success)
            if (message.isNotBlank()) {
                val isSuccess = message.contains("success", ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSuccess) SuccessBg else ErrorBg,
                    border = BorderStroke(1.dp, if (isSuccess) SuccessBorder else ErrorBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSuccess) {
                            CheckVectorIcon(tint = SuccessEmerald, modifier = Modifier.size(20.dp))
                        } else {
                            WarningVectorIcon(tint = ErrorText, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSuccess) "Notice" else "Attention",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) SuccessEmerald else ErrorText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = message,
                                fontSize = 12.sp,
                                color = if (isSuccess) SuccessEmerald else ErrorText,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Full-Width Submit Button
            Button(
                onClick = {
                    val user = auth.currentUser

                    if (user == null) {
                        message = "Please login first."
                        return@Button
                    }

                    if (
                        itemName.isBlank() ||
                        description.isBlank() ||
                        category.isBlank() ||
                        location.isBlank() ||
                        dateFound.isBlank()
                    ) {
                        message = "Please fill all fields."
                        return@Button
                    }

                    isSubmitting = true

                    val foundItem = hashMapOf(
                        "itemName" to itemName.trim(),
                        "description" to description.trim(),
                        "category" to category.trim(),
                        "location" to location.trim(),
                        "dateFound" to dateFound.trim(),
                        "userId" to user.uid,
                        "status" to "found",
                        "createdAt" to System.currentTimeMillis(),
                        "hasPhoto" to (selectedImageUri != null)
                    )

                    db.collection("found_items")
                        .add(foundItem)
                        .addOnSuccessListener {
                            isSubmitting = false
                            message = "Found item reported successfully!"
                            onSubmitted()
                        }
                        .addOnFailureListener {
                            isSubmitting = false
                            message = "Failed to save report."
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.6f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Submitting Report...",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CheckVectorIcon(tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Found Item Report",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Reusable Helper Composables
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PrimaryBlueLight, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun FieldLabel(label: String, isRequired: Boolean) {
    Row(
        modifier = Modifier.padding(bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
        if (isRequired) {
            Text(
                text = " *",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AsteriskRed
            )
        }
    }
}

@Composable
private fun FieldHelperText(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = TextMuted,
        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
    )
}

@Composable
private fun modernTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = CardBorderColor,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = ScreenBg,
    focusedTextColor = TextDark,
    unfocusedTextColor = TextDark,
    cursorColor = PrimaryBlue
)

// Standalone Vector Canvas Icons
@Composable
private fun ItemDetailsVectorIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
            size = Size(size.width * 0.7f, size.height * 0.7f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = stroke)
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.3f, size.height * 0.4f),
            end = Offset(size.width * 0.7f, size.height * 0.4f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.3f, size.height * 0.6f),
            end = Offset(size.width * 0.55f, size.height * 0.6f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun TagVectorIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
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

@Composable
private fun CategoryVectorIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val squareSize = size.width * 0.32f
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.12f, size.height * 0.12f),
            size = Size(squareSize, squareSize),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.56f, size.height * 0.12f),
            size = Size(squareSize, squareSize),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.12f, size.height * 0.56f),
            size = Size(squareSize, squareSize),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.56f, size.height * 0.56f),
            size = Size(squareSize, squareSize),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )
    }
}

@Composable
private fun DescriptionVectorIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.18f, size.height * 0.12f),
            size = Size(size.width * 0.64f, size.height * 0.76f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = stroke)
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.32f, size.height * 0.32f),
            end = Offset(size.width * 0.68f, size.height * 0.32f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.32f, size.height * 0.5f),
            end = Offset(size.width * 0.68f, size.height * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.32f, size.height * 0.68f),
            end = Offset(size.width * 0.52f, size.height * 0.68f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun LocationPinVectorIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.92f)
            cubicTo(
                size.width * 0.3f, size.height * 0.62f,
                size.width * 0.14f, size.height * 0.46f,
                size.width * 0.14f, size.height * 0.35f
            )
            cubicTo(
                size.width * 0.14f, size.height * 0.16f,
                size.width * 0.86f, size.height * 0.16f,
                size.width * 0.86f, size.height * 0.35f
            )
            cubicTo(
                size.width * 0.86f, size.height * 0.46f,
                size.width * 0.7f, size.height * 0.62f,
                size.width * 0.5f, size.height * 0.92f
            )
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = stroke, join = StrokeJoin.Round))
        drawCircle(
            color = tint,
            radius = size.width * 0.14f,
            center = Offset(size.width * 0.5f, size.height * 0.36f)
        )
    }
}

@Composable
private fun CalendarVectorIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.14f, size.height * 0.22f),
            size = Size(size.width * 0.72f, size.height * 0.68f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = stroke)
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.14f, size.height * 0.42f),
            end = Offset(size.width * 0.86f, size.height * 0.42f),
            strokeWidth = stroke
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.32f, size.height * 0.1f),
            end = Offset(size.width * 0.32f, size.height * 0.26f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.68f, size.height * 0.1f),
            end = Offset(size.width * 0.68f, size.height * 0.26f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun CameraVectorIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val body = Path().apply {
            moveTo(size.width * 0.14f, size.height * 0.35f)
            lineTo(size.width * 0.3f, size.height * 0.35f)
            lineTo(size.width * 0.38f, size.height * 0.22f)
            lineTo(size.width * 0.62f, size.height * 0.22f)
            lineTo(size.width * 0.7f, size.height * 0.35f)
            lineTo(size.width * 0.86f, size.height * 0.35f)
            lineTo(size.width * 0.86f, size.height * 0.82f)
            lineTo(size.width * 0.14f, size.height * 0.82f)
            close()
        }
        drawPath(path = body, color = tint, style = Stroke(width = stroke, join = StrokeJoin.Round))
        drawCircle(
            color = tint,
            radius = size.width * 0.16f,
            center = Offset(size.width * 0.5f, size.height * 0.56f),
            style = Stroke(width = stroke)
        )
    }
}

@Composable
private fun UploadVectorIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.65f),
            end = Offset(size.width * 0.5f, size.height * 0.2f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        val arrow = Path().apply {
            moveTo(size.width * 0.32f, size.height * 0.38f)
            lineTo(size.width * 0.5f, size.height * 0.2f)
            lineTo(size.width * 0.68f, size.height * 0.38f)
        }
        drawPath(path = arrow, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val tray = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.6f)
            lineTo(size.width * 0.22f, size.height * 0.8f)
            lineTo(size.width * 0.78f, size.height * 0.8f)
            lineTo(size.width * 0.78f, size.height * 0.6f)
        }
        drawPath(path = tray, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
private fun PersonVectorIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        drawCircle(
            color = tint,
            radius = size.width * 0.22f,
            center = Offset(size.width * 0.5f, size.height * 0.32f),
            style = Stroke(width = stroke)
        )
        drawArc(
            color = tint,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(size.width * 0.16f, size.height * 0.54f),
            size = Size(size.width * 0.68f, size.height * 0.44f),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun InfoVectorIcon(tint: Color, modifier: Modifier = Modifier.size(16.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        drawCircle(
            color = tint,
            radius = size.width * 0.42f,
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            style = Stroke(width = stroke)
        )
        drawCircle(
            color = tint,
            radius = size.width * 0.05f,
            center = Offset(size.width * 0.5f, size.height * 0.32f)
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.46f),
            end = Offset(size.width * 0.5f, size.height * 0.7f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun WarningVectorIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.14f)
            lineTo(size.width * 0.88f, size.height * 0.86f)
            lineTo(size.width * 0.12f, size.height * 0.86f)
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = stroke, join = StrokeJoin.Round))
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.4f),
            end = Offset(size.width * 0.5f, size.height * 0.62f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = tint,
            radius = size.width * 0.05f,
            center = Offset(size.width * 0.5f, size.height * 0.74f)
        )
    }
}

@Composable
private fun CloseVectorIcon(tint: Color, modifier: Modifier = Modifier.size(14.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(size.width * 0.2f, size.height * 0.2f),
            end = Offset(size.width * 0.8f, size.height * 0.8f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.8f, size.height * 0.2f),
            end = Offset(size.width * 0.2f, size.height * 0.8f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun CheckVectorIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier = modifier) {
        val stroke = 2.2.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.2f, size.height * 0.52f)
            lineTo(size.width * 0.42f, size.height * 0.74f)
            lineTo(size.width * 0.82f, size.height * 0.28f)
        }
        drawPath(path = path, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}