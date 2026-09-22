package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FridgeItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FridgeDao {
    @Query("SELECT * FROM fridge_items ORDER BY category ASC, name ASC")
    fun getAllItemsFlow(): Flow<List<FridgeItem>>

    @Query("SELECT * FROM fridge_items")
    suspend fun getAllItems(): List<FridgeItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FridgeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FridgeItem>)

    @Update
    suspend fun update(item: FridgeItem)

    @Delete
    suspend fun delete(item: FridgeItem)

    @Query("DELETE FROM fridge_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM fridge_items")
    suspend fun clearAll()
}
