package com.example.campuslostfound.domain.matcher

import com.example.campuslostfound.data.model.FoundItem
import com.example.campuslostfound.data.model.LostItem
import com.example.campuslostfound.data.model.MatchResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * ItemMatchingEngine
 *
 * Upgraded item matching engine that compares LostItem and FoundItem records using:
 * 1. Item Name Similarity (Weight: 30%)
 * 2. Description Keyword Similarity (Weight: 25%)
 * 3. Category Similarity (Weight: 20%)
 * 4. Location Similarity (Weight: 15%)
 * 5. Date Proximity (Weight: 10%)
 *
 * Employs text normalization, common stop-word filtering, and Jaccard similarity
 * on tokenized keyword sets to compute an explainable score between 0 and 100.
 */
object ItemMatchingEngine {

    // Common English stop-words removed to prevent irrelevant token inflation
    private val STOP_WORDS = setOf(
        "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
        "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being",
        "below", "between", "both", "but", "by", "can", "can't", "cannot", "could",
        "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down",
        "during", "each", "few", "for", "from", "further", "had", "hadn't", "has",
        "hasn't", "have", "haven't", "having", "he", "he'd", "he'll", "he's", "her",
        "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's",
        "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "isn't", "it",
        "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my",
        "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other",
        "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "shan't",
        "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such",
        "than", "that", "that's", "the", "their", "theirs", "them", "themselves",
        "then", "there", "there's", "these", "they", "they'd", "they'll", "they're",
        "they've", "this", "those", "through", "to", "too", "under", "until", "up",
        "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've", "were",
        "weren't", "what", "what's", "when", "when's", "where", "where's", "which",
        "while", "who", "who's", "whom", "why", "why's", "with", "won't", "would",
        "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours",
        "yourself", "yourselves"
    )

    // Category synonyms for campus lost & found
    private val CATEGORY_SYNONYM_GROUPS = listOf(
        setOf("phone", "mobile", "cellphone", "smartphone", "iphone", "android"),
        setOf("laptop", "notebook", "computer", "macbook", "pc", "chromebook"),
        setOf("wallet", "purse", "billfold", "moneyclip", "clutch", "pouch"),
        setOf("id", "id card", "badge", "student id", "license", "card"),
        setOf("earbuds", "headphones", "airpods", "headset", "earphones"),
        setOf("keys", "keychain", "carkey", "key ring"),
        setOf("bottle", "flask", "tumbler", "water bottle", "hydro flask", "sipper"),
        setOf("bag", "backpack", "tote", "duffel", "satchel", "bookbag", "knapsack"),
        setOf("watch", "smartwatch", "apple watch", "fitbit", "band"),
        setOf("clothing", "jacket", "hoodie", "sweater", "coat", "hat", "cap", "scarf"),
        setOf("umbrella", "parasol", "raincoat"),
        setOf("book", "textbook", "notebook", "binder", "folder")
    )

    // Common date patterns for parsing user input
    private val DATE_FORMATS = listOf(
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
        SimpleDateFormat("MMM dd, yyyy", Locale.US),
        SimpleDateFormat("MMM dd", Locale.US),
        SimpleDateFormat("dd MMM yyyy", Locale.US),
        SimpleDateFormat("dd MMM", Locale.US),
        SimpleDateFormat("MM/dd/yyyy", Locale.US),
        SimpleDateFormat("dd/MM/yyyy", Locale.US)
    )

