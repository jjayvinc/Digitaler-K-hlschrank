package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.FridgeRecipeRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFridgeItemDialog(
    prefilledName: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, unit: String, category: String) -> Unit,
    onOpenScan: ((com.example.data.ai.ScanType) -> Unit)? = null
) {
    var name by remember { mutableStateOf(prefilledName ?: "") }
    var amount by remember { mutableDoubleStateOf(1.0) }
    var selectedUnit by remember { mutableStateOf("Stück") }
    var selectedCategory by remember {
        mutableStateOf(
            if (prefilledName != null) FridgeRecipeRepository.getCategoryForIngredient(prefilledName)
            else "Gemüse & Obst"
        )
    }

    var selectedCatalogCategoryIndex by remember { mutableIntStateOf(0) }
    var showManualInput by remember { mutableStateOf(prefilledName != null) }

    val currentEmoji = remember(name) {
        if (name.isNotBlank()) IngredientCatalog.getEmojiFor(name) else "🥗"
    }

    val units = listOf(
        "Stück" to "📦",
        "g" to "⚖️",
        "kg" to "⚖️",
        "ml" to "🥛",
        "L" to "🥛",
        "EL" to "🥄",
        "TL" to "🥄",
        "Zehen" to "🧄",
        "Scheiben" to "🍞",
        "Dose" to "🥫",
        "Packung" to "📦",
        "Bund" to "🌿"
    )

    val currentCategoryTitle = IngredientCatalog.allCategories.getOrNull(selectedCatalogCategoryIndex)?.title ?: "Gemüse & Obst"
    val filteredCatalogItems = remember(currentCategoryTitle) {
        IngredientCatalog.items.filter { it.category == currentCategoryTitle }
    }

    // Matching suggestions based on name typed
    val suggestions = remember(name) {
        if (name.length >= 2) {
            IngredientCatalog.items.filter {
                it.name.contains(name, ignoreCase = true) && !it.name.equals(name, ignoreCase = true)
            }.take(4)
        } else {
            emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🥗", fontSize = 22.sp)
                    }
                    Column {
                        Text(
                            text = "Zutat in Kühlschrank legen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tippe eine Zutat an oder tippe manuell",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Clear, contentDescription = "Schließen")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // AI Fast Scan Switch Banner
                if (onOpenScan != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("✨", fontSize = 16.sp)
                                Text(
                                    text = "Schneller per Foto-Scan:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        onDismiss()
                                        onOpenScan(com.example.data.ai.ScanType.FRIDGE)
                                    }
                                ) {
                                    Text(
                                        text = "📸 Kühlschrank",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.clickable {
                                        onDismiss()
                                        onOpenScan(com.example.data.ai.ScanType.GROCERY_PURCHASE)
                                    }
                                ) {
                                    Text(
                                        text = "🧾 Einkauf",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. LIVE VISUAL PREVIEW CARD
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Large Emoji Bubble
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentEmoji, fontSize = 32.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (name.isNotBlank()) name else "Wähle oder tippe eine Zutat…",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (name.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "${FridgeRecipeRepository.formatAmount(amount)} $selectedUnit",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = selectedCategory,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. VISUAL INGREDIENT CATALOG (Regal)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Anschaulicher Zutaten-Katalog",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showManualInput = !showManualInput }) {
                            Text(if (showManualInput) "Katalog fokussieren" else "Eigene Zutat tippen ✍️")
                        }
                    }

                    // Category Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedCatalogCategoryIndex,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        IngredientCatalog.allCategories.forEachIndexed { index, cat ->
                            Tab(
                                selected = selectedCatalogCategoryIndex == index,
                                onClick = { selectedCatalogCategoryIndex = index },
                                text = {
                                    Text(
                                        text = "${cat.emoji} ${cat.title.split(" ")[0]}",
                                        fontWeight = if (selectedCatalogCategoryIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    // Grid of Visual Ingredient Badges for selected category
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredCatalogItems.forEach { item ->
                            val isSelected = name.equals(item.name, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                tonalElevation = if (isSelected) 4.dp else 1.dp,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable {
                                        name = item.name
                                        amount = item.defaultAmount
                                        selectedUnit = item.unit
                                        selectedCategory = item.category
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = item.emoji, fontSize = 26.sp)
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                    Text(
                                        text = "${FridgeRecipeRepository.formatAmount(item.defaultAmount)} ${item.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. SEARCH / MANUAL INPUT WITH SUGGESTIONS
                AnimatedVisibility(visible = showManualInput || name.isBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                if (it.isNotBlank()) {
                                    selectedCategory = FridgeRecipeRepository.getCategoryForIngredient(it)
                                }
                            },
                            label = { Text("Oder Zutat selbst eingeben (z. B. Brokkoli)") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                if (name.isNotEmpty()) {
                                    IconButton(onClick = { name = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Löschen")
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ingredient_name_input")
                        )

                        // Visual suggestion pills
                        if (suggestions.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                suggestions.forEach { sug ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            name = sug.name
                                            amount = sug.defaultAmount
                                            selectedUnit = sug.unit
                                            selectedCategory = sug.category
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(sug.emoji)
                                            Text(sug.name, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. QUANTITY STEPPER & QUICK-ADD
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Menge anpassen",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Stepper row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    val step = when (selectedUnit) {
                                        "g" -> if (amount > 100) 50.0 else 10.0
                                        "ml" -> if (amount > 100) 50.0 else 25.0
                                        else -> 1.0
                                    }
                                    amount = (amount - step).coerceAtLeast(1.0)
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Verringern")
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = FridgeRecipeRepository.formatAmount(amount),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = selectedUnit,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    val step = when (selectedUnit) {
                                        "g" -> if (amount >= 100) 50.0 else 25.0
                                        "ml" -> if (amount >= 100) 50.0 else 50.0
                                        else -> 1.0
                                    }
                                    amount += step
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Erhöhen")
                            }
                        }

                        // Quick portion pills
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val portions = when (selectedUnit) {
                                "g" -> listOf(50.0, 100.0, 200.0, 250.0, 500.0)
                                "ml" -> listOf(100.0, 200.0, 250.0, 500.0, 1000.0)
                                "EL", "TL" -> listOf(1.0, 2.0, 3.0, 4.0)
                                else -> listOf(1.0, 2.0, 3.0, 4.0, 6.0, 10.0)
                            }
                            portions.forEach { p ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (amount == p) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { amount = p }
                                ) {
                                    Text(
                                        text = "${FridgeRecipeRepository.formatAmount(p)} $selectedUnit",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (amount == p) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. UNIT SELECTION
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Einheit wählen",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        units.forEach { (unitName, icon) ->
                            FilterChip(
                                selected = selectedUnit == unitName,
                                onClick = { selectedUnit = unitName },
                                label = { Text("$icon $unitName") }
                            )
                        }
                    }
                }

                // 6. CATEGORY SELECTION
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Kategorie im Kühlschrank",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val allCats = listOf(
                            "🥦 Gemüse & Obst",
                            "🧀 Milch & Eier",
                            "🥩 Fleisch & Fisch",
                            "🌾 Vorrat & Teigwaren",
                            "🧂 Gewürze & Saucen",
                            "📦 Sonstiges"
                        )
                        allCats.forEach { fullCat ->
                            val cleanName = fullCat.substring(2).trim()
                            FilterChip(
                                selected = selectedCategory == cleanName,
                                onClick = { selectedCategory = cleanName },
                                label = { Text(fullCat) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), amount, selectedUnit, selectedCategory)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_fridge_item_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                Text("In den Kühlschrank")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
