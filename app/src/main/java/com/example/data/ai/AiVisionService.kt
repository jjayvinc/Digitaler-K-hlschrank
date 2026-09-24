package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.repository.FridgeRecipeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

enum class ScanType {
    FRIDGE,
    GROCERY_PURCHASE
}

enum class AiProvider(val id: String, val displayName: String, val badge: String, val description: String) {
    GEMINI("gemini", "Google Gemini 3.6 Flash", "✨", "Echtzeit-Fotoanalyse für Kühlschrank & Kassenbon (Bereit & kostenlos)"),
    OPENAI("openai", "OpenAI Vision (GPT-4o mini)", "⚡", "Optionale Analyse mit eigenem OpenAI API-Schlüssel"),
    ANTHROPIC("anthropic", "Anthropic Claude Vision", "🌟", "Optionale Analyse mit eigenem Claude API-Schlüssel")
}

data class ScannedIngredient(
    val name: String,
    val amount: Double,
    val unit: String,
    val category: String,
    val isSelected: Boolean = true,
    val fromDatabase: Boolean = false
)

data class ScanResult(
    val ingredients: List<ScannedIngredient>,
    val scanType: ScanType,
    val providerName: String,
    val note: String? = null
)

class AiVisionService(private val context: Context) {
    private val prefs = context.getSharedPreferences("ai_vision_settings", Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    var activeProvider: AiProvider
        get() {
            val saved = prefs.getString("active_provider", AiProvider.GEMINI.id)
            return AiProvider.entries.firstOrNull { it.id == saved } ?: AiProvider.GEMINI
        }
        set(value) {
            prefs.edit().putString("active_provider", value.id).apply()
        }

    var geminiCustomKey: String
        get() = prefs.getString("gemini_api_key", "").orEmpty()
        set(value) = prefs.edit().putString("gemini_api_key", value.trim()).apply()

    var openAiKey: String
        get() = prefs.getString("openai_api_key", "").orEmpty()
        set(value) = prefs.edit().putString("openai_api_key", value.trim()).apply()

    var anthropicKey: String
        get() = prefs.getString("anthropic_api_key", "").orEmpty()
        set(value) = prefs.edit().putString("anthropic_api_key", value.trim()).apply()

    // In-memory cache of cloud-synced keys from Firebase Firestore
    private val cloudKeys = java.util.concurrent.CopyOnWriteArrayList<String>()

    init {
        fetchCloudKeys()
    }

    private fun fetchCloudKeys() {
        try {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("app_config").document("gemini")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val keys = snapshot.get("active_keys") as? List<*>
                        if (keys != null) {
                            cloudKeys.clear()
                            cloudKeys.addAll(keys.filterIsInstance<String>().map { it.trim() }.filter { it.isNotBlank() })
                        }
                    }
                }
        } catch (_: Throwable) {
            // Optional fallback
        }
    }

    /**
     * Resolves the list of candidate Gemini API keys.
     * Supports multiple keys separated by commas, semicolons, or newlines,
     * as well as cloud-synced keys from Firebase Firestore.
     */
    fun getGeminiKeyPool(): List<String> {
        val pool = mutableListOf<String>()

        // 1. User custom keys (supports comma/newline-separated list of keys)
        if (geminiCustomKey.isNotBlank()) {
            val customKeys = geminiCustomKey.split(",", ";", "\n")
                .map { it.trim() }
                .filter { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            pool.addAll(customKeys)
        }

        // 2. Cloud keys fetched from Firebase Firestore
        pool.addAll(cloudKeys)

        // 3. Secrets / BuildConfig key
        val buildConfigKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String
        } catch (_: Exception) {
            null
        }
        if (!buildConfigKey.isNullOrBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            val keys = buildConfigKey.split(",", ";", "\n")
                .map { it.trim() }
                .filter { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            pool.addAll(keys)
        }

        // 4. Built-in environment key
        val builtinKey = try {
            val field = BuildConfig::class.java.getField("BUILTIN_GEMINI_KEY")
            field.get(null) as? String
        } catch (_: Exception) {
            null
        }
        if (!builtinKey.isNullOrBlank() && builtinKey != "MY_GEMINI_API_KEY") {
            val keys = builtinKey.split(",", ";", "\n")
                .map { it.trim() }
                .filter { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }
            pool.addAll(keys)
        }

        return pool.distinct()
    }

    /**
     * Resolves the active Gemini API key from the pool.
     */
    fun getEffectiveGeminiKey(): String? {
        return getGeminiKeyPool().firstOrNull()
    }

    /**
     * Scans an image with the selected AI Provider.
     * Throws clear exceptions if keys are missing or API fails, so the user knows what happened.
     */
    suspend fun scanImage(
        bitmap: Bitmap,
        scanType: ScanType
    ): ScanResult = withContext(Dispatchers.IO) {
        val provider = activeProvider
        val base64Image = bitmapToBase64(bitmap)

        when (provider) {
            AiProvider.GEMINI -> {
                val pool = getGeminiKeyPool()
                if (pool.isEmpty()) {
                    throw IllegalStateException(
                        "Kein Gemini API-Schlüssel gefunden. Bitte stelle sicher, dass der API-Schlüssel in der Build-Umgebung konfiguriert ist oder trage deinen Key in den ⚙️ Einstellungen ein."
                    )
                }
                scanWithGemini(base64Image, scanType, pool)
            }
            AiProvider.OPENAI -> {
                if (openAiKey.isBlank()) {
                    throw IllegalStateException(
                        "Kein OpenAI API-Schlüssel hinterlegt. Bitte trage deinen Key in den ⚙️ Scan-Einstellungen ein oder wähle Google Gemini."
                    )
                }
                scanWithOpenAi(base64Image, scanType, openAiKey)
            }
            AiProvider.ANTHROPIC -> {
                if (anthropicKey.isBlank()) {
                    throw IllegalStateException(
                        "Kein Anthropic API-Schlüssel hinterlegt. Bitte trage deinen Key in den ⚙️ Scan-Einstellungen ein oder wähle Google Gemini."
                    )
                }
                scanWithAnthropic(base64Image, scanType, anthropicKey)
            }
        }
    }

    /**
     * Real Google Gemini Vision API call with automatic key rotation and model fallback:
     * Fast response with thinkingBudget: 0 to eliminate 30+ second reasoning delays and timeouts.
     * Guaranteed valid JSON array via responseMimeType: "application/json".
     */
    private suspend fun scanWithGemini(
        base64Image: String,
        scanType: ScanType,
        keyPool: List<String>
    ): ScanResult {
        val prompt = buildPrompt(scanType)
        val geminiPayload = JSONObject().apply {
            val contents = JSONArray().apply {
                val item = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    }
                    put("parts", parts)
                }
                put(item)
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
                put("maxOutputTokens", 2048)
                put("thinkingConfig", JSONObject().apply {
                    put("thinkingBudget", 0)
                })
            })
        }

        val modelsToTry = listOf("gemini-3.6-flash", "gemini-3.8-flash", "gemini-flash-latest")
        var lastError: String? = null
        var lastStatusCode = 0
        var hadTimeout = false

        // Iterate through all candidate keys in the pool (automatic key rotation)
        for ((keyIndex, currentApiKey) in keyPool.withIndex()) {
            for (model in modelsToTry) {
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$currentApiKey")
                    .post(geminiPayload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                try {
                    val response = httpClient.newCall(request).execute()
                    val responseBody = response.body?.string().orEmpty()

                    if (response.isSuccessful) {
                        val json = JSONObject(responseBody)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val parts = candidate.getJSONObject("content").getJSONArray("parts")
                            var text: String? = null
                            for (p in 0 until parts.length()) {
                                val partObj = parts.getJSONObject(p)
                                if (partObj.has("text")) {
                                    text = partObj.getString("text")
                                    break
                                }
                            }

                            if (!text.isNullOrBlank()) {
                                val items = parseJsonIngredients(text, scanType)
                                val providerTag = if (keyPool.size > 1) {
                                    "Google Gemini ($model, Key #${keyIndex + 1})"
                                } else {
                                    "Google Gemini ($model)"
                                }
                                return ScanResult(
                                    ingredients = items,
                                    scanType = scanType,
                                    providerName = providerTag
                                )
                            }
                        }
                    } else {
                        lastStatusCode = response.code
                        val errorDetail = try {
                            val errJson = JSONObject(responseBody)
                            errJson.optJSONObject("error")?.optString("message") ?: responseBody
                        } catch (_: Exception) {
                            responseBody
                        }
                        lastError = errorDetail

                        // If Rate Limited (429) or Forbidden (403/401), rotate to the next key in the pool!
                        if (response.code == 429 || response.code == 401 || response.code == 403) {
                            break
                        }

                        // If 404 (model not found) or 503, try next fallback model for same key
                        if (response.code != 404 && response.code != 503) {
                            break
                        }
                    }
                } catch (timeoutEx: java.net.SocketTimeoutException) {
                    hadTimeout = true
                    lastError = "Zeitüberschreitung beim Serveraufruf"
                    // Try next model or next key
                    continue
                } catch (ioEx: java.io.IOException) {
                    lastError = "Netzwerkfehler: ${ioEx.localizedMessage}"
                    continue
                }
            }
        }

        val friendlyMessage = when {
            hadTimeout && lastStatusCode == 0 ->
                "Zeitüberschreitung (Timeout): Die KI-Analyse hat zu lange gedauert. Bitte prüfe deine Internetverbindung oder versuche es erneut."
            lastStatusCode == 429 ->
                "Alle verfügbaren API-Schlüssel haben das Limit erreicht (429). Bitte warte eine Minute oder trage weitere Schlüssel in den ⚙️ Einstellungen ein."
            lastStatusCode == 401 || lastStatusCode == 403 ->
                "API-Zugriff verweigert ($lastStatusCode). Bitte prüfe die hinterlegten Schlüssel in den ⚙️ Einstellungen."
            lastStatusCode == 503 ->
                "Die Gemini-Server sind aktuell stark ausgelastet (503). Bitte versuche es in wenigen Momenten erneut."
            lastStatusCode == 404 ->
                "Das KI-Modell ist temporär nicht erreichbar (404). Bitte versuche es in wenigen Augenblicken erneut."
            else ->
                "Gemini API Fehler ($lastStatusCode): ${lastError ?: "Unbekannter Fehler"}"
        }
        throw IllegalStateException(friendlyMessage)
    }

    /**
     * OpenAI Vision API call (GPT-4o mini)
     */
    private suspend fun scanWithOpenAi(
        base64Image: String,
        scanType: ScanType,
        apiKey: String
    ): ScanResult {
        val prompt = buildPrompt(scanType)
        val jsonPayload = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("temperature", 0.1)
            put("max_tokens", 1000)
            val messagesArray = JSONArray().apply {
                val userMsg = JSONObject().apply {
                    put("role", "user")
                    val contentArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", prompt)
                        })
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$base64Image")
                            })
                        })
                    }
                    put("content", contentArray)
                }
                put(userMsg)
            }
            put("messages", messagesArray)
        }

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorDetail = try {
                val errJson = JSONObject(responseBody)
                errJson.optJSONObject("error")?.optString("message") ?: responseBody
            } catch (_: Exception) {
                responseBody
            }
            throw IllegalStateException("OpenAI Fehler (${response.code}): $errorDetail")
        }

        val parsedJson = JSONObject(responseBody)
        val textResponse = parsedJson.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        val ingredients = parseJsonIngredients(textResponse, scanType)
        if (ingredients.isEmpty()) {
            throw IllegalStateException("Auf dem Foto wurden keine Lebensmittel erkannt.")
        }

        return ScanResult(
            ingredients = ingredients,
            scanType = scanType,
            providerName = "OpenAI GPT-4o mini"
        )
    }

    /**
     * Anthropic Claude Vision API call (Claude 3.5 Haiku)
     */
    private suspend fun scanWithAnthropic(
        base64Image: String,
        scanType: ScanType,
        apiKey: String
    ): ScanResult {
        val prompt = buildPrompt(scanType)
        val jsonPayload = JSONObject().apply {
            put("model", "claude-3-5-haiku-20241022")
            put("max_tokens", 1000)
            val messagesArray = JSONArray().apply {
                val userMsg = JSONObject().apply {
                    put("role", "user")
                    val contentArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "image")
                            put("source", JSONObject().apply {
                                put("type", "base64")
                                put("media_type", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", prompt)
                        })
                    }
                    put("content", contentArray)
                }
                put(userMsg)
            }
            put("messages", messagesArray)
        }

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorDetail = try {
                val errJson = JSONObject(responseBody)
                errJson.optJSONObject("error")?.optString("message") ?: responseBody
            } catch (_: Exception) {
                responseBody
            }
            throw IllegalStateException("Anthropic Fehler (${response.code}): $errorDetail")
        }

        val parsedJson = JSONObject(responseBody)
        val contentArray = parsedJson.getJSONArray("content")
        val textResponse = contentArray.getJSONObject(0).getString("text")

        val ingredients = parseJsonIngredients(textResponse, scanType)
        if (ingredients.isEmpty()) {
            throw IllegalStateException("Auf dem Foto wurden keine Lebensmittel erkannt.")
        }

        return ScanResult(
            ingredients = ingredients,
            scanType = scanType,
            providerName = "Anthropic Claude 3.5"
        )
    }

    private fun buildPrompt(scanType: ScanType): String {
        return when (scanType) {
            ScanType.FRIDGE ->
                "Du bist ein intelligenter Küchen- und Lebensmittel-Scanner. " +
                        "Analysiere das beigefügte Foto dieses Kühlschranks, Gefrierfachs oder der Speisekammer aufmerksam. " +
                        "Erkenne alle sichtbaren Lebensmittel, Zutaten, Packungen, Flaschen, Gläser, Dosen, Obst, Gemüse, Milchprodukte, Soßen, Fleisch, Käse etc. " +
                        "Sei gründlich und erfasse auch Produkte in Türablagen, Fächern und hinteren Reihen. " +
                        "Gib zu jeder Zutat einen verständlichen deutschen Namen, eine realistische Menge, eine passende Einheit (Stück, Packung, Glas, Flasche, g, kg, ml, L) " +
                        "und eine der folgenden Kategorien an: 'Gemüse & Obst', 'Milch & Eier', 'Fleisch & Fisch', 'Vorrat & Teigwaren', 'Gewürze & Saucen', 'Sonstiges'. " +
                        "Antworte AUSSCHLIESSLICH als valides JSON-Array im Format: " +
                        "[{\"name\":\"Vollmilch\",\"amount\":1.0,\"unit\":\"L\",\"category\":\"Milch & Eier\"}]"

            ScanType.GROCERY_PURCHASE ->
                "Du bist ein intelligenter Kassenbon- und Einkaufs-Scanner. " +
                        "Analysiere das beigefügte Foto (Kassenbon, Quittung oder Lebensmitteleinkauf). " +
                        "Erfasse alle gekauften Lebensmittel, Speisen, Getränke und Kochzutaten mit Menge und Einheit. " +
                        "Ignoriere Nicht-Lebensmittel (wie Pfand, Plastiktüten, Tabakwaren, Reinigungsmittel, Drogerieartikel). " +
                        "Ordne jede Zutat einer Kategorie zu: 'Gemüse & Obst', 'Milch & Eier', 'Fleisch & Fisch', 'Vorrat & Teigwaren', 'Gewürze & Saucen', 'Sonstiges'. " +
                        "Antworte AUSSCHLIESSLICH als valides JSON-Array im Format: " +
                        "[{\"name\":\"Spaghetti\",\"amount\":500.0,\"unit\":\"g\",\"category\":\"Vorrat & Teigwaren\"}]"
        }
    }

    private fun mapToAppCategory(rawCat: String, ingredientName: String): String {
        val lower = rawCat.lowercase()
        return when {
            lower.contains("gemüse") || lower.contains("obst") || lower.contains("frucht") || lower.contains("salat") || lower.contains("beere") -> "Gemüse & Obst"
            lower.contains("milch") || lower.contains("käse") || lower.contains("joghurt") || lower.contains("butter") || lower.contains("ei") || lower.contains("quark") || lower.contains("sahne") -> "Milch & Eier"
            lower.contains("fleisch") || lower.contains("wurst") || lower.contains("fisch") || lower.contains("schinken") || lower.contains("hähnchen") || lower.contains("rind") || lower.contains("geflügel") || lower.contains("lachs") -> "Fleisch & Fisch"
            lower.contains("vorrat") || lower.contains("teig") || lower.contains("nudel") || lower.contains("pasta") || lower.contains("reis") || lower.contains("mehl") || lower.contains("brot") || lower.contains("müsli") || lower.contains("hafer") -> "Vorrat & Teigwaren"
            lower.contains("gewürz") || lower.contains("sauce") || lower.contains("soße") || lower.contains("öl") || lower.contains("essig") || lower.contains("senf") || lower.contains("ketchup") || lower.contains("dip") || lower.contains("dressing") -> "Gewürze & Saucen"
            else -> FridgeRecipeRepository.getCategoryForIngredient(ingredientName)
        }
    }

    /**
     * Robust parser for LLM json output (supports root arrays, wrapper objects, and markdown blocks)
     */
    private fun parseJsonIngredients(rawOutput: String, scanType: ScanType): List<ScannedIngredient> {
        val result = mutableListOf<ScannedIngredient>()
        try {
            var clean = rawOutput.trim()
            if (clean.startsWith("```json")) {
                clean = clean.removePrefix("```json")
            } else if (clean.startsWith("```")) {
                clean = clean.removePrefix("```")
            }
            if (clean.endsWith("```")) {
                clean = clean.removeSuffix("```")
            }
            clean = clean.trim()

            // Handle root JSON Array or root JSON Object with an items/ingredients array
            val array = try {
                val startIdx = clean.indexOf('[')
                val endIdx = clean.lastIndexOf(']')
                if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                    JSONArray(clean.substring(startIdx, endIdx + 1))
                } else {
                    val rootObj = JSONObject(clean)
                    rootObj.optJSONArray("ingredients")
                        ?: rootObj.optJSONArray("items")
                        ?: rootObj.optJSONArray("lebensmittel")
                        ?: rootObj.optJSONArray("products")
                        ?: JSONArray().apply { put(rootObj) }
                }
            } catch (_: Exception) {
                JSONArray()
            }

            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val rawName = obj.optString("name", "").trim()
                if (rawName.isBlank()) continue

                val rawAmount = obj.opt("amount")
                val amount = when (rawAmount) {
                    is Number -> rawAmount.toDouble()
                    is String -> rawAmount.replace(",", ".").filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 1.0
                    else -> 1.0
                }

                val unit = obj.optString("unit", "Stück").trim().ifBlank { "Stück" }
                val rawCategory = obj.optString("category", "").trim()
                val category = mapToAppCategory(rawCategory, rawName)

                result.add(
                    ScannedIngredient(
                        name = rawName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        amount = if (amount <= 0.0) 1.0 else amount,
                        unit = unit,
                        category = category,
                        isSelected = true
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("AiVisionService", "Error parsing LLM response: $rawOutput", e)
        }

        return result
    }

    /**
     * Smart built-in demo items for explicit "Beispiel testen" button in the emulator.
     * Clearly labeled as interactive demo so user is never misled.
     */
    fun generateDemoExample(scanType: ScanType): ScanResult {
        val items = when (scanType) {
            ScanType.FRIDGE -> listOf(
                ScannedIngredient("Milch", 1.0, "L", "Milch & Eier"),
                ScannedIngredient("Eier", 6.0, "Stück", "Milch & Eier"),
                ScannedIngredient("Butter", 250.0, "g", "Milch & Eier"),
                ScannedIngredient("Gouda", 200.0, "g", "Milch & Eier"),
                ScannedIngredient("Tomaten", 4.0, "Stück", "Gemüse & Obst"),
                ScannedIngredient("Paprika", 2.0, "Stück", "Gemüse & Obst"),
                ScannedIngredient("Gurke", 1.0, "Stück", "Gemüse & Obst"),
                ScannedIngredient("Karotten", 3.0, "Stück", "Gemüse & Obst"),
                ScannedIngredient("Hähnchenbrust", 400.0, "g", "Fleisch & Fisch"),
                ScannedIngredient("Naturjoghurt", 500.0, "g", "Milch & Eier")
            )
            ScanType.GROCERY_PURCHASE -> listOf(
                ScannedIngredient("Spaghetti", 500.0, "g", "Vorrat & Teigwaren"),
                ScannedIngredient("Passierte Tomaten", 500.0, "ml", "Vorrat & Teigwaren"),
                ScannedIngredient("Parmesan", 150.0, "g", "Milch & Eier"),
                ScannedIngredient("Zwiebeln", 3.0, "Stück", "Gemüse & Obst"),
                ScannedIngredient("Knoblauch", 1.0, "Knolle", "Gemüse & Obst"),
                ScannedIngredient("Olivenöl", 500.0, "ml", "Gewürze & Saucen"),
                ScannedIngredient("Mozzarella", 2.0, "Packung", "Milch & Eier"),
                ScannedIngredient("Champignons", 250.0, "g", "Gemüse & Obst")
            )
        }
        return ScanResult(
            ingredients = items,
            scanType = scanType,
            providerName = "Interaktives Beispiel (Demo)",
            note = "Beispiel-Daten für Tests im Emulator"
        )
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val maxDimension = 1200
        val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = minOf(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
