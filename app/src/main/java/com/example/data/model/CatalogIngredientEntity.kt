package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_ingredients")
data class CatalogIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val category: String,
    val defaultAmount: Double = 1.0,
    val unit: String = "Stück",
    val isCustom: Boolean = false
)
