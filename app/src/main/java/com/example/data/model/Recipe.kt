package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // Pasta, Vegetarisch, Fleisch, Schnell, etc.
    val prepTimeMinutes: Int,
    val difficulty: String, // Einfach, Mittel, Anspruchsvoll
    val servings: Int = 2,
    val instructions: String, // Step by step instructions
    val isCustom: Boolean = false,
    val imageUrl: String? = null,
    val mealDbId: String? = null,
    val area: String? = null
)
