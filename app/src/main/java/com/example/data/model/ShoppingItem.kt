package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double = 1.0,
    val unit: String = "Stück",
    val category: String = "Sonstiges",
    val isBought: Boolean = false,
    val addedFromRecipe: String? = null,
    val dateAdded: Long = System.currentTimeMillis()
)
