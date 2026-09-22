package com.example

import com.example.data.ai.ScannedIngredient
import com.example.ui.components.IngredientCatalog
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun ingredientCatalog_searchDatabase_findsIngredients() {
        val results = IngredientCatalog.searchDatabase("Paprika")
        assertTrue("Should find Paprika in database", results.any { it.name.contains("Paprika") })
    }

    @Test
    fun ingredientCatalog_categoryFilter_worksCorrectly() {
        val dairyResults = IngredientCatalog.searchDatabase("", "Milch & Eier")
        assertTrue("Dairy category should not be empty", dairyResults.isNotEmpty())
        assertTrue("All returned items should belong to Milch & Eier", dairyResults.all { it.category == "Milch & Eier" })
    }

    @Test
    fun ingredientCatalog_getEmoji_returnsAppropriateEmoji() {
        assertEquals("🥚", IngredientCatalog.getEmojiFor("Eier"))
        assertEquals("🥛", IngredientCatalog.getEmojiFor("Milch"))
        assertEquals("🧀", IngredientCatalog.getEmojiFor("Gouda"))
        assertEquals("🍅", IngredientCatalog.getEmojiFor("Tomaten"))
    }

    @Test
    fun scannedIngredient_fromDatabase_flagPreserved() {
        val item = ScannedIngredient(
            name = "Schnittlauch",
            amount = 1.0,
            unit = "Bund",
            category = "Gemüse & Obst",
            isSelected = true,
            fromDatabase = true
        )
        assertTrue(item.fromDatabase)
        assertTrue(item.isSelected)
        assertEquals("Schnittlauch", item.name)
    }
}
