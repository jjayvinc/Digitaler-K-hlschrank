package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fridge_items")
data class FridgeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val normalizedName: String = normalize(name),
    val amount: Double = 1.0,
    val unit: String = "Stück",
    val category: String = "Sonstiges",
    val dateAdded: Long = System.currentTimeMillis()
) {
    companion object {
        fun normalize(raw: String): String {
            return raw.trim()
                .lowercase()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss")
                .replace(Regex("[^a-z0-9]"), "")
        }
    }
}
