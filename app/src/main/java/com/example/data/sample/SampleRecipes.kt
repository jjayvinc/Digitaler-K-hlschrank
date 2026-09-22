package com.example.data.sample

import com.example.data.db.AppDatabase
import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredient

object SampleRecipes {
    suspend fun populateInitialData(db: AppDatabase) {
        val recipeDao = db.recipeDao()
        if (recipeDao.getRecipeCount() > 0) return

        data class SeedRecipe(
            val recipe: Recipe,
            val ingredients: List<RecipeIngredient>
        )

        val seedList = listOf(
            SeedRecipe(
                recipe = Recipe(
                    title = "Cremige Tomaten-Mozzarella Pasta",
                    description = "Herrlich aromatische Pasta mit geschmolzenem Mozzarella, Knoblauch und fruchtigen Tomaten in 15 Minuten.",
                    category = "Pasta",
                    prepTimeMinutes = 15,
                    difficulty = "Einfach",
                    servings = 2,
                    instructions = "1. Nudeln in reichlich Salzwasser bissfest kochen.\n2. In einer Pfanne Olivenöl erhitzen und fein gehackten Knoblauch kurz anbraten.\n3. Gewürfelte Tomaten dazugeben und ca. 5 Minuten sanft köcheln lassen.\n4. Die gekochten Nudeln unterrühren.\n5. Den Mozzarella würfeln und unterheben, bis er leicht schmilzt. Mit Basilikum servieren."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Nudeln", amount = 250.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Tomaten", amount = 3.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Mozzarella", amount = 1.0, unit = "Packung"),
                    RecipeIngredient(recipeId = 0, name = "Knoblauch", amount = 2.0, unit = "Zehen"),
                    RecipeIngredient(recipeId = 0, name = "Olivenöl", amount = 2.0, unit = "EL", isOptional = true)
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Klassisches Rührei mit Schnittlauch",
                    description = "Fluffig-cremiges Rührei mit Butter und frischen Kräutern – der perfekte, schnelle Protein-Kick.",
                    category = "Schnell",
                    prepTimeMinutes = 10,
                    difficulty = "Einfach",
                    servings = 2,
                    instructions = "1. Eier in einer Schüssel mit etwas Milch, Salz und Pfeffer verquirlen.\n2. Butter in einer beschichteten Pfanne bei mittlerer Hitze schmelzen.\n3. Eiermasse hineingeben und mit einem Spatel langsam von außen nach innen schieben.\n4. Sobald das Ei gestockt, aber noch saftig ist, von der Hitze nehmen und mit frischem Schnittlauch bestreuen."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Eier", amount = 4.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Milch", amount = 50.0, unit = "ml"),
                    RecipeIngredient(recipeId = 0, name = "Butter", amount = 1.0, unit = "EL"),
                    RecipeIngredient(recipeId = 0, name = "Schnittlauch", amount = 1.0, unit = "Bund", isOptional = true)
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Shakshuka (Würzige Eier in Tomaten)",
                    description = "Klassiker aus der Pfanne: Pochierte Eier in aromatischer Tomaten-Paprika-Sauce.",
                    category = "Vegetarisch",
                    prepTimeMinutes = 25,
                    difficulty = "Mittel",
                    servings = 2,
                    instructions = "1. Zwiebel, Knoblauch und Paprika würfeln.\n2. In Olivenöl ca. 5 Minuten anbraten.\n3. Tomaten hinzufügen und mit Salz, Pfeffer und Gewürzen 10 Minuten einköcheln.\n4. Mit einem Löffel Vertiefungen formen und die Eier hineinschlagen.\n5. Zugedeckt bei milder Hitze ca. 6-8 Minuten stocken lassen."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Eier", amount = 4.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Tomaten", amount = 4.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Paprika", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Zwiebeln", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Knoblauch", amount = 2.0, unit = "Zehen"),
                    RecipeIngredient(recipeId = 0, name = "Olivenöl", amount = 2.0, unit = "EL", isOptional = true)
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Spaghetti Carbonara",
                    description = "Traditionelle italienische Pasta mit knusprigem Schinken, Eigelb und feinem Käse.",
                    category = "Pasta",
                    prepTimeMinutes = 20,
                    difficulty = "Mittel",
                    servings = 2,
                    instructions = "1. Spaghetti in Salzwasser al dente kochen. Etwas Nudelwasser auffangen.\n2. Schinken in feine Streifen schneiden und in der Pfanne knusprig auslassen.\n3. Eier mit geriebenem Käse und frisch gemahlenem Pfeffer verrühren.\n4. Nudeln zum Schinken geben, Pfanne von der Hitze nehmen, Eiermischung und 2 EL Nudelwasser rasch cremig unterheben."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Nudeln", amount = 250.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Eier", amount = 3.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Käse", amount = 60.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Schinken", amount = 100.0, unit = "g")
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Bunte Gemüsepfanne mit Reis",
                    description = "Knackiges Gemüse aus der Pfanne mit aromatischem Reis und feinem Dressing.",
                    category = "Vegetarisch",
                    prepTimeMinutes = 20,
                    difficulty = "Einfach",
                    servings = 2,
                    instructions = "1. Reis nach Packungsanleitung gar kochen.\n2. Zwiebeln, Paprika und Zucchini in mundgerechte Stücke schneiden.\n3. In heißem Öl scharf anbraten, Knoblauch hinzufügen.\n4. Mit Sojasauce oder Gewürzen abschmecken und den Reis untermischen."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Reis", amount = 150.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Paprika", amount = 2.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Zucchini", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Zwiebeln", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Knoblauch", amount = 1.0, unit = "Zehe")
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Goldbraune Pfannkuchen",
                    description = "Herrlich zarte Pfannkuchen nach Omas Rezept – schmecken süß oder herzhaft.",
                    category = "Backen & Süßes",
                    prepTimeMinutes = 15,
                    difficulty = "Einfach",
                    servings = 2,
                    instructions = "1. Mehl, Eier, Milch und eine Prise Salz zu einem glatten Teig verrühren.\n2. Teig 5 Minuten ruhen lassen.\n3. Etwas Butter in der Pfanne erhitzen und eine Kelle Teig gleichmäßig verteilen.\n4. Bei mittlerer Hitze von beiden Seiten goldbraun backen."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Mehl", amount = 200.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Milch", amount = 300.0, unit = "ml"),
                    RecipeIngredient(recipeId = 0, name = "Eier", amount = 2.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Butter", amount = 20.0, unit = "g")
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Herzhaftes Käse-Schinken-Toast",
                    description = "Der knusprige Snack-Klassiker mit saftig schmelzendem Käse und Schinken.",
                    category = "Schnell",
                    prepTimeMinutes = 10,
                    difficulty = "Einfach",
                    servings = 2,
                    instructions = "1. Brotscheiben dünn mit Butter bestreichen.\n2. Zwei Scheiben mit Schinken und reichlich Käse belegen.\n3. Die anderen Scheiben daraufklappen und im Kontaktgrill oder in der Pfanne knusprig backen, bis der Käse schmilzt."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Brot", amount = 4.0, unit = "Scheiben"),
                    RecipeIngredient(recipeId = 0, name = "Käse", amount = 4.0, unit = "Scheiben"),
                    RecipeIngredient(recipeId = 0, name = "Schinken", amount = 2.0, unit = "Scheiben"),
                    RecipeIngredient(recipeId = 0, name = "Butter", amount = 15.0, unit = "g")
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Wärmende Kartoffel-Lauch-Suppe",
                    description = "Sämige, wärmende Wohlfühlsuppe mit Kartoffeln, feinem Lauch und einem Schuss Sahne.",
                    category = "Suppen",
                    prepTimeMinutes = 30,
                    difficulty = "Einfach",
                    servings = 3,
                    instructions = "1. Kartoffeln schälen und würfeln, Zwiebeln hacken.\n2. In einem Topf mit Butter andünsten.\n3. Mit Gemüsebrühe aufgießen und 20 Minuten weich kochen.\n4. Fein pürieren, Sahne einrühren und mit Salz, Pfeffer und Muskat abschmecken."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Kartoffeln", amount = 500.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Zwiebeln", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Butter", amount = 20.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Sahne", amount = 100.0, unit = "ml")
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Mediterraner Bauernsalat",
                    description = "Knackig-frischer griechischer Salat mit sonnengereiften Tomaten, Gurke und cremigem Feta.",
                    category = "Vegetarisch",
                    prepTimeMinutes = 12,
                    difficulty = "Einfach",
                    servings = 2,
                    instructions = "1. Tomaten und Gurke in mundgerechte Stücke schneiden.\n2. Zwiebel in feine Ringe schneiden.\n3. Alles in eine Schüssel geben, Feta darüber bröckeln.\n4. Mit Olivenöl, Salz, Pfeffer und Oregano marinieren."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Tomaten", amount = 3.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Gurke", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Feta", amount = 150.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Zwiebeln", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Olivenöl", amount = 3.0, unit = "EL", isOptional = true)
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Gebratener Eierreis aus der Pfanne",
                    description = "Schnelle Resteverwertung: Knusprig gebratener Reis mit Ei, Gemüse und Sojasauce.",
                    category = "Pfanne",
                    prepTimeMinutes = 15,
                    difficulty = "Einfach",
                    servings = 2,
                    instructions = "1. Öl im Wok oder einer Pfanne erhitzen.\n2. Gehackte Zwiebeln und Knoblauch kurz anbraten.\n3. Den gekochten Reis hinzugeben und 3-4 Minuten anbraten.\n4. Reis an den Rand schieben, Eier in die Mitte schlagen und stocken lassen.\n5. Mit Reis verrühren und mit Sojasauce würzen."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Reis", amount = 200.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Eier", amount = 2.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Zwiebeln", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Knoblauch", amount = 1.0, unit = "Zehe")
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Cremiges Hähnchen-Curry",
                    description = "Zarte Hähnchenbrust in milder Kokos-Curry-Sauce mit buntem Gemüse.",
                    category = "Fleisch",
                    prepTimeMinutes = 25,
                    difficulty = "Mittel",
                    servings = 2,
                    instructions = "1. Reis kochen.\n2. Hähnchenbrust würfeln und in Öl scharf anbraten, dann herausnehmen.\n3. Zwiebeln und Paprika anbraten, Currypulver kurz mitrösten.\n4. Mit Kokosmilch ablöschen, Fleisch zurückgeben und 10 Minuten köcheln lassen.\n5. Mit Reis servieren."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Hähnchenbrust", amount = 300.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Reis", amount = 150.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Paprika", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Zwiebeln", amount = 1.0, unit = "Stück"),
                    RecipeIngredient(recipeId = 0, name = "Kokosmilch", amount = 200.0, unit = "ml")
                )
            ),
            SeedRecipe(
                recipe = Recipe(
                    title = "Apfel-Zimt Porridge",
                    description = "Warmes, nährendes Haferflocken-Frühstück mit frischem Apfel und duftendem Zimt.",
                    category = "Backen & Süßes",
                    prepTimeMinutes = 10,
                    difficulty = "Einfach",
                    servings = 1,
                    instructions = "1. Haferflocken mit Milch und einer Prise Salz in einen kleinen Topf geben.\n2. Bei mittlerer Hitze unter Rühren aufkochen, bis es cremig wird.\n3. Apfel in kleine Würfel schneiden und unterrühren.\n4. Mit Zimt und etwas Honig oder Zucker verfeinern."
                ),
                ingredients = listOf(
                    RecipeIngredient(recipeId = 0, name = "Haferflocken", amount = 80.0, unit = "g"),
                    RecipeIngredient(recipeId = 0, name = "Milch", amount = 200.0, unit = "ml"),
                    RecipeIngredient(recipeId = 0, name = "Apfel", amount = 1.0, unit = "Stück")
                )
            )
        )

        for (item in seedList) {
            val recipeId = recipeDao.insertRecipe(item.recipe)
            val ingredients = item.ingredients.map { it.copy(recipeId = recipeId) }
            recipeDao.insertIngredients(ingredients)
        }
    }
}
