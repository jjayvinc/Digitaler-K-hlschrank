package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CatalogIngredientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalog_ingredients ORDER BY name ASC")
    fun getAllCatalogIngredientsFlow(): Flow<List<CatalogIngredientEntity>>

    @Query("SELECT * FROM catalog_ingredients ORDER BY name ASC")
    suspend fun getAllCatalogIngredients(): List<CatalogIngredientEntity>

    @Query("SELECT COUNT(*) FROM catalog_ingredients")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CatalogIngredientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CatalogIngredientEntity>)

    @Query("SELECT * FROM catalog_ingredients WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): CatalogIngredientEntity?
}
