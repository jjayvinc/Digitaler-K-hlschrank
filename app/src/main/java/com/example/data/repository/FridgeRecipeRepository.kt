package com.example.data.repository

import android.util.Log
import com.example.data.api.MealDbGermanTranslator
import com.example.data.api.MealDbMapper
import com.example.data.api.NetworkModule
import com.example.data.db.AppDatabase
import com.example.data.model.CookingRecord
import com.example.data.model.FridgeItem
import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeWithIngredients
import com.example.data.model.ShoppingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlin.math.max

data class MatchedIngredient(
    val recipeIngredient: RecipeIngredient,
    val fridgeItem: FridgeItem,
    val isAmountSufficient: Boolean
)

data class RecipeMatchResult(
    val recipeWithIngredients: RecipeWithIngredients,
    val availableIngredients: List<MatchedIngredient>,
    val missingIngredients: List<RecipeIngredient>,
    val isFullyCookable: Boolean,
    val matchRatio: Float,
    val matchedCount: Int,
    val totalRequiredCount: Int
)

data class CookResult(
    val recipeTitle: String,
    val ingredientsDeducted: List<String>,
    val partialCook: Boolean
)

class FridgeRecipeRepository(private val db: AppDatabase) {
    private val fridgeDao = db.fridgeDao()
    private val recipeDao = db.recipeDao()
    private val shoppingDao = db.shoppingDao()

    val fridgeItems: Flow<List<FridgeItem>> = fridgeDao.getAllItemsFlow()
    val shoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAllShoppingItems()
    val cookingHistory: Flow<List<CookingRecord>> = recipeDao.getCookingHistory()
    val allRecipes: Flow<List<RecipeWithIngredients>> = recipeDao.getAllRecipesWithIngredients()

    // Reactive flow combining recipes and current fridge contents
    val recipeMatches: Flow<List<RecipeMatchResult>> =
        combine(allRecipes, fridgeItems) { recipes, fridge ->
            recipes.map { recipe ->
                calculateMatch(recipe, fridge)
            }.sortedWith(
                compareByDescending<RecipeMatchResult> { it.isFullyCookable }
                    .thenByDescending { it.matchRatio }
                    .thenBy { it.recipeWithIngredients.recipe.title }
            )
        }

    suspend fun addFridgeItem(item: FridgeItem): Long {
        val existing = fridgeDao.getAllItems().firstOrNull {
            matchesIngredientName(it.name, item.name) && it.unit.equals(item.unit, ignoreCase = true)
        }
        return if (existing != null) {
            val updated = existing.copy(amount = existing.amount + item.amount)
            fridgeDao.update(updated)
            existing.id
        } else {
            fridgeDao.insert(item)
        }
    }

    suspend fun updateFridgeItem(item: FridgeItem) {
        fridgeDao.update(item)
    }

    suspend fun deleteFridgeItem(item: FridgeItem) {
        fridgeDao.delete(item)
    }

    suspend fun updateFridgeItemAmount(item: FridgeItem, delta: Double) {
        val newAmount = item.amount + delta
        if (newAmount <= 0.05) {
            fridgeDao.delete(item)
        } else {
            fridgeDao.update(item.copy(amount = (newAmount * 10).toInt() / 10.0))
        }
    }

    suspend fun clearFridge() {
        fridgeDao.clearAll()
    }

    suspend fun addShoppingItem(item: ShoppingItem): Long {
        return shoppingDao.insert(item)
    }

    suspend fun updateShoppingItem(item: ShoppingItem) {
        shoppingDao.update(item)
    }

    suspend fun toggleShoppingItemBought(item: ShoppingItem) {
        shoppingDao.update(item.copy(isBought = !item.isBought))
    }

    suspend fun deleteShoppingItem(item: ShoppingItem) {
        shoppingDao.delete(item)
    }

    suspend fun deleteBoughtShoppingItems() {
        shoppingDao.deleteBought()
    }

    suspend fun clearShoppingList() {
        shoppingDao.clearAll()
    }

    suspend fun addMissingToShoppingList(
        recipeTitle: String,
        missing: List<RecipeIngredient>
    ): Int {
        val items = missing.map { ingredient ->
            ShoppingItem(
                name = ingredient.name,
                amount = ingredient.amount,
                unit = ingredient.unit,
                category = getCategoryForIngredient(ingredient.name),
                isBought = false,
                addedFromRecipe = recipeTitle
            )
        }
        shoppingDao.insertAll(items)
        return items.size
    }

