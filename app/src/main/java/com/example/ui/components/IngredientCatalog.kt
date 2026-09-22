package com.example.ui.components

import com.example.data.repository.FridgeRecipeRepository

data class CatalogIngredient(
    val emoji: String,
    val name: String,
    val defaultAmount: Double,
    val unit: String,
    val category: String,
    val commonPortions: List<Double> = emptyList()
)

object IngredientCatalog {

    val allCategories = listOf(
        CategoryInfo("🥦", "Gemüse & Obst", "Frisches Gemüse, Kräuter & Salate"),
        CategoryInfo("🧀", "Milch & Eier", "Milchprodukte, Käse & Eier"),
        CategoryInfo("🥩", "Fleisch & Fisch", "Geflügel, Rind, Fisch & Meeresfrüchte"),
        CategoryInfo("🌾", "Vorrat & Teigwaren", "Nudeln, Reis, Mehl & Konserven"),
        CategoryInfo("🧂", "Gewürze & Saucen", "Öle, Essig, Gewürze & Würzmittel"),
        CategoryInfo("🍎", "Obst & Snacks", "Früchte, Nüsse & Backzutaten")
    )

    data class CategoryInfo(
        val emoji: String,
        val title: String,
        val subtitle: String
    )

    val items = listOf(
        // Gemüse & Obst
        CatalogIngredient("🍅", "Tomaten", 4.0, "Stück", "Gemüse & Obst", listOf(2.0, 4.0, 6.0, 8.0)),
        CatalogIngredient("🧅", "Zwiebeln", 3.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 3.0, 5.0)),
        CatalogIngredient("🧄", "Knoblauch", 4.0, "Zehen", "Gemüse & Obst", listOf(2.0, 3.0, 4.0, 6.0)),
        CatalogIngredient("🥔", "Kartoffeln", 500.0, "g", "Gemüse & Obst", listOf(250.0, 500.0, 1000.0, 1500.0)),
        CatalogIngredient("🥕", "Möhren", 3.0, "Stück", "Gemüse & Obst", listOf(2.0, 3.0, 5.0, 10.0)),
        CatalogIngredient("🫑", "Paprika", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 3.0, 5.0)),
        CatalogIngredient("🥒", "Gurke", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥬", "Spinat", 200.0, "g", "Gemüse & Obst", listOf(100.0, 200.0, 400.0)),
        CatalogIngredient("🍄", "Champignons", 250.0, "g", "Gemüse & Obst", listOf(150.0, 250.0, 500.0)),
        CatalogIngredient("🥑", "Avocado", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🥦", "Brokkoli", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌽", "Mais", 1.0, "Dose", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🍆", "Zucchini", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🍆", "Aubergine", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥦", "Blumenkohl", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥬", "Eisbergsalat", 1.0, "Kopf", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌱", "Frühlingszwiebeln", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌿", "Frische Petersilie", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌱", "Basilikum", 1.0, "Topf", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌿", "Dill", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌿", "Schnittlauch", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🫚", "Ingwer", 50.0, "g", "Gemüse & Obst", listOf(30.0, 50.0, 100.0)),
        CatalogIngredient("🌶️", "Chili", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 4.0)),

        // Milch & Eier
        CatalogIngredient("🥚", "Eier", 6.0, "Stück", "Milch & Eier", listOf(2.0, 4.0, 6.0, 10.0)),
        CatalogIngredient("🥛", "Milch", 500.0, "ml", "Milch & Eier", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🧀", "Käse (Gouda)", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 250.0, 400.0)),
        CatalogIngredient("🧀", "Parmesan", 80.0, "g", "Milch & Eier", listOf(50.0, 80.0, 150.0)),
        CatalogIngredient("🍕", "Mozzarella", 1.0, "Kugel", "Milch & Eier", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🧈", "Butter", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🥣", "Naturjoghurt", 250.0, "g", "Milch & Eier", listOf(150.0, 250.0, 500.0)),
        CatalogIngredient("🍦", "Schlagsahne", 200.0, "ml", "Milch & Eier", listOf(100.0, 200.0, 400.0)),
        CatalogIngredient("🥗", "Feta", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 200.0)),
        CatalogIngredient("🥞", "Speisequark", 250.0, "g", "Milch & Eier", listOf(250.0, 500.0)),
        CatalogIngredient("🧀", "Frischkäse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 200.0)),
        CatalogIngredient("🥣", "Schmand / Crème fraîche", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 200.0)),
        CatalogIngredient("🧀", "Geriebener Käse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🥛", "Hafermilch", 500.0, "ml", "Milch & Eier", listOf(250.0, 500.0, 1000.0)),

        // Fleisch & Fisch
        CatalogIngredient("🍗", "Hähnchenbrust", 350.0, "g", "Fleisch & Fisch", listOf(200.0, 350.0, 500.0, 750.0)),
        CatalogIngredient("🥩", "Hackfleisch", 400.0, "g", "Fleisch & Fisch", listOf(250.0, 400.0, 500.0, 800.0)),
        CatalogIngredient("🥩", "Rindersteak", 300.0, "g", "Fleisch & Fisch", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🥓", "Speck / Bacon", 100.0, "g", "Fleisch & Fisch", listOf(50.0, 100.0, 200.0)),
        CatalogIngredient("🍖", "Kochschinken", 100.0, "g", "Fleisch & Fisch", listOf(80.0, 100.0, 150.0)),
        CatalogIngredient("🥓", "Salami", 80.0, "g", "Fleisch & Fisch", listOf(50.0, 80.0, 150.0)),
        CatalogIngredient("🐟", "Lachsfilet", 250.0, "g", "Fleisch & Fisch", listOf(150.0, 250.0, 400.0)),
        CatalogIngredient("🐟", "Thunfisch", 1.0, "Dose", "Fleisch & Fisch", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🦐", "Garnelen", 200.0, "g", "Fleisch & Fisch", listOf(150.0, 200.0, 400.0)),
        CatalogIngredient("🌭", "Wiener Würstchen", 4.0, "Stück", "Fleisch & Fisch", listOf(2.0, 4.0, 6.0)),
        CatalogIngredient("🌱", "Tofu", 200.0, "g", "Fleisch & Fisch", listOf(150.0, 200.0, 400.0)),

        // Vorrat & Teigwaren
        CatalogIngredient("🍝", "Spaghetti", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🍝", "Penne", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🍚", "Basmatireis", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🍞", "Toastbrot", 6.0, "Scheiben", "Vorrat & Teigwaren", listOf(4.0, 6.0, 10.0)),
        CatalogIngredient("🥖", "Baguette", 1.0, "Stück", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🌾", "Weizenmehl", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🥣", "Haferflocken", 300.0, "g", "Vorrat & Teigwaren", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🥫", "Gehackte Tomaten", 1.0, "Dose", "Vorrat & Teigwaren", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🥫", "Passierte Tomaten", 500.0, "ml", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🫘", "Kidneybohnen", 1.0, "Dose", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🫘", "Kichererbsen", 1.0, "Dose", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🥣", "Gemüsebrühe", 200.0, "ml", "Vorrat & Teigwaren", listOf(100.0, 200.0, 500.0)),
        CatalogIngredient("🥥", "Kokosmilch", 400.0, "ml", "Vorrat & Teigwaren", listOf(200.0, 400.0)),

        // Gewürze & Saucen
        CatalogIngredient("🫒", "Olivenöl", 250.0, "ml", "Gewürze & Saucen", listOf(100.0, 250.0, 500.0)),
        CatalogIngredient("🌻", "Pflanzenöl", 250.0, "ml", "Gewürze & Saucen", listOf(100.0, 250.0, 500.0)),
        CatalogIngredient("🧂", "Salz", 200.0, "g", "Gewürze & Saucen", listOf(100.0, 200.0, 500.0)),
        CatalogIngredient("🧂", "Schwarzer Pfeffer", 50.0, "g", "Gewürze & Saucen", listOf(30.0, 50.0, 100.0)),
        CatalogIngredient("🥫", "Tomatenmark", 2.0, "EL", "Gewürze & Saucen", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🥢", "Sojasauce", 100.0, "ml", "Gewürze & Saucen", listOf(50.0, 100.0, 200.0)),
        CatalogIngredient("🍯", "Honig", 150.0, "g", "Gewürze & Saucen", listOf(100.0, 150.0, 300.0)),
        CatalogIngredient("🧅", "Senf", 100.0, "g", "Gewürze & Saucen", listOf(50.0, 100.0, 200.0)),
        CatalogIngredient("🍶", "Balsamico Essig", 150.0, "ml", "Gewürze & Saucen", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🌿", "Getrockneter Oregano", 30.0, "g", "Gewürze & Saucen", listOf(20.0, 30.0, 50.0)),
        CatalogIngredient("🌶️", "Paprikapulver", 40.0, "g", "Gewürze & Saucen", listOf(20.0, 40.0, 80.0)),
        CatalogIngredient("🍛", "Currypulver", 40.0, "g", "Gewürze & Saucen", listOf(20.0, 40.0, 80.0)),
        CatalogIngredient("🥫", "Pesto", 1.0, "Glas", "Gewürze & Saucen", listOf(1.0, 2.0)),

        // Obst & Snacks
        CatalogIngredient("🍎", "Äpfel", 4.0, "Stück", "Obst & Snacks", listOf(2.0, 4.0, 6.0)),
        CatalogIngredient("🍌", "Bananen", 3.0, "Stück", "Obst & Snacks", listOf(2.0, 3.0, 5.0)),
        CatalogIngredient("🍋", "Zitronen", 2.0, "Stück", "Obst & Snacks", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🍓", "Erdbeeren", 250.0, "g", "Obst & Snacks", listOf(150.0, 250.0, 500.0)),
        CatalogIngredient("🫐", "Blaubeeren", 150.0, "g", "Obst & Snacks", listOf(125.0, 150.0, 300.0)),
        CatalogIngredient("🍊", "Orangen", 3.0, "Stück", "Obst & Snacks", listOf(2.0, 3.0, 6.0)),
        CatalogIngredient("🍫", "Schokolade", 100.0, "g", "Obst & Snacks", listOf(100.0, 200.0)),
        CatalogIngredient("🥜", "Erdnüsse", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🥜", "Walnüsse", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0, 250.0))
    )

    fun searchDatabase(query: String, categoryFilter: String? = null): List<CatalogIngredient> {
        val q = query.trim().lowercase()
        return items.filter { item ->
            val matchesCategory = categoryFilter == null || categoryFilter == "Alle" || item.category.equals(categoryFilter, ignoreCase = true)
            val matchesQuery = q.isEmpty() || item.name.lowercase().contains(q) || item.category.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }

    fun getEmojiFor(name: String): String {
        val clean = name.lowercase().trim()
        val direct = items.firstOrNull { clean.contains(it.name.lowercase()) || it.name.lowercase().contains(clean) }
        if (direct != null) return direct.emoji

        return when {
            clean.contains("ei") || clean.contains("egg") -> "🥚"
            clean.contains("milch") || clean.contains("milk") -> "🥛"
            clean.contains("käse") || clean.contains("kaese") || clean.contains("cheese") -> "🧀"
            clean.contains("butter") -> "🧈"
            clean.contains("tomate") -> "🍅"
            clean.contains("zwiebel") || clean.contains("onion") -> "🧅"
            clean.contains("knoblauch") || clean.contains("garlic") -> "🧄"
            clean.contains("kartoffel") || clean.contains("potato") -> "🥔"
            clean.contains("möhre") || clean.contains("karotte") || clean.contains("carrot") -> "🥕"
            clean.contains("paprika") || clean.contains("pepper") -> "🫑"
            clean.contains("gurke") || clean.contains("cucumber") -> "🥒"
            clean.contains("hähnchen") || clean.contains("chicken") -> "🍗"
            clean.contains("fleisch") || clean.contains("beef") || clean.contains("steak") -> "🥩"
            clean.contains("fisch") || clean.contains("lachs") || clean.contains("salmon") -> "🐟"
            clean.contains("nudel") || clean.contains("pasta") || clean.contains("spaghetti") -> "🍝"
            clean.contains("reis") || clean.contains("rice") -> "🍚"
            clean.contains("brot") || clean.contains("bread") || clean.contains("toast") -> "🍞"
            clean.contains("öl") || clean.contains("oil") -> "🫒"
            clean.contains("salz") || clean.contains("salt") || clean.contains("pfeffer") -> "🧂"
            clean.contains("apfel") || clean.contains("apple") -> "🍎"
            clean.contains("banane") || clean.contains("banana") -> "🍌"
            clean.contains("zitrone") || clean.contains("lemon") -> "🍋"
            clean.contains("spinat") || clean.contains("salat") -> "🥬"
            clean.contains("pilz") || clean.contains("champignon") || clean.contains("mushroom") -> "🍄"
            else -> "🍽️"
        }
    }
}
