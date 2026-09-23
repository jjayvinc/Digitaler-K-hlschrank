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
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
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

    /**
     * Resolves the active Gemini API key from custom settings, BuildConfig secrets, or builtin env.
     */
    fun getEffectiveGeminiKey(): String? {
        if (geminiCustomKey.isNotBlank()) {
            return geminiCustomKey
        }
        val buildConfigKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String
        } catch (_: Exception) {
            null
        }
        if (!buildConfigKey.isNullOrBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return buildConfigKey
        }
        val builtinKey = try {
            val field = BuildConfig::class.java.getField("BUILTIN_GEMINI_KEY")
            field.get(null) as? String
        } catch (_: Exception) {
            null
        }
        if (!builtinKey.isNullOrBlank() && builtinKey != "MY_GEMINI_API_KEY") {
            return builtinKey
        }
        return null
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
                val key = getEffectiveGeminiKey()
                if (key.isNullOrBlank()) {
                    throw IllegalStateException(
                        "Kein Gemini API-Schlüssel gefunden. Bitte stelle sicher, dass der API-Schlüssel in der Build-Umgebung konfiguriert ist oder trage deinen Key in den ⚙️ Einstellungen ein."
                    )
                }
                scanWithGemini(base64Image, scanType, key)
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
     * Real Google Gemini Vision API call with model fallback:
     * Tries gemini-3.6-flash first, falls back to gemini-3.5-flash or gemini-flash-latest.
     */
    private suspend fun scanWithGemini(
        base64Image: String,
        scanType: ScanType,
        apiKey: String
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
                put("temperature", 0.1)
                put("maxOutputTokens", 1200)
            })
        }

        val modelsToTry = listOf("gemini-3.6-flash", "gemini-3.5-flash", "gemini-flash-latest")
        var lastError: String? = null
        var lastStatusCode = 0

        for (model in modelsToTry) {
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                .post(geminiPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val text = candidates.getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    val items = parseJsonIngredients(text, scanType)
                    return ScanResult(
                        ingredients = items,
                        scanType = scanType,
                        providerName = "Google Gemini ($model)"
                    )
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
                // If 404 (model not found) or 503, try next fallback model
                if (response.code != 404 && response.code != 503) {
                    break
                }
            }
        }

        val friendlyMessage = when (lastStatusCode) {
            429 -> "Das Anfrage-Limit des KI-Servers ist momentan erreicht. Bitte warte eine Minute oder trage in den ⚙️ Einstellungen deinen eigenen kostenlosen Gemini-Key ein."
            401, 403 -> "API-Zugriff verweigert ($lastStatusCode). Bitte prüfe deinen Schlüssel in den ⚙️ Einstellungen oder klicke auf 'Standard nutzen'."
            404 -> "Das KI-Modell ist temporär nicht erreichbar (404). Bitte versuche es in wenigen Augenblicken erneut."
            else -> "Gemini API Fehler ($lastStatusCode): ${lastError ?: "Unbekannter Fehler"}"
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
                "Du bist ein intelligenter Küchenassistent. Analysiere das beigefügte Foto dieses Kühlschranks oder Küchentischs sorgfältig. " +
                        "Erkenne AUSSCHLIESSLICH Lebensmittel und Zutaten, die auf dem Bild tatsächlich sichtbar sind. " +
                        "Erfinde keine Zutaten dazu! Wenn auf dem Foto nur wenige oder keine Lebensmittel zu sehen sind, gib nur diese wenigen oder eine leere Liste zurück. " +
                        "Nenne zu jeder Zutat den deutschen Namen, die geschätzte Menge, Einheit (g, kg, ml, L, Stück, Packung, Glas) und Kategorie. " +
                        "Gültige Kategorien: 'Gemüse & Obst', 'Milch & Eier', 'Fleisch & Fisch', 'Vorrat & Teigwaren', 'Gewürze & Saucen', 'Sonstiges'. " +
                        "Antworte AUSSCHLIESSLICH mit einem validen JSON-Array ohne Markdown-Codeblöcke und ohne zusätzliche Erklärungen:\n" +
                        "[{\"name\":\"Milch\",\"amount\":1.0,\"unit\":\"L\",\"category\":\"Milch & Eier\"}]"

            ScanType.GROCERY_PURCHASE ->
                "Du bist ein Kassenbon- und Einkaufs-Scanner. Analysiere das beigefügte Foto (Einkaufszettel, Kassenbon oder Lebensmittel-Einkauf). " +
                        "Erkenne alle eingekauften Lebensmittel, Speisen und Zutaten. Lies bei Kassenbons die Artikelzeilen und Mengenangaben genau ab. " +
                        "Ignoriere Nicht-Lebensmittel wie Tüten, Pfand, Rabatte, Summenzeilen oder Drogerieartikel. " +
                        "Nenne zu jeder Zutat den deutschen Namen, Menge, Einheit und Kategorie. " +
                        "Gültige Kategorien: 'Gemüse & Obst', 'Milch & Eier', 'Fleisch & Fisch', 'Vorrat & Teigwaren', 'Gewürze & Saucen', 'Sonstiges'. " +
                        "Antworte AUSSCHLIESSLICH mit einem validen JSON-Array ohne Markdown-Codeblöcke und ohne weitere Worte:\n" +
                        "[{\"name\":\"Nudeln\",\"amount\":500.0,\"unit\":\"g\",\"category\":\"Vorrat & Teigwaren\"}]"
        }
    }

    /**
     * Robust parser for LLM json output
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

            val startIdx = clean.indexOf('[')
            val endIdx = clean.lastIndexOf(']')
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                clean = clean.substring(startIdx, endIdx + 1)
            }

            val array = JSONArray(clean)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val rawName = obj.optString("name", "").trim()
                if (rawName.isBlank()) continue

                val amount = obj.optDouble("amount", 1.0)
                val unit = obj.optString("unit", "Stück").trim()
                var category = obj.optString("category", "").trim()
                if (category.isBlank() || category == "null") {
                    category = FridgeRecipeRepository.getCategoryForIngredient(rawName)
                }

                result.add(
                    ScannedIngredient(
                        name = rawName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        amount = if (amount <= 0) 1.0 else amount,
                        unit = if (unit.isBlank()) "Stück" else unit,
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
        val maxDimension = 1024
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
