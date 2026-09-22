package com.example.data.api

import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredient

object MealDbMapper {

    // Map common English categories to German app categories
    private val categoryTranslation = mapOf(
        "Beef" to "Fleisch & Rind",
        "Chicken" to "Geflügel",
        "Dessert" to "Dessert",
        "Lamb" to "Lamm & Fleisch",
        "Miscellaneous" to "Sonstiges",
        "Pasta" to "Pasta",
        "Pork" to "Schweinefleisch",
        "Seafood" to "Fisch & Meeresfrüchte",
        "Side" to "Beilage",
        "Starter" to "Vorspeise",
        "Vegan" to "Vegan",
        "Vegetarian" to "Vegetarisch",
        "Breakfast" to "Frühstück",
        "Goat" to "Fleisch"
    )

    // Translate common ingredient names to German for effortless matching with Fridge
    private val ingredientTranslation = mapOf(
        "chicken" to "Hähnchen",
        "chicken breast" to "Hähnchenbrust",
        "chicken breasts" to "Hähnchenbrust",
        "chicken thighs" to "Hähnchenkeulen",
        "beef" to "Rindfleisch",
        "minced beef" to "Hackfleisch",
        "ground beef" to "Hackfleisch",
        "pork" to "Schweinefleisch",
        "bacon" to "Speck",
        "egg" to "Ei",
        "eggs" to "Eier",
        "milk" to "Milch",
        "butter" to "Butter",
        "cheese" to "Käse",
        "cheddar cheese" to "Cheddar",
        "parmesan" to "Parmesan",
        "parmesan cheese" to "Parmesan",
        "mozzarella" to "Mozzarella",
        "onion" to "Zwiebel",
        "onions" to "Zwiebeln",
        "red onion" to "Rote Zwiebel",
        "garlic" to "Knoblauch",
        "garlic clove" to "Knoblauchzehe",
        "garlic cloves" to "Knoblauchzehen",
        "tomato" to "Tomate",
        "tomatoes" to "Tomaten",
        "cherry tomatoes" to "Cherrytomaten",
        "tomato puree" to "Tomatenmark",
        "chopped tomatoes" to "Gehackte Tomaten",
        "potato" to "Kartoffel",
        "potatoes" to "Kartoffeln",
        "pasta" to "Pasta",
        "spaghetti" to "Spaghetti",
        "penne" to "Penne",
        "noodles" to "Nudeln",
        "rice" to "Reis",
        "basmati rice" to "Basmatireis",
        "flour" to "Mehl",
        "sugar" to "Zucker",
        "brown sugar" to "Brauner Zucker",
        "salt" to "Salz",
        "pepper" to "Pfeffer",
        "black pepper" to "Schwarzer Pfeffer",
        "olive oil" to "Olivenöl",
        "vegetable oil" to "Pflanzenöl",
        "oil" to "Öl",
        "cream" to "Sahne",
        "heavy cream" to "Schlagsahne",
        "sour cream" to "Sauerrahm",
        "lemon" to "Zitrone",
        "lemon juice" to "Zitronensaft",
        "lime" to "Limette",
        "carrot" to "Karotte",
        "carrots" to "Möhren",
        "bell pepper" to "Paprika",
        "red pepper" to "Rote Paprika",
        "green pepper" to "Grüne Paprika",
        "mushrooms" to "Champignons",
        "spinach" to "Spinat",
        "parsley" to "Petersilie",
        "coriander" to "Koriander",
        "basil" to "Basilikum",
        "oregano" to "Oregano",
        "soy sauce" to "Sojasauce",
        "mustard" to "Senf",
        "honey" to "Honig",
        "ginger" to "Ingwer",
        "bread" to "Brot",
        "cucumber" to "Gurke"
    )

    fun toRecipe(dto: MealDbDto): Recipe {
        val rawTitle = dto.strMeal?.trim().orEmpty().ifEmpty { "Köstliches Gericht" }
        val title = MealDbGermanTranslator.translateTitle(rawTitle)
        val rawCategory = dto.strCategory?.trim().orEmpty()
        val category = MealDbGermanTranslator.translateCategory(rawCategory)
        val rawArea = dto.strArea?.trim().orEmpty()
        val area = MealDbGermanTranslator.translateArea(rawArea)

        val rawInstructions = dto.strInstructions?.trim().orEmpty()
        val instructions = MealDbGermanTranslator.translateInstructions(rawInstructions)

        val previewDesc = if (instructions.length > 140) {
            instructions.take(140).replace("\r\n", " ").replace("\n", " ").trim() + "…"
        } else {
            instructions.replace("\r\n", " ").replace("\n", " ").trim()
        }

        val prepTime = estimatePrepTime(rawInstructions, rawCategory)
        val difficulty = estimateDifficulty(rawInstructions)

        val finalDescription = if (area.isNotEmpty() && area != "International") {
            "[$area] $previewDesc"
        } else {
            previewDesc
        }

        return Recipe(
            id = 0,
            title = title,
            description = finalDescription,
            category = category,
            prepTimeMinutes = prepTime,
            difficulty = difficulty,
            servings = 2,
            instructions = instructions,
            isCustom = false,
            imageUrl = dto.strMealThumb,
            mealDbId = dto.idMeal,
            area = area
        )
    }

