package com.example.campuslostfound

import com.example.campuslostfound.data.model.FoundItem
import com.example.campuslostfound.data.model.LostItem
import com.example.campuslostfound.domain.matcher.ItemMatchingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemMatchingEngineTest {

    @Test
    fun testTextNormalizationAndStopWordsRemoval() {
        val raw = "A blue hydro flask with my stickers on it!"
        val tokens = ItemMatchingEngine.normalizeTokens(raw)
        // Stop words "a", "with", "my", "on", "it" should be removed
        assertTrue("blue should be kept", "blue" in tokens)
        assertTrue("hydro should be kept", "hydro" in tokens)
        assertTrue("flask should be kept", "flask" in tokens)
        assertTrue("stickers should be kept", "stickers" in tokens)
        assertTrue("'with' stopword should be removed", "with" !in tokens)
        assertTrue("'my' stopword should be removed", "my" !in tokens)
        assertTrue("'on' stopword should be removed", "on" !in tokens)
        assertTrue("'it' stopword should be removed", "it" !in tokens)
    }

    @Test
    fun testHighConfidenceMatch() {
        val lost = LostItem(
            id = "lost_1",
            itemName = "Apple MacBook Pro 14",
            description = "Space gray laptop with Python sticker near trackpad",
            category = "Electronics",
            location = "Central Library 2nd Floor",
            dateLost = "2026-10-15",
            createdAt = 1760500000000L
        )

        val found = FoundItem(
            id = "found_1",
            itemName = "MacBook Pro 14 inch",
            description = "Space gray Apple laptop found on table with sticker",
            category = "Electronics",
            location = "Central Library",
            dateFound = "2026-10-15",
            createdAt = 1760505000000L
        )

        val result = ItemMatchingEngine.compareItems(lost, found)

        assertTrue("Score should be high (>= 70), got: ${result.score}", result.score >= 70)
        assertEquals("HIGH", result.confidence)
        assertTrue("Should contain Item Name factor", result.matchedFactors.any { it.contains("Item Name") })
        assertTrue("Should contain Category factor", result.matchedFactors.any { it.contains("Category") })
        assertTrue("Explanation should be informative", result.explanation.isNotBlank())
    }

    @Test
    fun testLowConfidenceMatchForDifferentItems() {
        val lost = LostItem(
            id = "lost_wallet",
            itemName = "Brown Leather Wallet",
            description = "Contains campus ID and student debit card",
            category = "Wallet",
            location = "Main Dining Hall",
            dateLost = "2026-09-01",
            createdAt = 1756700000000L
        )

        val found = FoundItem(
            id = "found_keys",
            itemName = "Toyota Car Keys",
            description = "Single key with black remote fob",
            category = "Keys",
            location = "Parking Lot B",
            dateFound = "2026-10-20",
            createdAt = 1760900000000L
        )

        val result = ItemMatchingEngine.compareItems(lost, found)

        assertTrue("Score should be low (< 40), got: ${result.score}", result.score < 40)
        assertEquals("LOW", result.confidence)
    }

    @Test
    fun testMatchAllSorting() {
        val lost = LostItem(
            id = "lost_umbrella",
            itemName = "Black Foldable Umbrella",
            description = "Small rain umbrella with curved wooden handle",
            category = "Umbrella",
            location = "Science Building Entrance",
            dateLost = "2026-10-10",
            createdAt = 1760100000000L
        )

        val foundPoor = FoundItem(
            id = "found_keys",
            itemName = "Silver Door Key",
            description = "Dorm room key",
            category = "Keys",
            location = "Cafeteria",
            dateFound = "2026-09-01",
            createdAt = 1756700000000L
        )

        val foundGood = FoundItem(
            id = "found_umbrella",
            itemName = "Black Umbrella",
            description = "Foldable umbrella left by entrance",
            category = "Umbrella",
            location = "Science Building",
            dateFound = "2026-10-10",
            createdAt = 1760105000000L
        )

        val results = ItemMatchingEngine.matchAll(listOf(lost), listOf(foundPoor, foundGood))

        assertEquals(2, results.size)
        assertEquals("Top match should be the umbrella", "found_umbrella", results[0].foundItemId)
        assertTrue("First match score should be higher than second", results[0].score > results[1].score)
    }
}