    // Moves all checked/bought items into the digital fridge, then removes them from the shopping list
    suspend fun transferBoughtToFridge(): Int {
        val bought = shoppingDao.getBoughtItems()
        if (bought.isEmpty()) return 0

        for (item in bought) {
            addFridgeItem(
                FridgeItem(
                    name = item.name,
                    amount = item.amount,
                    unit = item.unit,
                    category = item.category
                )
            )
        }
        shoppingDao.deleteBought()
        return bought.size
    }

    // Core requirement: When a dish is cooked, automatically remove ingredients from the fridge!
    suspend fun cookRecipe(recipeWithIngredients: RecipeWithIngredients): CookResult {
        val currentFridge = fridgeDao.getAllItems()
        val deductedSummaries = mutableListOf<String>()
        var isPartial = false

        for (req in recipeWithIngredients.ingredients) {
            if (req.isOptional) continue

            val match = currentFridge.firstOrNull { fridgeItem ->
                matchesIngredientName(fridgeItem.name, req.name)
            }

            if (match != null) {
                val newAmount = match.amount - req.amount
                if (newAmount <= 0.05 || !isCompatibleUnit(match.unit, req.unit)) {
                    // Fully consumed or unit mismatch (e.g. 1 Packung used up)
                    fridgeDao.delete(match)
                    deductedSummaries.add("${match.name} (${formatAmount(match.amount)} ${match.unit} entnommen)")
                } else {
                    // Reduced
                    val updatedAmount = (newAmount * 10).toInt() / 10.0
                    fridgeDao.update(match.copy(amount = updatedAmount))
                    deductedSummaries.add("${match.name} (${formatAmount(req.amount)} ${req.unit} entnommen, Rest: ${formatAmount(updatedAmount)} ${match.unit})")
                }
            } else {
                isPartial = true
            }
        }

        // Record history
        val summaryString = if (deductedSummaries.isNotEmpty()) {
            deductedSummaries.joinToString(", ")
        } else {
            "Keine Zutaten im Kühlschrank gefunden"
        }

        recipeDao.insertCookingRecord(
            CookingRecord(
                recipeTitle = recipeWithIngredients.recipe.title,
                ingredientsUsedSummary = summaryString
            )
        )

        return CookResult(
            recipeTitle = recipeWithIngredients.recipe.title,
            ingredientsDeducted = deductedSummaries,
            partialCook = isPartial
        )
    }

    suspend fun addCustomRecipe(recipe: Recipe, ingredients: List<RecipeIngredient>): Long {
        val id = recipeDao.insertRecipe(recipe.copy(isCustom = true))
        val withId = ingredients.map { it.copy(recipeId = id) }
        recipeDao.insertIngredients(withId)
        return id
    }

    /**
     * Translates any already stored MealDB recipes in the local database to German.
     */
    suspend fun translateExistingMealDbRecipes() = withContext(Dispatchers.IO) {
        try {
            val recipes = recipeDao.getAllMealDbRecipes()
            for (r in recipes) {
                val newTitle = MealDbGermanTranslator.translateTitle(r.title)
                val newCategory = MealDbGermanTranslator.translateCategory(r.category)
                val newArea = MealDbGermanTranslator.translateArea(r.area.orEmpty())
                val newInstructions = MealDbGermanTranslator.translateInstructions(r.instructions)
                val preview = if (newInstructions.length > 140) newInstructions.take(140).replace("\n", " ").trim() + "…" else newInstructions.replace("\n", " ").trim()
                val newDesc = if (newArea.isNotEmpty() && newArea != "International") {
                    "[$newArea] $preview"
                } else {
                    preview
                }
                val updated = r.copy(
                    title = newTitle,
                    category = newCategory,
                    area = newArea,
                    instructions = newInstructions,
                    description = newDesc
                )
                recipeDao.updateRecipe(updated)
            }
        } catch (e: Exception) {
            Log.e("TheMealDB", "Translation pass failed", e)
        }
    }

