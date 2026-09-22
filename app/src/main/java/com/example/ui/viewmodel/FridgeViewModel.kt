package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CookingRecord
import com.example.data.model.FridgeItem
import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredient
import com.example.data.model.ShoppingItem
import com.example.data.repository.CookResult
import com.example.data.repository.FridgeRecipeRepository
import com.example.data.repository.RecipeMatchResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    FRIDGE,
    RECIPES,
    SHOPPING
}

class FridgeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = FridgeRecipeRepository(database)
    val aiService = com.example.data.ai.AiVisionService(application)

    // Active screen navigation
    private val _currentTab = MutableStateFlow(AppTab.FRIDGE)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // AI Scanner state
    private val _showScanDialog = MutableStateFlow<com.example.data.ai.ScanType?>(null)
    val showScanDialog: StateFlow<com.example.data.ai.ScanType?> = _showScanDialog.asStateFlow()

    fun openScanDialog(type: com.example.data.ai.ScanType) {
        _showScanDialog.value = type
    }

    fun closeScanDialog() {
        _showScanDialog.value = null
    }

    fun applyScannedIngredients(
        items: List<com.example.data.ai.ScannedIngredient>,
        replaceFridge: Boolean,
        scanType: com.example.data.ai.ScanType
    ) {
        viewModelScope.launch {
            if (replaceFridge && scanType == com.example.data.ai.ScanType.FRIDGE) {
                repository.clearFridge()
            }
            for (item in items) {
                repository.addFridgeItem(
                    FridgeItem(
                        name = item.name,
                        amount = item.amount,
                        unit = item.unit,
                        category = item.category
                    )
                )
            }
            closeScanDialog()
            val msg = if (scanType == com.example.data.ai.ScanType.FRIDGE) {
                "✨ ${items.size} Zutat(en) automatisch dem Kühlschrank hinzugefügt!"
            } else {
                "🛒 ${items.size} Einkaufsartikel dem Kühlschrank hinzugerechnet!"
            }
            _snackbarEvent.emit(msg)
        }
    }

    // Snackbar notifications
    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    // Search and Filter states
    val fridgeSearchQuery = MutableStateFlow("")
    val fridgeCategoryFilter = MutableStateFlow<String?>(null)

    val recipeSearchQuery = MutableStateFlow("")
    val recipeFilterMode = MutableStateFlow("Alle") // "Alle", "Sofort kochbar", "Fast fertig", "Pasta", "Vegetarisch", "Schnell"

    // Dialog & Detail states
    private val _selectedRecipe = MutableStateFlow<RecipeMatchResult?>(null)
    val selectedRecipe: StateFlow<RecipeMatchResult?> = _selectedRecipe.asStateFlow()

    private val _showAddFridgeDialog = MutableStateFlow(false)
    val showAddFridgeDialog: StateFlow<Boolean> = _showAddFridgeDialog.asStateFlow()

    private val _prefilledFridgeName = MutableStateFlow<String?>(null)
    val prefilledFridgeName: StateFlow<String?> = _prefilledFridgeName.asStateFlow()

    private val _showAddRecipeDialog = MutableStateFlow(false)
    val showAddRecipeDialog: StateFlow<Boolean> = _showAddRecipeDialog.asStateFlow()

    // Raw flows from repository
    val fridgeItems: StateFlow<List<FridgeItem>> = repository.fridgeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingItems: StateFlow<List<ShoppingItem>> = repository.shoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cookingHistory: StateFlow<List<CookingRecord>> = repository.cookingHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recipeMatches: StateFlow<List<RecipeMatchResult>> = repository.recipeMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Badge counters
    val cookableRecipesCount: StateFlow<Int> = recipeMatches
        .combine(MutableStateFlow(Unit)) { matches, _ ->
            matches.count { it.isFullyCookable }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val shoppingItemsCount: StateFlow<Int> = shoppingItems
        .combine(MutableStateFlow(Unit)) { items, _ ->
            items.count { !it.isBought }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered Fridge Items
    val filteredFridgeItems: StateFlow<List<FridgeItem>> = combine(
        fridgeItems,
        fridgeSearchQuery,
        fridgeCategoryFilter
    ) { items, query, category ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true)
            val matchesCategory = category == null || item.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Recipes
    val filteredRecipes: StateFlow<List<RecipeMatchResult>> = combine(
        recipeMatches,
        recipeSearchQuery,
        recipeFilterMode
    ) { recipes, query, filter ->
        recipes.filter { match ->
            val r = match.recipeWithIngredients.recipe
            val matchesQuery = query.isBlank() ||
                    r.title.contains(query, ignoreCase = true) ||
                    r.description.contains(query, ignoreCase = true) ||
                    match.recipeWithIngredients.ingredients.any { it.name.contains(query, ignoreCase = true) }

            val matchesFilter = when (filter) {
                "Sofort kochbar" -> match.isFullyCookable
                "Fast fertig" -> !match.isFullyCookable && match.missingIngredients.size in 1..2
                "Pasta" -> r.category.equals("Pasta", ignoreCase = true)
                "Vegetarisch" -> r.category.equals("Vegetarisch", ignoreCase = true)
                "Schnell" -> r.prepTimeMinutes <= 15
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Online database states (TheMealDB)
    private val _isSyncingOnline = MutableStateFlow(false)
    val isSyncingOnline: StateFlow<Boolean> = _isSyncingOnline.asStateFlow()

    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline: StateFlow<Boolean> = _isSearchingOnline.asStateFlow()

    init {
        viewModelScope.launch {
            repository.translateExistingMealDbRecipes()
        }
        // Automatically sync initial rich recipes batch from TheMealDB on start
        syncTheMealDb(silent = true)
    }

    fun syncTheMealDb(silent: Boolean = false) {
        viewModelScope.launch {
            _isSyncingOnline.value = true
            try {
                val newCount = repository.syncTheMealDbRecipes()
                if (!silent) {
                    if (newCount > 0) {
                        _snackbarEvent.emit("$newCount neue Rezeptideen geladen! 🍽️")
                    } else {
                        _snackbarEvent.emit("Alle Rezeptideen sind aktuell.")
                    }
                }
            } catch (e: Exception) {
                if (!silent) {
                    _snackbarEvent.emit("Aktualisierung fehlgeschlagen. Bitte Internetverbindung prüfen.")
                }
            } finally {
                _isSyncingOnline.value = false
            }
        }
    }

    fun searchOnlineMealDb(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearchingOnline.value = true
            try {
                val importedCount = repository.searchOnlineAndImport(query.trim())
                if (importedCount > 0) {
                    _snackbarEvent.emit("$importedCount neue Rezepte für „$query“ gefunden!")
                } else {
                    _snackbarEvent.emit("Keine weiteren Rezepte für „$query“ gefunden.")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Rezeptsuche fehlgeschlagen.")
            } finally {
                _isSearchingOnline.value = false
            }
        }
    }

    fun fetchRandomMealOnline() {
        viewModelScope.launch {
            _isSyncingOnline.value = true
            try {
                val result = repository.fetchRandomOnlineMeal()
                if (result != null) {
                    _snackbarEvent.emit("Neues Rezept „${result.recipe.title}“ entdeckt! 🍽️")
                    // Find and select it
                    val fridge = fridgeItems.value
                    val match = FridgeRecipeRepository.calculateMatch(result, fridge)
                    _selectedRecipe.value = match
                } else {
                    _snackbarEvent.emit("Konnte kein Gericht abrufen.")
                }
            } catch (e: Exception) {
                _snackbarEvent.emit("Fehler beim Abrufen.")
            } finally {
                _isSyncingOnline.value = false
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun openRecipeDetail(recipe: RecipeMatchResult) {
        _selectedRecipe.value = recipe
    }

    fun closeRecipeDetail() {
        _selectedRecipe.value = null
    }

    fun openAddFridgeDialog(prefillName: String? = null) {
        _prefilledFridgeName.value = prefillName
        _showAddFridgeDialog.value = true
    }

    fun closeAddFridgeDialog() {
        _showAddFridgeDialog.value = false
        _prefilledFridgeName.value = null
    }

    fun openAddRecipeDialog() {
        _showAddRecipeDialog.value = true
    }

    fun closeAddRecipeDialog() {
        _showAddRecipeDialog.value = false
    }

    // Fridge actions
    fun addFridgeItem(name: String, amount: Double, unit: String, category: String) {
        viewModelScope.launch {
            repository.addFridgeItem(
                FridgeItem(
                    name = name.trim(),
                    amount = amount,
                    unit = unit.trim(),
                    category = category
                )
            )
            _snackbarEvent.emit("„${name.trim()}“ zum Kühlschrank hinzugefügt!")
        }
    }

    fun updateFridgeAmount(item: FridgeItem, delta: Double) {
        viewModelScope.launch {
            repository.updateFridgeItemAmount(item, delta)
        }
    }

    fun deleteFridgeItem(item: FridgeItem) {
        viewModelScope.launch {
            repository.deleteFridgeItem(item)
            _snackbarEvent.emit("„${item.name}“ aus dem Kühlschrank entfernt.")
        }
    }

    fun quickAddPreset(name: String, amount: Double, unit: String, category: String) {
        viewModelScope.launch {
            repository.addFridgeItem(
                FridgeItem(
                    name = name,
                    amount = amount,
                    unit = unit,
                    category = category
                )
            )
            _snackbarEvent.emit("„$name“ hinzugefügt ($amount $unit)")
        }
    }

    fun clearFridge() {
        viewModelScope.launch {
            repository.clearFridge()
            _snackbarEvent.emit("Kühlschrank wurde geleert.")
        }
    }

    // Core requirement: Cook recipe and automatically deduct/remove ingredients from fridge
    fun cookRecipe(match: RecipeMatchResult) {
        viewModelScope.launch {
            val result: CookResult = repository.cookRecipe(match.recipeWithIngredients)
            closeRecipeDetail()
            val msg = if (result.ingredientsDeducted.isNotEmpty()) {
                "Guten Appetit! ${result.ingredientsDeducted.size} Zutat(en) automatisch aus dem Kühlschrank entnommen."
            } else {
                "Gericht zubereitet!"
            }
            _snackbarEvent.emit(msg)
        }
    }

    // Shopping actions
    fun addMissingToShoppingList(match: RecipeMatchResult) {
        viewModelScope.launch {
            val count = repository.addMissingToShoppingList(
                recipeTitle = match.recipeWithIngredients.recipe.title,
                missing = match.missingIngredients
            )
            _snackbarEvent.emit("$count fehlende Zutat(en) zur Einkaufsliste hinzugefügt!")
        }
    }

    fun addCustomShoppingItem(name: String, amount: Double, unit: String) {
        viewModelScope.launch {
            repository.addShoppingItem(
                ShoppingItem(
                    name = name.trim(),
                    amount = amount,
                    unit = unit.trim(),
                    category = FridgeRecipeRepository.getCategoryForIngredient(name)
                )
            )
            _snackbarEvent.emit("„${name.trim()}“ auf Einkaufsliste gesetzt.")
        }
    }

    fun toggleShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.toggleShoppingItemBought(item)
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    fun deleteBoughtItems() {
        viewModelScope.launch {
            repository.deleteBoughtShoppingItems()
            _snackbarEvent.emit("Erledigte Einkäufe gelöscht.")
        }
    }

    fun transferBoughtToFridge() {
        viewModelScope.launch {
            val transferredCount = repository.transferBoughtToFridge()
            if (transferredCount > 0) {
                _snackbarEvent.emit("$transferredCount gekaufte(r) Artikel in den Kühlschrank eingeräumt! 🎉")
            } else {
                _snackbarEvent.emit("Keine abgehakten Artikel zum Einräumen vorhanden.")
            }
        }
    }

    fun addCustomRecipe(
        title: String,
        description: String,
        category: String,
        prepTimeMinutes: Int,
        difficulty: String,
        servings: Int,
        instructions: String,
        ingredients: List<RecipeIngredient>
    ) {
        viewModelScope.launch {
            repository.addCustomRecipe(
                recipe = Recipe(
                    title = title.trim(),
                    description = description.trim(),
                    category = category,
                    prepTimeMinutes = prepTimeMinutes,
                    difficulty = difficulty,
                    servings = servings,
                    instructions = instructions.trim(),
                    isCustom = true
                ),
                ingredients = ingredients
            )
            closeAddRecipeDialog()
            _snackbarEvent.emit("Eigenes Rezept „$title“ gespeichert!")
        }
    }
}
