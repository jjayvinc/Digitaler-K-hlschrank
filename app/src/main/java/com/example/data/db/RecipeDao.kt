package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.CookingRecord
import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredient
import com.example.data.model.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAllRecipesWithIngredients(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeWithIngredientsById(id: Long): Flow<RecipeWithIngredients?>

    @Query("SELECT COUNT(*) FROM recipes")
    suspend fun getRecipeCount(): Int

    @Query("SELECT * FROM recipes WHERE mealDbId = :mealDbId LIMIT 1")
    suspend fun getRecipeByMealDbId(mealDbId: String): Recipe?

    @Query("SELECT mealDbId FROM recipes WHERE mealDbId IS NOT NULL")
    suspend fun getAllStoredMealDbIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @androidx.room.Update
    suspend fun updateRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<RecipeIngredient>)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Long)

    @Query("SELECT * FROM recipes WHERE mealDbId IS NOT NULL")
    suspend fun getAllMealDbRecipes(): List<Recipe>

    @Transaction
    suspend fun insertRecipeWithIngredients(recipe: Recipe, ingredients: List<RecipeIngredient>): Long {
        val existing = if (recipe.mealDbId != null) getRecipeByMealDbId(recipe.mealDbId) else null
        val recipeId = if (existing != null) {
            val updatedRecipe = recipe.copy(id = existing.id)
            updateRecipe(updatedRecipe)
            deleteIngredientsForRecipe(existing.id)
            val mappedIngredients = ingredients.map { it.copy(recipeId = existing.id) }
            insertIngredients(mappedIngredients)
            existing.id
        } else {
            val id = insertRecipe(recipe)
            val mappedIngredients = ingredients.map { it.copy(recipeId = id) }
            insertIngredients(mappedIngredients)
            id
        }
        return recipeId
    }

    @Transaction
    suspend fun insertRecipesWithIngredientsBatch(items: List<Pair<Recipe, List<RecipeIngredient>>>) {
        for ((recipe, ingredients) in items) {
            insertRecipeWithIngredients(recipe, ingredients)
        }
    }

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCookingRecord(record: CookingRecord): Long

    @Query("SELECT * FROM cooking_history ORDER BY timestamp DESC LIMIT 50")
    fun getCookingHistory(): Flow<List<CookingRecord>>
}