    /**
     * Normalizes text by lowercasing, replacing punctuation, removing stop-words,
     * and filtering out single-character tokens.
     */
    fun normalizeTokens(text: String): Set<String> {
        if (text.isBlank()) return emptySet()
        return text.lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it.length > 1 && it !in STOP_WORDS }
            .toSet()
    }

    /**
     * Computes Jaccard similarity: |A ∩ B| / |A ∪ B|.
     * Enhanced with a 30% subset containment weight to capture exact phrase fragments.
     */
    fun tokenSimilarity(first: Set<String>, second: Set<String>): Double {
        if (first.isEmpty() || second.isEmpty()) return 0.0
        if (first == second) return 1.0

        val intersectionCount = first.count { it in second }
        if (intersectionCount == 0) return 0.0

        val unionSize = (first + second).size
        val jaccard = intersectionCount.toDouble() / unionSize.toDouble()
        val containment = intersectionCount.toDouble() / minOf(first.size, second.size).toDouble()

        return ((jaccard * 0.70) + (containment * 0.30)).coerceIn(0.0, 1.0)
    }

    /**
     * Calculates Category similarity considering exact string match,
     * token similarity, and domain synonym groupings.
     */
    private fun calculateCategorySimilarity(lostCategory: String, foundCategory: String): Double {
        val catA = lostCategory.trim().lowercase(Locale.ROOT)
        val catB = foundCategory.trim().lowercase(Locale.ROOT)

        if (catA.isBlank() || catB.isBlank()) return 0.0
        if (catA == catB) return 1.0

        // Check if one contains the other directly
        if (catA.contains(catB) || catB.contains(catA)) return 0.95

        // Check domain synonym groups
        for (group in CATEGORY_SYNONYM_GROUPS) {
            val inA = group.any { catA.contains(it) || it.contains(catA) }
            val inB = group.any { catB.contains(it) || it.contains(catB) }
            if (inA && inB) return 0.90
        }

        // Fallback to token similarity
        return tokenSimilarity(normalizeTokens(lostCategory), normalizeTokens(foundCategory))
    }

    /**
     * Calculates Location similarity considering exact match, token Jaccard,
     * and campus area containment (e.g. "Library 2nd Floor" vs "Library").
     */
    private fun calculateLocationSimilarity(lostLocation: String, foundLocation: String): Double {
        val locA = lostLocation.trim().lowercase(Locale.ROOT)
        val locB = foundLocation.trim().lowercase(Locale.ROOT)

        if (locA.isBlank() || locB.isBlank()) return 0.0
        if (locA == locB) return 1.0

        if (locA.contains(locB) || locB.contains(locA)) return 0.85

        return tokenSimilarity(normalizeTokens(lostLocation), normalizeTokens(foundLocation))
    }

    /**
     * Calculates Date Proximity (0.0 to 1.0) by computing day difference
     * between lost and found dates or creation timestamps.
     */
    private fun calculateDateProximity(
        lostDateStr: String,
        foundDateStr: String,
        lostCreatedAt: Long,
        foundCreatedAt: Long
    ): Double {
        val lostMillis = parseDateToMillis(lostDateStr) ?: if (lostCreatedAt > 0) lostCreatedAt else null
        val foundMillis = parseDateToMillis(foundDateStr) ?: if (foundCreatedAt > 0) foundCreatedAt else null

        if (lostMillis == null || foundMillis == null) {
            // Check text token overlap if timestamps unavailable
            val tokensA = normalizeTokens(lostDateStr)
            val tokensB = normalizeTokens(foundDateStr)
            return if (tokensA.isNotEmpty() && tokensA == tokensB) 0.80 else 0.30
        }

        val diffDays = abs(lostMillis - foundMillis) / (1000L * 60 * 60 * 24)

        return when {
            diffDays == 0L -> 1.00
            diffDays <= 1L -> 0.90
            diffDays <= 3L -> 0.75
            diffDays <= 7L -> 0.50
            diffDays <= 14L -> 0.30
            diffDays <= 30L -> 0.15
            else -> 0.05
        }
    }

    private fun parseDateToMillis(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        val clean = dateStr.trim()
        for (format in DATE_FORMATS) {
            try {
                val parsed = format.parse(clean)
                if (parsed != null) return parsed.time
            } catch (_: Exception) { }
        }
        return null
    }

    /**
     * Compares a LostItem with a FoundItem using the 5 weighted factors
     * and produces a fully explainable MatchResult.
     */
    fun compareItems(lost: LostItem, found: FoundItem): MatchResult {
        val nameTokensLost = normalizeTokens(lost.itemName)
        val nameTokensFound = normalizeTokens(found.itemName)
        var nameSimilarity = tokenSimilarity(nameTokensLost, nameTokensFound)
        // Direct string match bonus if exact (ignoring case)
        if (lost.itemName.trim().equals(found.itemName.trim(), ignoreCase = true) && lost.itemName.isNotBlank()) {
            nameSimilarity = 1.0
        }

        val descTokensLost = normalizeTokens(lost.description)
        val descTokensFound = normalizeTokens(found.description)
        var descSimilarity = tokenSimilarity(descTokensLost, descTokensFound)
        // Cross-field bonus: if name token from lost item appears in found description
        val crossNameInDesc = nameTokensLost.count { it in descTokensFound }
        if (nameTokensLost.isNotEmpty() && crossNameInDesc > 0) {
            descSimilarity = maxOf(descSimilarity, crossNameInDesc.toDouble() / nameTokensLost.size * 0.75)
        }

        val categorySimilarity = calculateCategorySimilarity(lost.category, found.category)
        val locationSimilarity = calculateLocationSimilarity(lost.location, found.location)
        val dateProximity = calculateDateProximity(
            lostDateStr = lost.dateLost,
            foundDateStr = found.dateFound,
            lostCreatedAt = lost.createdAt,
            foundCreatedAt = found.createdAt
        )

        // Weighted Scoring Formula:
        // Name: 30%, Description: 25%, Category: 20%, Location: 15%, Date: 10%
        val rawScore = (nameSimilarity * 0.30) +
                (descSimilarity * 0.25) +
                (categorySimilarity * 0.20) +
                (locationSimilarity * 0.15) +
                (dateProximity * 0.10)

        val finalScore = (rawScore * 100.0).roundToInt().coerceIn(0, 100)

        // Determine Confidence Level
        val confidence = when {
            finalScore >= 70 -> "HIGH"
            finalScore >= 40 -> "MEDIUM"
            else -> "LOW"
        }

        // Determine Significant Matched Factors
        val matchedFactors = mutableListOf<String>()
        if (nameSimilarity >= 0.35) matchedFactors.add("Item Name (${(nameSimilarity * 100).roundToInt()}%)")
        if (descSimilarity >= 0.25) matchedFactors.add("Description (${(descSimilarity * 100).roundToInt()}%)")
        if (categorySimilarity >= 0.70) matchedFactors.add("Category (${(categorySimilarity * 100).roundToInt()}%)")
        if (locationSimilarity >= 0.40) matchedFactors.add("Location (${(locationSimilarity * 100).roundToInt()}%)")
        if (dateProximity >= 0.70) matchedFactors.add("Date Proximity (${(dateProximity * 100).roundToInt()}%)")

        // Build Human-Readable Explanation
        val explanation = buildExplanation(
            confidence = confidence,
            finalScore = finalScore,
            nameScore = nameSimilarity,
            descScore = descSimilarity,
            catScore = categorySimilarity,
            locScore = locationSimilarity,
            dateScore = dateProximity,
            lost = lost,
            found = found
        )

        return MatchResult(
            lostItemId = lost.id,
            foundItemId = found.id,
            itemName = found.itemName,
            description = found.description,
            category = found.category,
            location = found.location,
            score = finalScore,
            confidence = confidence,
            matchedFactors = matchedFactors,
            explanation = explanation,
            lostItemName = lost.itemName,
            date = found.dateFound
        )
    }

    private fun buildExplanation(
        confidence: String,
        finalScore: Int,
        nameScore: Double,
        descScore: Double,
        catScore: Double,
        locScore: Double,
        dateScore: Double,
        lost: LostItem,
        found: FoundItem
    ): String {
        val parts = mutableListOf<String>()

        if (catScore >= 0.9) {
            parts.add("matching category '${found.category}'")
        }

        if (nameScore >= 0.5) {
            parts.add("strong name overlap ('${lost.itemName}' ↔ '${found.itemName}')")
        } else if (nameScore >= 0.25) {
            parts.add("partial name match")
        }

        if (descScore >= 0.3) {
            parts.add("shared description keywords")
        }

        if (locScore >= 0.8) {
            parts.add("matching location '${found.location}'")
        } else if (locScore >= 0.4) {
            parts.add("nearby location")
        }

        if (dateScore >= 0.9) {
            parts.add("same/adjacent report date")
        } else if (dateScore >= 0.7) {
            parts.add("dates within 3 days")
        }

        val summary = if (parts.isNotEmpty()) parts.joinToString(", ") else "minimal attribute overlap"
        val breakdown = "Breakdown: Name ${(nameScore * 100).roundToInt()}%, Desc ${(descScore * 100).roundToInt()}%, Category ${(catScore * 100).roundToInt()}%, Location ${(locScore * 100).roundToInt()}%, Date ${(dateScore * 100).roundToInt()}%."

        return "$confidence confidence ($finalScore%): $summary. $breakdown"
    }

    /**
     * Cross-compares every LostItem with every FoundItem.
     * Sorts the results by highest score descending.
     */
    fun matchAll(lostItems: List<LostItem>, foundItems: List<FoundItem>): List<MatchResult> {
        if (lostItems.isEmpty() || foundItems.isEmpty()) return emptyList()

        val results = mutableListOf<MatchResult>()
        for (lost in lostItems) {
            for (found in foundItems) {
                results.add(compareItems(lost, found))
            }
        }

        return results.sortedByDescending { it.score }
    }
}