    fun toIngredients(dto: MealDbDto): List<RecipeIngredient> {
        val pairs = listOf(
            dto.strIngredient1 to dto.strMeasure1,
            dto.strIngredient2 to dto.strMeasure2,
            dto.strIngredient3 to dto.strMeasure3,
            dto.strIngredient4 to dto.strMeasure4,
            dto.strIngredient5 to dto.strMeasure5,
            dto.strIngredient6 to dto.strMeasure6,
            dto.strIngredient7 to dto.strMeasure7,
            dto.strIngredient8 to dto.strMeasure8,
            dto.strIngredient9 to dto.strMeasure9,
            dto.strIngredient10 to dto.strMeasure10,
            dto.strIngredient11 to dto.strMeasure11,
            dto.strIngredient12 to dto.strMeasure12,
            dto.strIngredient13 to dto.strMeasure13,
            dto.strIngredient14 to dto.strMeasure14,
            dto.strIngredient15 to dto.strMeasure15,
            dto.strIngredient16 to dto.strMeasure16,
            dto.strIngredient17 to dto.strMeasure17,
            dto.strIngredient18 to dto.strMeasure18,
            dto.strIngredient19 to dto.strMeasure19,
            dto.strIngredient20 to dto.strMeasure20
        )

        val result = mutableListOf<RecipeIngredient>()

        for ((ingRaw, measureRaw) in pairs) {
            val ing = ingRaw?.trim() ?: continue
            if (ing.isEmpty()) continue

            val translatedName = translateIngredient(ing)
            val (amount, unit) = parseMeasure(measureRaw?.trim().orEmpty())

            val isOptional = translatedName.equals("Salz", ignoreCase = true) ||
                    translatedName.equals("Pfeffer", ignoreCase = true) ||
                    translatedName.contains("Öl", ignoreCase = true)

            result.add(
                RecipeIngredient(
                    recipeId = 0,
                    name = translatedName,
                    amount = amount,
                    unit = unit,
                    isOptional = isOptional
                )
            )
        }

        return result
    }

    private fun translateIngredient(raw: String): String {
        return MealDbGermanTranslator.translateIngredient(raw)
    }

    private fun parseMeasure(raw: String): Pair<Double, String> {
        if (raw.isBlank()) return Pair(1.0, "Prise/nach Bedarf")

        val clean = raw.trim()

        // Match fractions like 1/2, 1 1/2, or numbers like 250g, 2 tbs
        val fractionRegex = Regex("""^(\d+)?\s*(\d+)/(\d+)\s*(.*)$""")
        val fractionMatch = fractionRegex.find(clean)
        if (fractionMatch != null) {
            val whole = fractionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val num = fractionMatch.groupValues[2].toDoubleOrNull() ?: 0.0
            val den = fractionMatch.groupValues[3].toDoubleOrNull() ?: 1.0
            val unit = fractionMatch.groupValues[4].trim()
            val total = whole + (num / den)
            return Pair(total, cleanUnit(unit))
        }

        val numberRegex = Regex("""^([\d.,]+)\s*(.*)$""")
        val numberMatch = numberRegex.find(clean)
        if (numberMatch != null) {
            val numStr = numberMatch.groupValues[1].replace(",", ".")
            val amount = numStr.toDoubleOrNull() ?: 1.0
            val unit = numberMatch.groupValues[2].trim()
            return Pair(amount, cleanUnit(unit))
        }

        // Qualitative like "pinch", "to taste", "dash"
        return Pair(1.0, cleanUnit(clean))
    }

    private fun cleanUnit(rawUnit: String): String {
        val u = rawUnit.lowercase().trim()
        return when {
            u.isEmpty() -> "Stück"
            u.contains("tbsp") || u.contains("tablespoon") -> "EL"
            u.contains("tsp") || u.contains("teaspoon") -> "TL"
            u.contains("cup") -> "Tasse(n)"
            u.contains("gram") || u.contains("g") -> "g"
            u.contains("kg") || u.contains("kilo") -> "kg"
            u.contains("ml") -> "ml"
            u.contains("liter") || u.contains("l") -> "L"
            u.contains("pinch") -> "Prise"
            u.contains("clove") -> "Zehe(n)"
            u.contains("slice") -> "Scheibe(n)"
            u.contains("can") || u.contains("tin") -> "Dose"
            u.contains("handful") -> "Handvoll"
            else -> rawUnit.ifEmpty { "Stück" }
        }
    }

    private fun estimatePrepTime(instructions: String, category: String): Int {
        val lower = instructions.lowercase()
        return when {
            lower.contains("bake") && (lower.contains("hour") || lower.contains("60 min")) -> 60
            lower.contains("simmer for 45") || lower.contains("bake 45") -> 45
            lower.contains("bake") || lower.contains("roast") || lower.contains("30 min") -> 35
            category.equals("Pasta", ignoreCase = true) -> 20
            category.equals("Breakfast", ignoreCase = true) -> 15
            category.equals("Starter", ignoreCase = true) || category.equals("Side", ignoreCase = true) -> 15
            else -> 25
        }
    }

    private fun estimateDifficulty(instructions: String): String {
        val words = instructions.split(Regex("\\s+")).size
        return when {
            words < 70 -> "Einfach"
            words < 180 -> "Mittel"
            else -> "Anspruchsvoll"
        }
    }
}
