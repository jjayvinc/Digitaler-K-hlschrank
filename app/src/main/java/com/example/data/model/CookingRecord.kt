package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cooking_history")
data class CookingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeTitle: String,
    val ingredientsUsedSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)
