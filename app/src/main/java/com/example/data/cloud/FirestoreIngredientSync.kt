package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.db.CatalogDao
import com.example.data.model.CatalogIngredientEntity
import com.example.ui.components.IngredientCatalog
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirestoreIngredientSync {
    private const val TAG = "FirestoreSync"
    private const val COLLECTION_NAME = "catalog_ingredients"

    private var listenerRegistration: ListenerRegistration? = null
    private var isSyncActive = false

    fun getFirestore(context: Context): FirebaseFirestore {
        return try {
            val dbId = context.getString(R.string.firestore_database_id).trim()
            if (dbId.isNotBlank()) {
                val app = FirebaseApp.getInstance()
                FirebaseFirestore.getInstance(app, dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Falling back to default Firestore instance: ${e.message}")
            FirebaseFirestore.getInstance()
        }
    }

    /**
     * Starts listening for realtime ingredient updates across all users.
     * When any user scans or adds a new ingredient anywhere, this listener triggers
     * and automatically injects the new ingredient into the local in-memory catalog
     * and Room database.
     */
    fun startRealtimeSync(
        context: Context,
        catalogDao: CatalogDao,
        coroutineScope: CoroutineScope
    ) {
        if (isSyncActive) return
        isSyncActive = true

        try {
            val firestore = getFirestore(context)
            listenerRegistration = firestore.collection(COLLECTION_NAME)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen failed on $COLLECTION_NAME", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val entitiesToInsert = mutableListOf<CatalogIngredientEntity>()

                                for (doc in snapshot.documents) {
                                    val name = doc.getString("name")?.trim().orEmpty()
                                    if (name.isBlank()) continue

                                    val emoji = doc.getString("emoji") ?: IngredientCatalog.getEmojiFor(name)
                                    val category = doc.getString("category") ?: "Vorrat & Teigwaren"
                                    val defaultAmount = doc.getDouble("defaultAmount") ?: 1.0
                                    val unit = doc.getString("unit") ?: "Stück"

                                    // Register in-memory catalog
                                    IngredientCatalog.registerIngredient(
                                        name = name,
                                        category = category,
                                        amount = defaultAmount,
                                        unit = unit
                                    )

                                    // Check if existing in local DB
                                    val existing = catalogDao.findByName(name)
                                    if (existing == null) {
                                        entitiesToInsert.add(
                                            CatalogIngredientEntity(
                                                name = name,
                                                emoji = emoji,
                                                category = category,
                                                defaultAmount = defaultAmount,
                                                unit = unit,
                                                isCustom = true
                                            )
                                        )
                                    }
                                }

                                if (entitiesToInsert.isNotEmpty()) {
                                    catalogDao.insertAll(entitiesToInsert)
                                    Log.d(TAG, "Synced ${entitiesToInsert.size} new shared ingredients from Firestore")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error applying Firestore ingredient snapshot", e)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Could not start Firestore sync", e)
        }
    }

    /**
     * Publishes a newly discovered ingredient to Firestore so every user worldwide gets it.
     */
    fun publishIngredient(
        context: Context,
        name: String,
        category: String,
        amount: Double = 1.0,
        unit: String = "Stück"
    ) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        val docId = trimmed.lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace(Regex("[^a-z0-9]"), "_")

        try {
            val firestore = getFirestore(context)
            val emoji = IngredientCatalog.getEmojiFor(trimmed)
            val data = mapOf(
                "name" to trimmed,
                "emoji" to emoji,
                "category" to category,
                "defaultAmount" to amount,
                "unit" to unit,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            firestore.collection(COLLECTION_NAME)
                .document(docId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully shared ingredient '$trimmed' to Firestore worldwide")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to share ingredient '$trimmed' to Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing ingredient to Firestore", e)
        }
    }

    /**
     * Publishes multiple ingredients at once (e.g. from receipt/fridge scan).
     */
    fun publishIngredientsBatch(
        context: Context,
        ingredients: List<com.example.data.ai.ScannedIngredient>
    ) {
        if (ingredients.isEmpty()) return
        try {
            val firestore = getFirestore(context)
            val batch = firestore.batch()

            for (item in ingredients) {
                val trimmed = item.name.trim()
                if (trimmed.isBlank()) continue

                val docId = trimmed.lowercase()
                    .replace("ä", "ae")
                    .replace("ö", "oe")
                    .replace("ü", "ue")
                    .replace("ß", "ss")
                    .replace(Regex("[^a-z0-9]"), "_")

                val docRef = firestore.collection(COLLECTION_NAME).document(docId)
                val emoji = IngredientCatalog.getEmojiFor(trimmed)
                val data = mapOf(
                    "name" to trimmed,
                    "emoji" to emoji,
                    "category" to item.category,
                    "defaultAmount" to item.amount,
                    "unit" to item.unit,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, data, SetOptions.merge())
            }

            batch.commit()
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully batch published ${ingredients.size} ingredients to Firestore")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed batch commit to Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error batch publishing to Firestore", e)
        }
    }

    fun stopSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
        isSyncActive = false
    }
}