    /**
     * Synchronizes a curated batch of diverse recipes from TheMealDB into local database.
     * Ensures all popular categories and letters are represented immediately offline!
     */
    suspend fun syncTheMealDbRecipes(lettersToSync: List<Char> = listOf('c', 'p', 'b', 's', 'm', 't', 'a')): Int = withContext(Dispatchers.IO) {
        var addedCount = 0
        try {
            // First translate any existing recipes that might be in English
            translateExistingMealDbRecipes()

            val existingMealDbIds = recipeDao.getAllStoredMealDbIds().toSet()
            val api = NetworkModule.mealDbApi

            for (letter in lettersToSync) {
                try {
                    val response = api.listMealsByLetter(letter)
                    val meals = response.meals.orEmpty()
                    for (dto in meals) {
                        val mealId = dto.idMeal ?: continue
                        if (!existingMealDbIds.contains(mealId)) {
                            val recipe = MealDbMapper.toRecipe(dto)
                            val ingredients = MealDbMapper.toIngredients(dto)
                            recipeDao.insertRecipeWithIngredients(recipe, ingredients)
                            addedCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.w("TheMealDB", "Failed to fetch letter $letter", e)
                }
            }
        } catch (e: Exception) {
            Log.e("TheMealDB", "Sync failed", e)
        }
        addedCount
    }

    /**
     * Live search in TheMealDB online database.
     * Any fetched results not yet in the local database are seamlessly saved
     * so that ingredients match with fridge and are available offline!
     */
    suspend fun searchOnlineAndImport(query: String): Int = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext 0
        var count = 0
        try {
            val api = NetworkModule.mealDbApi
            val response = api.searchMeals(query)
            val meals = response.meals.orEmpty()
            val existingMealDbIds = recipeDao.getAllStoredMealDbIds().toSet()

            for (dto in meals) {
                val mealId = dto.idMeal ?: continue
                if (!existingMealDbIds.contains(mealId)) {
                    val recipe = MealDbMapper.toRecipe(dto)
                    val ingredients = MealDbMapper.toIngredients(dto)
                    recipeDao.insertRecipeWithIngredients(recipe, ingredients)
                    count++
                }
            }
        } catch (e: Exception) {
            Log.e("TheMealDB", "Online search failed for $query", e)
        }
        count
    }

    /**
     * Loads a random recipe from TheMealDB and saves it to local DB.
     */
    suspend fun fetchRandomOnlineMeal(): RecipeWithIngredients? = withContext(Dispatchers.IO) {
        try {
            val api = NetworkModule.mealDbApi
            val response = api.getRandomMeal()
            val meal = response.meals?.firstOrNull() ?: return@withContext null
            val recipe = MealDbMapper.toRecipe(meal)
            val ingredients = MealDbMapper.toIngredients(meal)
            val id = recipeDao.insertRecipeWithIngredients(recipe, ingredients)
            RecipeWithIngredients(recipe = recipe.copy(id = id), ingredients = ingredients.map { it.copy(recipeId = id) })
        } catch (e: Exception) {
            Log.e("TheMealDB", "Failed to fetch random meal", e)
            null
        }
    }

    companion object {
        fun calculateMatch(
            recipe: RecipeWithIngredients,
            fridgeItems: List<FridgeItem>
        ): RecipeMatchResult {
            val available = mutableListOf<MatchedIngredient>()
            val missing = mutableListOf<RecipeIngredient>()

            val requiredIngredients = recipe.ingredients.filter { !it.isOptional }
            val effectiveIngredients = if (requiredIngredients.isEmpty()) recipe.ingredients else requiredIngredients

            for (ingredient in recipe.ingredients) {
                val matchingFridgeItem = fridgeItems.firstOrNull { fridgeItem ->
                    matchesIngredientName(fridgeItem.name, ingredient.name)
                }

                if (matchingFridgeItem != null) {
                    val isSufficient = matchingFridgeItem.amount >= (ingredient.amount * 0.8) ||
                            !isCompatibleUnit(matchingFridgeItem.unit, ingredient.unit)
                    available.add(
                        MatchedIngredient(
                            recipeIngredient = ingredient,
                            fridgeItem = matchingFridgeItem,
                            isAmountSufficient = isSufficient
                        )
                    )
                } else {
                    if (!ingredient.isOptional) {
                        missing.add(ingredient)
                    }
                }
            }

            val requiredCount = effectiveIngredients.size
            val matchedRequiredCount = available.count { match ->
                effectiveIngredients.any { it.id == match.recipeIngredient.id }
            }

            val ratio = if (requiredCount > 0) {
                (matchedRequiredCount.toFloat() / requiredCount).coerceIn(0f, 1f)
            } else 1f

            val fullyCookable = missing.isEmpty() && requiredCount > 0

            return RecipeMatchResult(
                recipeWithIngredients = recipe,
                availableIngredients = available,
                missingIngredients = missing,
                isFullyCookable = fullyCookable,
                matchRatio = ratio,
                matchedCount = matchedRequiredCount,
                totalRequiredCount = requiredCount
            )
        }

        fun matchesIngredientName(a: String, b: String): Boolean {
            val normA = normalizeName(a)
            val normB = normalizeName(b)

            if (normA == normB) return true
            if (normA.contains(normB) || normB.contains(normA)) return true

            // Common German food synonyms and plurals
            val synonyms = listOf(
                setOf("ei", "eier", "huehnerei", "eigelb"),
                setOf("tomate", "tomaten", "strauchtomate", "cherrytomate"),
                setOf("nudel", "nudeln", "spaghetti", "pasta", "penne", "macaroni"),
                setOf("kaese", "parmesan", "gouda", "geriebenerkaese", "cheddar", "mozzarella"),
                setOf("zwiebel", "zwiebeln", "rotezwiebel", "gemuesezwiebel", "schalotte"),
                setOf("kartoffel", "kartoffeln", "festkochendekartoffeln"),
                setOf("reis", "basmatireis", "jasminreis", "langkornreis"),
                setOf("schinken", "kochschinken", "speck", "bacon", "rohschinken"),
                setOf("haehnchen", "haehnchenbrust", "huehnchen", "poulet", "huehnerbrust"),
                setOf("milch", "vollmilch", "hafermilch", "sojamilch"),
                setOf("brot", "toast", "toastbrot", "baguette"),
                setOf("butter", "margarine"),
                setOf("knoblauch", "knoblauchzehe", "knoblauchzehen"),
                setOf("paprika", "spitzpaprika", "rotepaprika"),
                setOf("sahne", "schlagsahne", "kochcreme", "cremefraiche"),
                setOf("mehl", "weizenmehl", "dinkelmehl"),
                setOf("apfel", "aepfel", "elstar")
            )

            for (group in synonyms) {
                val hasA = group.any { normA.contains(it) }
                val hasB = group.any { normB.contains(it) }
                if (hasA && hasB) return true
            }

            return false
        }

        private fun normalizeName(raw: String): String {
            return raw.trim()
                .lowercase()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replace(Regex("[^a-z0-9]"), "")
        }

        fun isCompatibleUnit(unitA: String, unitB: String): Boolean {
            val u1 = unitA.trim().lowercase()
            val u2 = unitB.trim().lowercase()
            if (u1 == u2) return true
            val weightUnits = setOf("g", "gramm", "kg", "kilogramm")
            if (u1 in weightUnits && u2 in weightUnits) return true
            val volumeUnits = setOf("ml", "milliliter", "l", "liter")
            if (u1 in volumeUnits && u2 in volumeUnits) return true
            val countUnits = setOf("stück", "stk", "stk.", "zehe", "zehen", "scheibe", "scheiben")
            if (u1 in countUnits && u2 in countUnits) return true
            return false
        }

        fun formatAmount(amount: Double): String {
            return if (amount % 1.0 == 0.0) {
                amount.toInt().toString()
            } else {
                String.format(java.util.Locale.GERMAN, "%.1f", amount)
            }
        }

        fun getCategoryForIngredient(name: String): String {
            val norm = normalizeName(name)
            return when {
                norm.contains("tomate") || norm.contains("paprika") || norm.contains("zucchini") ||
                        norm.contains("kartoffel") || norm.contains("zwiebel") || norm.contains("knoblauch") ||
                        norm.contains("gurke") || norm.contains("apfel") || norm.contains("lauch") ||
                        norm.contains("moehre") || norm.contains("karotte") -> "Gemüse & Obst"

                norm.contains("milch") || norm.contains("ei") || norm.contains("kaese") ||
                        norm.contains("butter") || norm.contains("sahne") || norm.contains("mozzarella") ||
                        norm.contains("feta") || norm.contains("joghurt") || norm.contains("quark") -> "Milch & Eier"

                norm.contains("fleisch") || norm.contains("haehnchen") || norm.contains("schinken") ||
                        norm.contains("speck") || norm.contains("hack") || norm.contains("rind") ||
                        norm.contains("lachs") || norm.contains("thunfisch") || norm.contains("fisch") -> "Fleisch & Fisch"

                norm.contains("nudel") || norm.contains("pasta") || norm.contains("spaghetti") ||
                        norm.contains("reis") || norm.contains("mehl") || norm.contains("haferflocken") ||
                        norm.contains("brot") || norm.contains("toast") -> "Vorrat & Teigwaren"

                norm.contains("oel") || norm.contains("salz") || norm.contains("pfeffer") ||
                        norm.contains("curry") || norm.contains("zimt") || norm.contains("basilikum") ||
                        norm.contains("oregano") || norm.contains("sojasauce") || norm.contains("kraeuter") -> "Gewürze & Saucen"

                else -> "Sonstiges"
            }
        }
    }
}
