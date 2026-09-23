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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlin.math.max

data class NormalizedFridgeItem(
    val item: FridgeItem,
    val normalizedName: String
)

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
    val catalogDao = db.catalogDao()

    val fridgeItems: Flow<List<FridgeItem>> = fridgeDao.getAllItemsFlow()
    val shoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAllShoppingItems()
    val cookingHistory: Flow<List<CookingRecord>> = recipeDao.getCookingHistory()
    val allRecipes: Flow<List<RecipeWithIngredients>> = recipeDao.getAllRecipesWithIngredients()

    // Highly optimized reactive flow offloaded to Dispatchers.Default
    val recipeMatches: Flow<List<RecipeMatchResult>> =
        combine(allRecipes, fridgeItems) { recipes, fridge ->
            val normalizedFridge = fridge.map { NormalizedFridgeItem(it, normalizeName(it.name)) }
            recipes.map { recipe ->
                calculateMatchWithNormalizedFridge(recipe, normalizedFridge)
            }.sortedWith(
                compareByDescending<RecipeMatchResult> { it.isFullyCookable }
                    .thenByDescending { it.matchRatio }
                    .thenBy { it.recipeWithIngredients.recipe.title }
            )
        }.flowOn(Dispatchers.Default)

    suspend fun getRecipeCount(): Int = recipeDao.getRecipeCount()

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
            val toUpdate = mutableListOf<Recipe>()
            for (r in recipes) {
                val newTitle = MealDbGermanTranslator.translateTitle(r.title)
                val newCategory = MealDbGermanTranslator.translateCategory(r.category)
                val newArea = MealDbGermanTranslator.translateArea(r.area.orEmpty())
                val newInstructions = MealDbGermanTranslator.translateInstructions(r.instructions)
                if (newTitle != r.title || newCategory != r.category || newArea != r.area || newInstructions != r.instructions) {
                    val preview = if (newInstructions.length > 140) newInstructions.take(140).replace("\n", " ").trim() + "…" else newInstructions.replace("\n", " ").trim()
                    val newDesc = if (newArea.isNotEmpty() && newArea != "International") {
                        "[$newArea] $preview"
                    } else {
                        preview
                    }
                    toUpdate.add(
                        r.copy(
                            title = newTitle,
                            category = newCategory,
                            area = newArea,
                            instructions = newInstructions,
                            description = newDesc
                        )
                    )
                }
            }
            for (item in toUpdate) {
                recipeDao.updateRecipe(item)
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
            val batch = mutableListOf<Pair<Recipe, List<RecipeIngredient>>>()

            for (letter in lettersToSync) {
                try {
                    val response = api.listMealsByLetter(letter)
                    val meals = response.meals.orEmpty()
                    for (dto in meals) {
                        val mealId = dto.idMeal ?: continue
                        if (!existingMealDbIds.contains(mealId)) {
                            val recipe = MealDbMapper.toRecipe(dto)
                            val ingredients = MealDbMapper.toIngredients(dto)
                            batch.add(Pair(recipe, ingredients))
                            addedCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.w("TheMealDB", "Failed to fetch letter $letter", e)
                }
            }
            if (batch.isNotEmpty()) {
                recipeDao.insertRecipesWithIngredientsBatch(batch)
                registerIngredientsFromBatch(batch)
            }
        } catch (e: Exception) {
            Log.e("TheMealDB", "Sync failed", e)
        }
        addedCount
    }

    /**
     * Synchronizes the complete master list of all ingredients from TheMealDB.
     * Translates each ingredient to German, assigns a category and emoji, and saves
     * it into the searchable catalog.
     */
    suspend fun syncAllTheMealDbIngredients(): Int = withContext(Dispatchers.IO) {
        var count = 0
        try {
            val api = NetworkModule.mealDbApi
            val response = api.getAllIngredients()
            val meals = response.meals.orEmpty()
            val existing = catalogDao.getAllCatalogIngredients().map { it.name.lowercase() }.toSet()
            val toInsert = mutableListOf<com.example.data.model.CatalogIngredientEntity>()

            for (item in meals) {
                val raw = item.strIngredient?.trim().orEmpty()
                if (raw.isBlank()) continue
                val translated = MealDbGermanTranslator.translateIngredient(raw)
                val category = getCategoryForIngredient(translated)

                com.example.ui.components.IngredientCatalog.registerIngredient(
                    name = translated,
                    category = category,
                    amount = 1.0,
                    unit = "Stück"
                )

                if (!existing.contains(translated.lowercase())) {
                    toInsert.add(
                        com.example.data.model.CatalogIngredientEntity(
                            name = translated,
                            emoji = com.example.ui.components.IngredientCatalog.getEmojiFor(translated),
                            category = category,
                            defaultAmount = 1.0,
                            unit = "Stück",
                            isCustom = false
                        )
                    )
                    count++
                }
            }

            if (toInsert.isNotEmpty()) {
                catalogDao.insertAll(toInsert)
                Log.d("TheMealDB", "Imported $count master ingredients from TheMealDB into catalog")
            }
        } catch (e: Exception) {
            Log.e("TheMealDB", "Failed to sync TheMealDB master ingredient list", e)
        }
        count
    }

    private suspend fun registerIngredientsFromBatch(batch: List<Pair<Recipe, List<RecipeIngredient>>>) {
        try {
            val existing = catalogDao.getAllCatalogIngredients().map { it.name.lowercase() }.toSet()
            val toInsert = mutableListOf<com.example.data.model.CatalogIngredientEntity>()

            for ((_, ingredients) in batch) {
                for (ing in ingredients) {
                    val name = ing.name.trim()
                    if (name.isBlank()) continue
                    val cat = getCategoryForIngredient(name)

                    com.example.ui.components.IngredientCatalog.registerIngredient(
                        name = name,
                        category = cat,
                        amount = ing.amount,
                        unit = ing.unit
                    )

                    if (!existing.contains(name.lowercase())) {
                        toInsert.add(
                            com.example.data.model.CatalogIngredientEntity(
                                name = name,
                                emoji = com.example.ui.components.IngredientCatalog.getEmojiFor(name),
                                category = cat,
                                defaultAmount = ing.amount,
                                unit = ing.unit,
                                isCustom = false
                            )
                        )
                    }
                }
            }
            if (toInsert.isNotEmpty()) {
                catalogDao.insertAll(toInsert)
            }
        } catch (e: Exception) {
            Log.w("TheMealDB", "Failed to register ingredients from batch", e)
        }
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
            val batch = mutableListOf<Pair<Recipe, List<RecipeIngredient>>>()

            for (dto in meals) {
                val mealId = dto.idMeal ?: continue
                if (!existingMealDbIds.contains(mealId)) {
                    val recipe = MealDbMapper.toRecipe(dto)
                    val ingredients = MealDbMapper.toIngredients(dto)
                    batch.add(Pair(recipe, ingredients))
                    count++
                }
            }
            if (batch.isNotEmpty()) {
                recipeDao.insertRecipesWithIngredientsBatch(batch)
                registerIngredientsFromBatch(batch)
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
        private val SYNONYM_GROUPS: List<Set<String>> = listOf(
            setOf("ei", "eier", "huehnerei", "eigelb", "eiweiss"),
            setOf("tomate", "tomaten", "strauchtomate", "cherrytomate", "rispentomate", "fleischtomate"),
            setOf("nudel", "nudeln", "spaghetti", "pasta", "penne", "macaroni", "fusilli", "tagliatelle"),
            setOf("kaese", "parmesan", "gouda", "geriebenerkaese", "cheddar", "mozzarella", "edamer", "emmentaler"),
            setOf("zwiebel", "zwiebeln", "rotezwiebel", "gemuesezwiebel", "schalotte", "fruehlingszwiebel"),
            setOf("kartoffel", "kartoffeln", "festkochendekartoffeln", "suesskartoffel"),
            setOf("reis", "basmatireis", "jasminreis", "langkornreis", "risottoreis"),
            setOf("schinken", "kochschinken", "speck", "bacon", "rohschinken", "prosciutto"),
            setOf("haehnchen", "haehnchenbrust", "huehnchen", "poulet", "huehnerbrust", "putenbrust", "gefluegel"),
            setOf("milch", "vollmilch", "hafermilch", "sojamilch", "mandelmilch"),
            setOf("brot", "toast", "toastbrot", "baguette", "ciabatta"),
            setOf("butter", "margarine", "butterschmalz"),
            setOf("knoblauch", "knoblauchzehe", "knoblauchzehen"),
            setOf("paprika", "spitzpaprika", "rotepaprika"),
            setOf("sahne", "schlagsahne", "kochcreme", "cremefraiche", "schmand"),
            setOf("mehl", "weizenmehl", "dinkelmehl"),
            setOf("apfel", "aepfel", "elstar")
        )

        fun calculateMatchWithNormalizedFridge(
            recipe: RecipeWithIngredients,
            normalizedFridge: List<NormalizedFridgeItem>
        ): RecipeMatchResult {
            val available = mutableListOf<MatchedIngredient>()
            val missing = mutableListOf<RecipeIngredient>()

            val requiredIngredients = recipe.ingredients.filter { !it.isOptional }
            val effectiveIngredients = if (requiredIngredients.isEmpty()) recipe.ingredients else requiredIngredients

            for (ingredient in recipe.ingredients) {
                val ingNorm = normalizeName(ingredient.name)
                val matchingFridge = normalizedFridge.firstOrNull {
                    matchesNormalized(it.normalizedName, ingNorm)
                }

                if (matchingFridge != null) {
                    val isSufficient = matchingFridge.item.amount >= (ingredient.amount * 0.8) ||
                            !isCompatibleUnit(matchingFridge.item.unit, ingredient.unit)
                    available.add(
                        MatchedIngredient(
                            recipeIngredient = ingredient,
                            fridgeItem = matchingFridge.item,
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

        fun calculateMatch(
            recipe: RecipeWithIngredients,
            fridgeItems: List<FridgeItem>
        ): RecipeMatchResult {
            val normalizedFridge = fridgeItems.map { NormalizedFridgeItem(it, normalizeName(it.name)) }
            return calculateMatchWithNormalizedFridge(recipe, normalizedFridge)
        }

        fun matchesIngredientName(a: String, b: String): Boolean {
            return matchesNormalized(normalizeName(a), normalizeName(b))
        }

        fun matchesNormalized(normA: String, normB: String): Boolean {
            if (normA == normB) return true
            if (normA.isNotEmpty() && normB.isNotEmpty()) {
                if (normA.contains(normB) || normB.contains(normA)) return true
            }

            for (group in SYNONYM_GROUPS) {
                val hasA = group.any { normA.contains(it) }
                if (hasA) {
                    val hasB = group.any { normB.contains(it) }
                    if (hasB) return true
                }
            }

            return false
        }

        fun normalizeName(raw: String): String {
            val lower = raw.trim().lowercase()
            val sb = java.lang.StringBuilder(lower.length + 4)
            var i = 0
            while (i < lower.length) {
                when (val c = lower[i]) {
                    'ä' -> sb.append("ae")
                    'ö' -> sb.append("oe")
                    'ü' -> sb.append("ue")
                    'ß' -> sb.append("ss")
                    in 'a'..'z', in '0'..'9' -> sb.append(c)
                }
                i++
            }
            return sb.toString()
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
