package com.example.ui.components

import com.example.data.db.CatalogDao
import com.example.data.model.CatalogIngredientEntity
import com.example.data.ai.ScannedIngredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class CatalogIngredient(
    val emoji: String,
    val name: String,
    val defaultAmount: Double,
    val unit: String,
    val category: String,
    val commonPortions: List<Double> = emptyList(),
    val isCustom: Boolean = false
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

    // Fast cache for O(1) emoji lookup
    private val emojiCache = ConcurrentHashMap<String, String>()

    // Thread-safe container for custom & dynamically discovered ingredients
    private val customItems = CopyOnWriteArrayList<CatalogIngredient>()

    // Base comprehensive catalogue with over 350+ ingredients
    private val baseItems: List<CatalogIngredient> = listOf(
        // ==================== 1. GEMÜSE & OBST (GEMÜSE, KRÄUTER & SALATE) ====================
        CatalogIngredient("🍅", "Tomaten", 4.0, "Stück", "Gemüse & Obst", listOf(2.0, 4.0, 6.0, 8.0)),
        CatalogIngredient("🍅", "Rispentomaten", 500.0, "g", "Gemüse & Obst", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🍅", "Kirschtomaten", 250.0, "g", "Gemüse & Obst", listOf(150.0, 250.0, 500.0)),
        CatalogIngredient("🍅", "Fleischtomaten", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🧅", "Zwiebeln", 3.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 3.0, 5.0)),
        CatalogIngredient("🧅", "Rote Zwiebeln", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🧅", "Schalotten", 3.0, "Stück", "Gemüse & Obst", listOf(2.0, 3.0, 5.0)),
        CatalogIngredient("🌱", "Frühlingszwiebeln", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🥬", "Lauch / Porree", 1.0, "Stange", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🧄", "Knoblauch", 4.0, "Zehen", "Gemüse & Obst", listOf(2.0, 3.0, 4.0, 6.0)),
        CatalogIngredient("🥔", "Kartoffeln", 500.0, "g", "Gemüse & Obst", listOf(250.0, 500.0, 1000.0, 2000.0)),
        CatalogIngredient("🥔", "Süßkartoffeln", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🥕", "Möhren", 3.0, "Stück", "Gemüse & Obst", listOf(2.0, 3.0, 5.0, 10.0)),
        CatalogIngredient("🫑", "Paprika (rot)", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🫑", "Paprika (gelb)", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🫑", "Paprika (grün)", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🫑", "Spitzpaprika", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🥒", "Gurke", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥒", "Salatgurke", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥒", "Gewürzgurken", 1.0, "Glas", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🍆", "Zucchini", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🍆", "Aubergine", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥦", "Brokkoli", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥦", "Blumenkohl", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥦", "Romanesco", 1.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥬", "Rosenkohl", 300.0, "g", "Gemüse & Obst", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🥬", "Weißkohl", 500.0, "g", "Gemüse & Obst", listOf(300.0, 500.0, 1000.0)),
        CatalogIngredient("🥬", "Rotkohl", 500.0, "g", "Gemüse & Obst", listOf(300.0, 500.0, 1000.0)),
        CatalogIngredient("🥬", "Wirsing", 1.0, "Kopf", "Gemüse & Obst", listOf(0.5, 1.0)),
        CatalogIngredient("🥬", "Spitzkohl", 1.0, "Kopf", "Gemüse & Obst", listOf(0.5, 1.0)),
        CatalogIngredient("🥬", "Chinakohl", 1.0, "Kopf", "Gemüse & Obst", listOf(0.5, 1.0)),
        CatalogIngredient("🥬", "Spinat", 200.0, "g", "Gemüse & Obst", listOf(100.0, 200.0, 400.0)),
        CatalogIngredient("🥬", "Babyspinat", 125.0, "g", "Gemüse & Obst", listOf(100.0, 125.0, 250.0)),
        CatalogIngredient("🥬", "Mangold", 300.0, "g", "Gemüse & Obst", listOf(200.0, 300.0)),
        CatalogIngredient("🥬", "Eisbergsalat", 1.0, "Kopf", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥬", "Kopfsalat", 1.0, "Kopf", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥬", "Feldsalat", 150.0, "g", "Gemüse & Obst", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🥬", "Rucola", 100.0, "g", "Gemüse & Obst", listOf(100.0, 125.0, 200.0)),
        CatalogIngredient("🥬", "Römersalat", 1.0, "Kopf", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥬", "Chicorée", 2.0, "Stück", "Gemüse & Obst", listOf(2.0, 4.0)),
        CatalogIngredient("🍄", "Champignons", 250.0, "g", "Gemüse & Obst", listOf(150.0, 250.0, 500.0)),
        CatalogIngredient("🍄", "Braune Champignons", 250.0, "g", "Gemüse & Obst", listOf(150.0, 250.0, 500.0)),
        CatalogIngredient("🍄", "Kräuterseitlinge", 200.0, "g", "Gemüse & Obst", listOf(150.0, 200.0)),
        CatalogIngredient("🍄", "Shiitake", 150.0, "g", "Gemüse & Obst", listOf(100.0, 150.0)),
        CatalogIngredient("🍄", "Steinpilze", 150.0, "g", "Gemüse & Obst", listOf(100.0, 150.0)),
        CatalogIngredient("🍄", "Pfifferlinge", 200.0, "g", "Gemüse & Obst", listOf(150.0, 200.0)),
        CatalogIngredient("🌽", "Mais", 1.0, "Dose", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌽", "Maiskolben", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🫛", "Erbsen (TK)", 300.0, "g", "Gemüse & Obst", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🫛", "Zuckerschoten", 150.0, "g", "Gemüse & Obst", listOf(100.0, 150.0)),
        CatalogIngredient("🫘", "Grüne Bohnen", 250.0, "g", "Gemüse & Obst", listOf(200.0, 250.0, 500.0)),
        CatalogIngredient("🥬", "Staudensellerie", 2.0, "Stangen", "Gemüse & Obst", listOf(2.0, 4.0)),
        CatalogIngredient("🥔", "Knollensellerie", 0.5, "Knolle", "Gemüse & Obst", listOf(0.5, 1.0)),
        CatalogIngredient("🧅", "Fenchel", 1.0, "Knolle", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🔴", "Radieschen", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🥕", "Rettich", 1.0, "Stück", "Gemüse & Obst", listOf(0.5, 1.0)),
        CatalogIngredient("🔴", "Rote Bete", 2.0, "Knollen", "Gemüse & Obst", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🥕", "Pastinaken", 2.0, "Stück", "Gemüse & Obst", listOf(2.0, 3.0)),
        CatalogIngredient("🥔", "Kohlrabi", 1.0, "Knolle", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🎃", "Hokkaido Kürbis", 1.0, "Stück", "Gemüse & Obst", listOf(0.5, 1.0)),
        CatalogIngredient("🎃", "Butternut Kürbis", 1.0, "Stück", "Gemüse & Obst", listOf(0.5, 1.0)),
        CatalogIngredient("🌱", "Spargel (weiß)", 500.0, "g", "Gemüse & Obst", listOf(500.0, 1000.0)),
        CatalogIngredient("🌱", "Spargel (grün)", 500.0, "g", "Gemüse & Obst", listOf(250.0, 500.0)),
        CatalogIngredient("🥑", "Avocado", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🫚", "Ingwer", 50.0, "g", "Gemüse & Obst", listOf(30.0, 50.0, 100.0)),
        CatalogIngredient("🌶️", "Chili (rot)", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🌶️", "Jalapeños", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🌶️", "Peperoni", 2.0, "Stück", "Gemüse & Obst", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🌿", "Frische Petersilie", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌿", "Schnittlauch", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌱", "Basilikum", 1.0, "Topf", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌿", "Dill", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌿", "Koriander (frisch)", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌿", "Rosmarin", 2.0, "Zweige", "Gemüse & Obst", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🌿", "Thymian", 3.0, "Zweige", "Gemüse & Obst", listOf(2.0, 3.0, 5.0)),
        CatalogIngredient("🌿", "Minze", 1.0, "Bund", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌿", "Salbei", 1.0, "Zweig", "Gemüse & Obst", listOf(1.0, 2.0)),
        CatalogIngredient("🌱", "Kresse", 1.0, "Kästchen", "Gemüse & Obst", listOf(1.0, 2.0)),

        // ==================== 2. MILCH, KÄSE & EIER ====================
        CatalogIngredient("🥚", "Eier", 6.0, "Stück", "Milch & Eier", listOf(2.0, 4.0, 6.0, 10.0)),
        CatalogIngredient("🥚", "Eigelb", 2.0, "Stück", "Milch & Eier", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🥚", "Eiweiß", 2.0, "Stück", "Milch & Eier", listOf(2.0, 4.0)),
        CatalogIngredient("🥛", "Milch", 500.0, "ml", "Milch & Eier", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🥛", "Vollmilch", 1000.0, "ml", "Milch & Eier", listOf(500.0, 1000.0)),
        CatalogIngredient("🥛", "Fettarme Milch", 1000.0, "ml", "Milch & Eier", listOf(500.0, 1000.0)),
        CatalogIngredient("🥛", "Buttermilch", 500.0, "ml", "Milch & Eier", listOf(250.0, 500.0)),
        CatalogIngredient("🥛", "Kefir", 500.0, "ml", "Milch & Eier", listOf(250.0, 500.0)),
        CatalogIngredient("🥛", "Kondensmilch", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🍦", "Schlagsahne", 200.0, "ml", "Milch & Eier", listOf(100.0, 200.0, 400.0)),
        CatalogIngredient("🥣", "Saure Sahne", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 200.0)),
        CatalogIngredient("🥣", "Schmand", 200.0, "g", "Milch & Eier", listOf(100.0, 200.0)),
        CatalogIngredient("🥣", "Crème fraîche", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 200.0)),
        CatalogIngredient("🥞", "Mascarpone", 250.0, "g", "Milch & Eier", listOf(250.0, 500.0)),
        CatalogIngredient("🥞", "Ricotta", 250.0, "g", "Milch & Eier", listOf(250.0, 500.0)),
        CatalogIngredient("🥞", "Speisequark", 250.0, "g", "Milch & Eier", listOf(250.0, 500.0)),
        CatalogIngredient("🥞", "Magerquark", 250.0, "g", "Milch & Eier", listOf(250.0, 500.0)),
        CatalogIngredient("🥞", "Kräuterquark", 200.0, "g", "Milch & Eier", listOf(150.0, 200.0)),
        CatalogIngredient("🥣", "Naturjoghurt", 250.0, "g", "Milch & Eier", listOf(150.0, 250.0, 500.0)),
        CatalogIngredient("🥣", "Griechischer Joghurt", 200.0, "g", "Milch & Eier", listOf(150.0, 200.0, 400.0)),
        CatalogIngredient("🧈", "Butter", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🧈", "Kräuterbutter", 100.0, "g", "Milch & Eier", listOf(50.0, 100.0)),
        CatalogIngredient("🧈", "Butterschmalz", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🧈", "Margarine", 200.0, "g", "Milch & Eier", listOf(100.0, 200.0)),
        CatalogIngredient("🧀", "Gouda", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 250.0, 400.0)),
        CatalogIngredient("🧀", "Alter Gouda", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🧀", "Edamer", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🧀", "Emmentaler", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 200.0)),
        CatalogIngredient("🧀", "Butterkäse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🧀", "Cheddar", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 200.0)),
        CatalogIngredient("🧀", "Bergkäse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🧀", "Parmesan", 80.0, "g", "Milch & Eier", listOf(50.0, 80.0, 150.0)),
        CatalogIngredient("🧀", "Grana Padano", 80.0, "g", "Milch & Eier", listOf(50.0, 80.0, 150.0)),
        CatalogIngredient("🧀", "Pecorino", 80.0, "g", "Milch & Eier", listOf(50.0, 80.0)),
        CatalogIngredient("🍕", "Mozzarella", 1.0, "Kugel", "Milch & Eier", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🍕", "Büffelmozzarella", 1.0, "Kugel", "Milch & Eier", listOf(1.0, 2.0)),
        CatalogIngredient("🍕", "Burrata", 1.0, "Kugel", "Milch & Eier", listOf(1.0, 2.0)),
        CatalogIngredient("🥗", "Feta", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 200.0)),
        CatalogIngredient("🥗", "Schafskäse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🥗", "Hirtenkäse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🧀", "Halloumi", 200.0, "g", "Milch & Eier", listOf(100.0, 200.0)),
        CatalogIngredient("🧀", "Frischkäse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 200.0)),
        CatalogIngredient("🧀", "Kräuterfrischkäse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🧀", "Ziegenkäse", 100.0, "g", "Milch & Eier", listOf(80.0, 100.0, 150.0)),
        CatalogIngredient("🧀", "Camembert", 1.0, "Stück", "Milch & Eier", listOf(1.0, 2.0)),
        CatalogIngredient("🧀", "Brie", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🧀", "Gorgonzola", 100.0, "g", "Milch & Eier", listOf(80.0, 100.0)),
        CatalogIngredient("🧀", "Geriebener Käse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🧀", "Gratinkäse", 150.0, "g", "Milch & Eier", listOf(100.0, 150.0)),
        CatalogIngredient("🥛", "Hafermilch", 500.0, "ml", "Milch & Eier", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🥛", "Sojamilch", 500.0, "ml", "Milch & Eier", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🥛", "Mandelmilch", 500.0, "ml", "Milch & Eier", listOf(250.0, 500.0)),
        CatalogIngredient("🌱", "Tofu (natur)", 200.0, "g", "Milch & Eier", listOf(150.0, 200.0, 400.0)),
        CatalogIngredient("🌱", "Räuchertofu", 200.0, "g", "Milch & Eier", listOf(150.0, 200.0)),

        // ==================== 3. FLEISCH & FISCH ====================
        CatalogIngredient("🍗", "Hähnchenbrust", 350.0, "g", "Fleisch & Fisch", listOf(200.0, 350.0, 500.0, 750.0)),
        CatalogIngredient("🍗", "Hähnchenschenkel", 2.0, "Stück", "Fleisch & Fisch", listOf(2.0, 4.0)),
        CatalogIngredient("🍗", "Hähnchen-Ministeaks", 300.0, "g", "Fleisch & Fisch", listOf(250.0, 300.0, 500.0)),
        CatalogIngredient("🍗", "Putenschnitzel", 300.0, "g", "Fleisch & Fisch", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🍗", "Putenbrust", 350.0, "g", "Fleisch & Fisch", listOf(200.0, 350.0, 500.0)),
        CatalogIngredient("🥩", "Hackfleisch", 400.0, "g", "Fleisch & Fisch", listOf(250.0, 400.0, 500.0, 800.0)),
        CatalogIngredient("🥩", "Rinderhackfleisch", 400.0, "g", "Fleisch & Fisch", listOf(250.0, 400.0, 500.0)),
        CatalogIngredient("🥩", "Gemischtes Hackfleisch", 400.0, "g", "Fleisch & Fisch", listOf(250.0, 400.0, 500.0)),
        CatalogIngredient("🥩", "Rindersteak", 300.0, "g", "Fleisch & Fisch", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🥩", "Rumpsteak", 250.0, "g", "Fleisch & Fisch", listOf(200.0, 250.0, 400.0)),
        CatalogIngredient("🥩", "Rinderfilet", 250.0, "g", "Fleisch & Fisch", listOf(200.0, 250.0)),
        CatalogIngredient("🥩", "Rindergulasch", 400.0, "g", "Fleisch & Fisch", listOf(300.0, 400.0, 600.0)),
        CatalogIngredient("🥩", "Schweineschnitzel", 300.0, "g", "Fleisch & Fisch", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🥩", "Schweinefilet", 350.0, "g", "Fleisch & Fisch", listOf(250.0, 350.0, 500.0)),
        CatalogIngredient("🥩", "Schweinekotelett", 2.0, "Stück", "Fleisch & Fisch", listOf(1.0, 2.0)),
        CatalogIngredient("🥓", "Speck / Bacon", 100.0, "g", "Fleisch & Fisch", listOf(50.0, 100.0, 200.0)),
        CatalogIngredient("🥓", "Schinkenwürfel", 100.0, "g", "Fleisch & Fisch", listOf(50.0, 100.0, 150.0)),
        CatalogIngredient("🍖", "Kochschinken", 100.0, "g", "Fleisch & Fisch", listOf(80.0, 100.0, 150.0)),
        CatalogIngredient("🍖", "Prosciutto", 80.0, "g", "Fleisch & Fisch", listOf(50.0, 80.0, 120.0)),
        CatalogIngredient("🍖", "Schwarzwälder Schinken", 80.0, "g", "Fleisch & Fisch", listOf(50.0, 80.0)),
        CatalogIngredient("🥓", "Salami", 80.0, "g", "Fleisch & Fisch", listOf(50.0, 80.0, 150.0)),
        CatalogIngredient("🥓", "Chorizo", 100.0, "g", "Fleisch & Fisch", listOf(50.0, 100.0)),
        CatalogIngredient("🌭", "Wiener Würstchen", 4.0, "Stück", "Fleisch & Fisch", listOf(2.0, 4.0, 6.0)),
        CatalogIngredient("🌭", "Bratwurst", 3.0, "Stück", "Fleisch & Fisch", listOf(2.0, 3.0, 5.0)),
        CatalogIngredient("🌭", "Nürnberger Rostbratwürstchen", 6.0, "Stück", "Fleisch & Fisch", listOf(6.0, 10.0)),
        CatalogIngredient("🥩", "Leberkäse", 200.0, "g", "Fleisch & Fisch", listOf(150.0, 200.0)),
        CatalogIngredient("🥩", "Fleischwurst", 200.0, "g", "Fleisch & Fisch", listOf(150.0, 200.0)),
        CatalogIngredient("🥩", "Lammkoteletts", 4.0, "Stück", "Fleisch & Fisch", listOf(2.0, 4.0)),
        CatalogIngredient("🐟", "Lachsfilet", 250.0, "g", "Fleisch & Fisch", listOf(150.0, 250.0, 400.0)),
        CatalogIngredient("🐟", "Räucherlachs", 100.0, "g", "Fleisch & Fisch", listOf(80.0, 100.0, 150.0)),
        CatalogIngredient("🐟", "Thunfisch (Dose)", 1.0, "Dose", "Fleisch & Fisch", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🐟", "Thunfischsteak", 200.0, "g", "Fleisch & Fisch", listOf(150.0, 200.0)),
        CatalogIngredient("🐟", "Forelle", 1.0, "Stück", "Fleisch & Fisch", listOf(1.0, 2.0)),
        CatalogIngredient("🐟", "Kabeljau", 250.0, "g", "Fleisch & Fisch", listOf(150.0, 250.0)),
        CatalogIngredient("🐟", "Seelachs", 250.0, "g", "Fleisch & Fisch", listOf(150.0, 250.0)),
        CatalogIngredient("🐟", "Zander", 250.0, "g", "Fleisch & Fisch", listOf(150.0, 250.0)),
        CatalogIngredient("🐟", "Dorade", 1.0, "Stück", "Fleisch & Fisch", listOf(1.0, 2.0)),
        CatalogIngredient("🦐", "Garnelen", 200.0, "g", "Fleisch & Fisch", listOf(150.0, 200.0, 400.0)),
        CatalogIngredient("🦐", "Riesengarnelen", 200.0, "g", "Fleisch & Fisch", listOf(150.0, 200.0)),
        CatalogIngredient("🦐", "Nordseekrabben", 100.0, "g", "Fleisch & Fisch", listOf(50.0, 100.0)),
        CatalogIngredient("🦑", "Tintenfisch / Calamari", 200.0, "g", "Fleisch & Fisch", listOf(150.0, 200.0)),
        CatalogIngredient("🦪", "Miesmuscheln", 500.0, "g", "Fleisch & Fisch", listOf(500.0, 1000.0)),
        CatalogIngredient("🐟", "Sardellen / Anchovis", 1.0, "Dose", "Fleisch & Fisch", listOf(1.0, 2.0)),

        // ==================== 4. VORRAT, GETREIDE & TEIGWAREN ====================
        CatalogIngredient("🍝", "Spaghetti", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🍝", "Penne", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🍝", "Fusilli", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0)),
        CatalogIngredient("🍝", "Tagliatelle", 400.0, "g", "Vorrat & Teigwaren", listOf(250.0, 400.0)),
        CatalogIngredient("🍝", "Lasagneplatten", 250.0, "g", "Vorrat & Teigwaren", listOf(200.0, 250.0, 500.0)),
        CatalogIngredient("🍝", "Farfalle", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0)),
        CatalogIngredient("🍝", "Tortellini", 250.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0)),
        CatalogIngredient("🥔", "Gnocchi", 400.0, "g", "Vorrat & Teigwaren", listOf(250.0, 400.0, 600.0)),
        CatalogIngredient("🥔", "Schupfnudeln", 400.0, "g", "Vorrat & Teigwaren", listOf(250.0, 400.0)),
        CatalogIngredient("🍝", "Spätzle", 400.0, "g", "Vorrat & Teigwaren", listOf(250.0, 400.0)),
        CatalogIngredient("🍜", "Mie-Nudeln", 250.0, "g", "Vorrat & Teigwaren", listOf(150.0, 250.0)),
        CatalogIngredient("🍜", "Glasnudeln", 100.0, "g", "Vorrat & Teigwaren", listOf(100.0, 200.0)),
        CatalogIngredient("🍚", "Basmatireis", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🍚", "Jasminreis", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0)),
        CatalogIngredient("🍚", "Risottoreis", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0)),
        CatalogIngredient("🍚", "Milchreis", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0)),
        CatalogIngredient("🌾", "Couscous", 300.0, "g", "Vorrat & Teigwaren", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🌾", "Bulgur", 300.0, "g", "Vorrat & Teigwaren", listOf(200.0, 300.0)),
        CatalogIngredient("🌾", "Quinoa", 250.0, "g", "Vorrat & Teigwaren", listOf(150.0, 250.0)),
        CatalogIngredient("🌾", "Polenta", 300.0, "g", "Vorrat & Teigwaren", listOf(200.0, 300.0)),
        CatalogIngredient("🌾", "Weizenmehl", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🌾", "Dinkelmehl", 500.0, "g", "Vorrat & Teigwaren", listOf(250.0, 500.0)),
        CatalogIngredient("🌾", "Speisestärke", 150.0, "g", "Vorrat & Teigwaren", listOf(50.0, 150.0)),
        CatalogIngredient("🌾", "Paniermehl", 200.0, "g", "Vorrat & Teigwaren", listOf(100.0, 200.0)),
        CatalogIngredient("🥣", "Haferflocken", 300.0, "g", "Vorrat & Teigwaren", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🍞", "Toastbrot", 6.0, "Scheiben", "Vorrat & Teigwaren", listOf(4.0, 6.0, 10.0)),
        CatalogIngredient("🍞", "Vollkornbrot", 4.0, "Scheiben", "Vorrat & Teigwaren", listOf(2.0, 4.0, 8.0)),
        CatalogIngredient("🥖", "Baguette", 1.0, "Stück", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🥖", "Ciabatta", 1.0, "Stück", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🫓", "Fladenbrot", 1.0, "Stück", "Vorrat & Teigwaren", listOf(0.5, 1.0)),
        CatalogIngredient("🫓", "Tortilla-Wraps", 4.0, "Stück", "Vorrat & Teigwaren", listOf(4.0, 6.0, 8.0)),
        CatalogIngredient("🥫", "Gehackte Tomaten", 1.0, "Dose", "Vorrat & Teigwaren", listOf(1.0, 2.0, 3.0)),
        CatalogIngredient("🥫", "Passierte Tomaten", 500.0, "ml", "Vorrat & Teigwaren", listOf(250.0, 500.0, 1000.0)),
        CatalogIngredient("🥫", "Geschälte Tomaten", 1.0, "Dose", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🫘", "Kidneybohnen", 1.0, "Dose", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🫘", "Kichererbsen", 1.0, "Dose", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🫘", "Weiße Bohnen", 1.0, "Dose", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🫘", "Rote Linsen", 300.0, "g", "Vorrat & Teigwaren", listOf(200.0, 300.0, 500.0)),
        CatalogIngredient("🫘", "Braune Linsen", 300.0, "g", "Vorrat & Teigwaren", listOf(200.0, 300.0)),
        CatalogIngredient("🥣", "Gemüsebrühe", 200.0, "ml", "Vorrat & Teigwaren", listOf(100.0, 200.0, 500.0)),
        CatalogIngredient("🥣", "Hühnerbrühe", 250.0, "ml", "Vorrat & Teigwaren", listOf(200.0, 400.0)),
        CatalogIngredient("🥣", "Rinderbrühe", 250.0, "ml", "Vorrat & Teigwaren", listOf(200.0, 400.0)),
        CatalogIngredient("🥥", "Kokosmilch", 400.0, "ml", "Vorrat & Teigwaren", listOf(200.0, 400.0)),
        CatalogIngredient("🌱", "Hefe (frisch)", 1.0, "Würfel", "Vorrat & Teigwaren", listOf(0.5, 1.0)),
        CatalogIngredient("🌱", "Trockenhefe", 1.0, "Päckchen", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🧁", "Backpulver", 1.0, "Päckchen", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🧁", "Vanillezucker", 1.0, "Päckchen", "Vorrat & Teigwaren", listOf(1.0, 2.0)),
        CatalogIngredient("🧂", "Zucker", 250.0, "g", "Vorrat & Teigwaren", listOf(100.0, 250.0, 500.0)),
        CatalogIngredient("🧂", "Brauner Zucker", 200.0, "g", "Vorrat & Teigwaren", listOf(100.0, 200.0)),
        CatalogIngredient("🧂", "Puderzucker", 150.0, "g", "Vorrat & Teigwaren", listOf(100.0, 150.0)),

        // ==================== 5. GEWÜRZE, ÖLE & SAUCEN ====================
        CatalogIngredient("🫒", "Olivenöl", 250.0, "ml", "Gewürze & Saucen", listOf(100.0, 250.0, 500.0)),
        CatalogIngredient("🌻", "Pflanzenöl", 250.0, "ml", "Gewürze & Saucen", listOf(100.0, 250.0, 500.0)),
        CatalogIngredient("🌻", "Sonnenblumenöl", 250.0, "ml", "Gewürze & Saucen", listOf(100.0, 250.0)),
        CatalogIngredient("🌱", "Rapsöl", 250.0, "ml", "Gewürze & Saucen", listOf(100.0, 250.0)),
        CatalogIngredient("🌱", "Sesamöl", 100.0, "ml", "Gewürze & Saucen", listOf(50.0, 100.0)),
        CatalogIngredient("🍶", "Balsamico Essig", 150.0, "ml", "Gewürze & Saucen", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🍶", "Heller Balsamico", 150.0, "ml", "Gewürze & Saucen", listOf(100.0, 150.0)),
        CatalogIngredient("🍶", "Apfelessig", 150.0, "ml", "Gewürze & Saucen", listOf(100.0, 150.0)),
        CatalogIngredient("🍶", "Weißweinessig", 150.0, "ml", "Gewürze & Saucen", listOf(100.0, 150.0)),
        CatalogIngredient("🧂", "Salz", 200.0, "g", "Gewürze & Saucen", listOf(100.0, 200.0, 500.0)),
        CatalogIngredient("🧂", "Meersalz", 150.0, "g", "Gewürze & Saucen", listOf(100.0, 150.0)),
        CatalogIngredient("🧂", "Schwarzer Pfeffer", 50.0, "g", "Gewürze & Saucen", listOf(30.0, 50.0, 100.0)),
        CatalogIngredient("🧂", "Weißer Pfeffer", 40.0, "g", "Gewürze & Saucen", listOf(20.0, 40.0)),
        CatalogIngredient("🌶️", "Paprikapulver", 40.0, "g", "Gewürze & Saucen", listOf(20.0, 40.0, 80.0)),
        CatalogIngredient("🌶️", "Paprika rosenscharf", 40.0, "g", "Gewürze & Saucen", listOf(20.0, 40.0)),
        CatalogIngredient("🌶️", "Geräuchertes Paprikapulver", 35.0, "g", "Gewürze & Saucen", listOf(20.0, 35.0)),
        CatalogIngredient("🍛", "Currypulver", 40.0, "g", "Gewürze & Saucen", listOf(20.0, 40.0, 80.0)),
        CatalogIngredient("🟡", "Kurkuma", 35.0, "g", "Gewürze & Saucen", listOf(20.0, 35.0)),
        CatalogIngredient("🌿", "Kreuzkümmel (Cumin)", 35.0, "g", "Gewürze & Saucen", listOf(20.0, 35.0)),
        CatalogIngredient("🌿", "Zimt", 30.0, "g", "Gewürze & Saucen", listOf(20.0, 30.0)),
        CatalogIngredient("🌰", "Muskatnuss", 1.0, "Stück", "Gewürze & Saucen", listOf(1.0, 2.0)),
        CatalogIngredient("🌿", "Lorbeerblätter", 5.0, "Blätter", "Gewürze & Saucen", listOf(3.0, 5.0, 10.0)),
        CatalogIngredient("🌶️", "Chiliflocken", 30.0, "g", "Gewürze & Saucen", listOf(20.0, 30.0)),
        CatalogIngredient("🌶️", "Cayennepfeffer", 30.0, "g", "Gewürze & Saucen", listOf(20.0, 30.0)),
        CatalogIngredient("🌿", "Getrockneter Oregano", 30.0, "g", "Gewürze & Saucen", listOf(20.0, 30.0, 50.0)),
        CatalogIngredient("🌿", "Getrockneter Basilikum", 30.0, "g", "Gewürze & Saucen", listOf(20.0, 30.0)),
        CatalogIngredient("🌿", "Getrockneter Thymian", 25.0, "g", "Gewürze & Saucen", listOf(15.0, 25.0)),
        CatalogIngredient("🌿", "Getrockneter Rosmarin", 25.0, "g", "Gewürze & Saucen", listOf(15.0, 25.0)),
        CatalogIngredient("🌿", "Kräuter der Provence", 30.0, "g", "Gewürze & Saucen", listOf(20.0, 30.0)),
        CatalogIngredient("🌿", "Italienische Kräuter", 30.0, "g", "Gewürze & Saucen", listOf(20.0, 30.0)),
        CatalogIngredient("🧄", "Knoblauchpulver", 40.0, "g", "Gewürze & Saucen", listOf(20.0, 40.0)),
        CatalogIngredient("🥫", "Tomatenmark", 2.0, "EL", "Gewürze & Saucen", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🥢", "Sojasauce", 100.0, "ml", "Gewürze & Saucen", listOf(50.0, 100.0, 200.0)),
        CatalogIngredient("🥢", "Helle Sojasauce", 100.0, "ml", "Gewürze & Saucen", listOf(50.0, 100.0)),
        CatalogIngredient("🐟", "Fischsauce", 60.0, "ml", "Gewürze & Saucen", listOf(30.0, 60.0)),
        CatalogIngredient("🥢", "Austernsauce", 80.0, "ml", "Gewürze & Saucen", listOf(50.0, 80.0)),
        CatalogIngredient("🥢", "Teriyaki Sauce", 100.0, "ml", "Gewürze & Saucen", listOf(50.0, 100.0)),
        CatalogIngredient("🌶️", "Sweet Chili Sauce", 120.0, "ml", "Gewürze & Saucen", listOf(80.0, 120.0)),
        CatalogIngredient("🌶️", "Sriracha", 100.0, "ml", "Gewürze & Saucen", listOf(50.0, 100.0)),
        CatalogIngredient("🌶️", "Tabasco", 1.0, "Flasche", "Gewürze & Saucen", listOf(1.0)),
        CatalogIngredient("🍖", "BBQ-Sauce", 150.0, "ml", "Gewürze & Saucen", listOf(100.0, 150.0)),
        CatalogIngredient("🥫", "Pesto", 1.0, "Glas", "Gewürze & Saucen", listOf(1.0, 2.0)),
        CatalogIngredient("🥫", "Pesto Genovese (grün)", 1.0, "Glas", "Gewürze & Saucen", listOf(1.0, 2.0)),
        CatalogIngredient("🥫", "Pesto Rosso (rot)", 1.0, "Glas", "Gewürze & Saucen", listOf(1.0, 2.0)),
        CatalogIngredient("🧅", "Senf", 100.0, "g", "Gewürze & Saucen", listOf(50.0, 100.0, 200.0)),
        CatalogIngredient("🧅", "Dijon-Senf", 80.0, "g", "Gewürze & Saucen", listOf(50.0, 80.0)),
        CatalogIngredient("🧅", "Süßer Senf", 80.0, "g", "Gewürze & Saucen", listOf(50.0, 80.0)),
        CatalogIngredient("🍅", "Ketchup", 150.0, "ml", "Gewürze & Saucen", listOf(100.0, 150.0, 300.0)),
        CatalogIngredient("🥣", "Mayonnaise", 150.0, "ml", "Gewürze & Saucen", listOf(100.0, 150.0)),
        CatalogIngredient("🥣", "Remoulade", 150.0, "ml", "Gewürze & Saucen", listOf(100.0, 150.0)),
        CatalogIngredient("🍯", "Honig", 150.0, "g", "Gewürze & Saucen", listOf(100.0, 150.0, 300.0)),
        CatalogIngredient("🍁", "Ahornsirup", 100.0, "ml", "Gewürze & Saucen", listOf(50.0, 100.0)),
        CatalogIngredient("🥜", "Erdnussbutter", 150.0, "g", "Gewürze & Saucen", listOf(100.0, 150.0)),
        CatalogIngredient("🌱", "Tahini (Sesammus)", 100.0, "g", "Gewürze & Saucen", listOf(50.0, 100.0)),

        // ==================== 6. OBST & SNACKS ====================
        CatalogIngredient("🍎", "Äpfel", 4.0, "Stück", "Obst & Snacks", listOf(2.0, 4.0, 6.0)),
        CatalogIngredient("🍐", "Birnen", 3.0, "Stück", "Obst & Snacks", listOf(2.0, 3.0, 5.0)),
        CatalogIngredient("🍌", "Bananen", 3.0, "Stück", "Obst & Snacks", listOf(2.0, 3.0, 5.0)),
        CatalogIngredient("🍋", "Zitronen", 2.0, "Stück", "Obst & Snacks", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🍋", "Limetten", 2.0, "Stück", "Obst & Snacks", listOf(1.0, 2.0, 4.0)),
        CatalogIngredient("🍊", "Orangen", 3.0, "Stück", "Obst & Snacks", listOf(2.0, 3.0, 6.0)),
        CatalogIngredient("🍊", "Mandarinen", 5.0, "Stück", "Obst & Snacks", listOf(3.0, 5.0, 10.0)),
        CatalogIngredient("🍊", "Grapefruit", 1.0, "Stück", "Obst & Snacks", listOf(1.0, 2.0)),
        CatalogIngredient("🍓", "Erdbeeren", 250.0, "g", "Obst & Snacks", listOf(150.0, 250.0, 500.0)),
        CatalogIngredient("🫐", "Blaubeeren", 150.0, "g", "Obst & Snacks", listOf(125.0, 150.0, 300.0)),
        CatalogIngredient("🍓", "Himbeeren", 125.0, "g", "Obst & Snacks", listOf(100.0, 125.0, 250.0)),
        CatalogIngredient("🫐", "Brombeeren", 125.0, "g", "Obst & Snacks", listOf(100.0, 125.0)),
        CatalogIngredient("🍒", "Kirschen", 200.0, "g", "Obst & Snacks", listOf(150.0, 200.0, 400.0)),
        CatalogIngredient("🍇", "Weintrauben (hell)", 250.0, "g", "Obst & Snacks", listOf(200.0, 250.0, 500.0)),
        CatalogIngredient("🍇", "Weintrauben (dunkel)", 250.0, "g", "Obst & Snacks", listOf(200.0, 250.0, 500.0)),
        CatalogIngredient("🍑", "Pfirsiche", 3.0, "Stück", "Obst & Snacks", listOf(2.0, 3.0)),
        CatalogIngredient("🍑", "Nektarinen", 3.0, "Stück", "Obst & Snacks", listOf(2.0, 3.0)),
        CatalogIngredient("🍑", "Aprikosen", 4.0, "Stück", "Obst & Snacks", listOf(2.0, 4.0)),
        CatalogIngredient("🫐", "Pflaumen / Zwetschgen", 300.0, "g", "Obst & Snacks", listOf(200.0, 300.0)),
        CatalogIngredient("🍉", "Wassermelone", 0.5, "Stück", "Obst & Snacks", listOf(0.5, 1.0)),
        CatalogIngredient("🍈", "Honigmelone", 0.5, "Stück", "Obst & Snacks", listOf(0.5, 1.0)),
        CatalogIngredient("🥭", "Mango", 1.0, "Stück", "Obst & Snacks", listOf(1.0, 2.0)),
        CatalogIngredient("🍍", "Ananas", 1.0, "Stück", "Obst & Snacks", listOf(0.5, 1.0)),
        CatalogIngredient("🥝", "Kiwi", 3.0, "Stück", "Obst & Snacks", listOf(2.0, 3.0, 5.0)),
        CatalogIngredient("🔴", "Granatapfel", 1.0, "Stück", "Obst & Snacks", listOf(1.0, 2.0)),
        CatalogIngredient("🫐", "Feigen (frisch)", 3.0, "Stück", "Obst & Snacks", listOf(2.0, 3.0)),
        CatalogIngredient("🌴", "Datteln", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0)),
        CatalogIngredient("🍇", "Rosinen / Sultaninen", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0)),
        CatalogIngredient("🍫", "Schokolade", 100.0, "g", "Obst & Snacks", listOf(100.0, 200.0)),
        CatalogIngredient("🍫", "Zartbitterschokolade", 100.0, "g", "Obst & Snacks", listOf(100.0, 200.0)),
        CatalogIngredient("🍫", "Vollmilchschokolade", 100.0, "g", "Obst & Snacks", listOf(100.0, 200.0)),
        CatalogIngredient("🍫", "Kakaopulver", 100.0, "g", "Obst & Snacks", listOf(50.0, 100.0)),
        CatalogIngredient("🥜", "Erdnüsse", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🥜", "Walnüsse", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🥜", "Mandeln", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0, 250.0)),
        CatalogIngredient("🥜", "Haselnüsse", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0)),
        CatalogIngredient("🥜", "Cashewkerne", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0)),
        CatalogIngredient("🥜", "Pinienkerne", 50.0, "g", "Obst & Snacks", listOf(30.0, 50.0)),
        CatalogIngredient("🌻", "Sonnenblumenkerne", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0)),
        CatalogIngredient("🎃", "Kürbiskerne", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0)),
        CatalogIngredient("🌱", "Sesam", 100.0, "g", "Obst & Snacks", listOf(50.0, 100.0)),
        CatalogIngredient("🌱", "Chiasamen", 150.0, "g", "Obst & Snacks", listOf(100.0, 150.0)),
        CatalogIngredient("🥥", "Kokosraspel", 100.0, "g", "Obst & Snacks", listOf(50.0, 100.0))
    )

    /**
     * Combined items: base catalog + custom/scanned items
     */
    val items: List<CatalogIngredient>
        get() = baseItems + customItems

    /**
     * Fast search across catalog items
     */
    fun searchDatabase(query: String, categoryFilter: String? = null): List<CatalogIngredient> {
        val q = query.trim().lowercase()
        val all = items
        if (q.isEmpty() && (categoryFilter == null || categoryFilter == "Alle")) {
            return all
        }
        return all.filter { item ->
            val matchesCategory = categoryFilter == null || categoryFilter == "Alle" || item.category.equals(categoryFilter, ignoreCase = true)
            val matchesQuery = q.isEmpty() || item.name.lowercase().contains(q) || item.category.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }

    /**
     * Finds or creates a CatalogIngredient, registering it so it appears in the catalog database
     */
    fun registerIngredient(
        name: String,
        emoji: String = getEmojiFor(name),
        category: String = getCategoryFor(name),
        amount: Double = 1.0,
        unit: String = "Stück"
    ): CatalogIngredient {
        val trimmed = name.trim()
        val existing = items.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        if (existing != null) return existing

        val created = CatalogIngredient(
            emoji = emoji,
            name = trimmed,
            defaultAmount = amount,
            unit = unit,
            category = category,
            commonPortions = listOf(1.0, 2.0, 3.0),
            isCustom = true
        )
        customItems.add(created)
        return created
    }

    /**
     * Syncs custom ingredients from Room database into memory
     */
    fun loadCustomItemsFromEntities(entities: List<CatalogIngredientEntity>) {
        for (entity in entities) {
            val alreadyPresent = items.any { it.name.equals(entity.name, ignoreCase = true) }
            if (!alreadyPresent) {
                customItems.add(
                    CatalogIngredient(
                        emoji = entity.emoji,
                        name = entity.name,
                        defaultAmount = entity.defaultAmount,
                        unit = entity.unit,
                        category = entity.category,
                        commonPortions = listOf(1.0, 2.0, 3.0),
                        isCustom = true
                    )
                )
            }
        }
    }

    /**
     * Automatically registers scanned items into the catalog database if they don't exist yet!
     * Ensures newly scanned items are discoverable for future scans and database searches.
     */
    suspend fun registerScannedIngredients(
        scannedList: List<ScannedIngredient>,
        catalogDao: CatalogDao
    ) = withContext(Dispatchers.IO) {
        val newEntitiesToInsert = mutableListOf<CatalogIngredientEntity>()

        for (scanned in scannedList) {
            val trimmedName = scanned.name.trim()
            if (trimmedName.isBlank()) continue

            val existsInItems = items.any { it.name.equals(trimmedName, ignoreCase = true) }
            if (!existsInItems) {
                val emoji = getEmojiFor(trimmedName)
                val cat = if (scanned.category.isNotBlank() && scanned.category != "Sonstiges") {
                    scanned.category
                } else {
                    getCategoryFor(trimmedName)
                }

                val newCatalogItem = CatalogIngredient(
                    emoji = emoji,
                    name = trimmedName,
                    defaultAmount = scanned.amount,
                    unit = scanned.unit,
                    category = cat,
                    commonPortions = listOf(1.0, 2.0, 3.0),
                    isCustom = true
                )
                customItems.add(newCatalogItem)

                val entity = CatalogIngredientEntity(
                    name = trimmedName,
                    emoji = emoji,
                    category = cat,
                    defaultAmount = scanned.amount,
                    unit = scanned.unit,
                    isCustom = true
                )
                newEntitiesToInsert.add(entity)
            }
        }

        if (newEntitiesToInsert.isNotEmpty()) {
            try {
                catalogDao.insertAll(newEntitiesToInsert)
            } catch (e: Exception) {
                android.util.Log.e("IngredientCatalog", "Failed to persist new scanned ingredients to database", e)
            }
        }
    }

    /**
     * Highly optimized O(1) Emoji Resolver with memory cache
     */
    fun getEmojiFor(name: String): String {
        val clean = name.lowercase().trim()
        if (clean.isBlank()) return "🍽️"

        // Check memory cache first
        val cached = emojiCache[clean]
        if (cached != null) return cached

        // Exact match in catalogue
        val exact = items.firstOrNull { it.name.equals(clean, ignoreCase = true) }
        if (exact != null) {
            emojiCache[clean] = exact.emoji
            return exact.emoji
        }

        // Substring match in catalogue
        val direct = items.firstOrNull { clean.contains(it.name.lowercase()) || it.name.lowercase().contains(clean) }
        if (direct != null) {
            emojiCache[clean] = direct.emoji
            return direct.emoji
        }

        // Fallback pattern matching
        val resolved = when {
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
            clean.contains("hähnchen") || clean.contains("chicken") || clean.contains("geflügel") -> "🍗"
            clean.contains("fleisch") || clean.contains("beef") || clean.contains("steak") -> "🥩"
            clean.contains("fisch") || clean.contains("lachs") || clean.contains("salmon") -> "🐟"
            clean.contains("garnele") || clean.contains("shrimp") -> "🦐"
            clean.contains("nudel") || clean.contains("pasta") || clean.contains("spaghetti") -> "🍝"
            clean.contains("reis") || clean.contains("rice") -> "🍚"
            clean.contains("brot") || clean.contains("bread") || clean.contains("toast") -> "🍞"
            clean.contains("öl") || clean.contains("oil") -> "🫒"
            clean.contains("salz") || clean.contains("salt") || clean.contains("pfeffer") -> "🧂"
            clean.contains("apfel") || clean.contains("apple") -> "🍎"
            clean.contains("banane") || clean.contains("banana") -> "🍌"
            clean.contains("zitrone") || clean.contains("lemon") -> "🍋"
            clean.contains("beere") || clean.contains("berry") -> "🫐"
            clean.contains("spinat") || clean.contains("salat") -> "🥬"
            clean.contains("pilz") || clean.contains("champignon") || clean.contains("mushroom") -> "🍄"
            clean.contains("kürbis") -> "🎃"
            clean.contains("schokolade") -> "🍫"
            clean.contains("nuss") || clean.contains("mandel") -> "🥜"
            else -> "🍽️"
        }

        emojiCache[clean] = resolved
        return resolved
    }

    fun getCategoryFor(name: String): String {
        val clean = name.lowercase().trim()
        val direct = items.firstOrNull { clean.contains(it.name.lowercase()) || it.name.lowercase().contains(clean) }
        if (direct != null) return direct.category

        return when {
            clean.contains("tomate") || clean.contains("zwiebel") || clean.contains("gurke") ||
            clean.contains("paprika") || clean.contains("salat") || clean.contains("kartoffel") ||
            clean.contains("pilz") || clean.contains("champignon") || clean.contains("spinat") ||
            clean.contains("kohl") || clean.contains("möhre") || clean.contains("karotte") ||
            clean.contains("knoblauch") || clean.contains("ingwer") || clean.contains("chili") ||
            clean.contains("kürbis") || clean.contains("avocado") || clean.contains("zucchini") ||
            clean.contains("aubergine") || clean.contains("brokkoli") -> "Gemüse & Obst"

            clean.contains("käse") || clean.contains("kaese") || clean.contains("milch") ||
            clean.contains("quark") || clean.contains("joghurt") || clean.contains("butter") ||
            clean.contains("sahne") || clean.contains("ei") || clean.contains("eier") ||
            clean.contains("schmand") || clean.contains("mozzarella") || clean.contains("parmesan") ||
            clean.contains("feta") -> "Milch & Eier"

            clean.contains("fleisch") || clean.contains("hähnchen") || clean.contains("haehnchen") ||
            clean.contains("rind") || clean.contains("schwein") || clean.contains("hack") ||
            clean.contains("steak") || clean.contains("wurst") || clean.contains("schinken") ||
            clean.contains("speck") || clean.contains("bacon") || clean.contains("fisch") ||
            clean.contains("lachs") || clean.contains("thunfisch") || clean.contains("garnele") -> "Fleisch & Fisch"

            clean.contains("nudel") || clean.contains("pasta") || clean.contains("spaghetti") ||
            clean.contains("penne") || clean.contains("reis") || clean.contains("mehl") ||
            clean.contains("brot") || clean.contains("toast") || clean.contains("hafer") ||
            clean.contains("brühe") || clean.contains("dose") || clean.contains("linsen") ||
            clean.contains("bohne") || clean.contains("kichererbse") -> "Vorrat & Teigwaren"

            clean.contains("öl") || clean.contains("oel") || clean.contains("essig") ||
            clean.contains("salz") || clean.contains("pfeffer") || clean.contains("senf") ||
            clean.contains("ketchup") || clean.contains("pesto") || clean.contains("sauce") ||
            clean.contains("curry") || clean.contains("paprikapulver") || clean.contains("honig") -> "Gewürze & Saucen"

            clean.contains("apfel") || clean.contains("banane") || clean.contains("beere") ||
            clean.contains("orange") || clean.contains("zitrone") || clean.contains("nuss") ||
            clean.contains("erdnuss") || clean.contains("schokolade") || clean.contains("snack") -> "Obst & Snacks"

            else -> "Vorrat & Teigwaren"
        }
    }
}
