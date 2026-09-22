package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.RecipeIngredient

data class NewIngredientDraft(
    val name: String = "",
    val amount: String = "1",
    val unit: String = "Stück"
)

@Composable
fun AddRecipeDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: String,
        prepTimeMinutes: Int,
        difficulty: String,
        servings: Int,
        instructions: String,
        ingredients: List<RecipeIngredient>
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Kreativ") }
    var prepTimeText by remember { mutableStateOf("20") }
    var instructions by remember { mutableStateOf("") }

    val ingredients = remember {
        mutableStateListOf(
            NewIngredientDraft("", "200", "g"),
            NewIngredientDraft("", "2", "Stück")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Neues Rezept erstellen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Rezeptname (z. B. Pasta Primavera)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Kurzbeschreibung") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategorie") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = prepTimeText,
                        onValueChange = { prepTimeText = it },
                        label = { Text("Dauer (Min)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Zutatenliste:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp)
                )

                ingredients.forEachIndexed { index, ing ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = ing.name,
                                onValueChange = { newName ->
                                    ingredients[index] = ing.copy(name = newName)
                                },
                                label = { Text("Zutat") },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f)
                            )
                            OutlinedTextField(
                                value = ing.amount,
                                onValueChange = { newAmount ->
                                    ingredients[index] = ing.copy(amount = newAmount)
                                },
                                label = { Text("Menge") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(0.9f)
                            )
                            OutlinedTextField(
                                value = ing.unit,
                                onValueChange = { newUnit ->
                                    ingredients[index] = ing.copy(unit = newUnit)
                                },
                                label = { Text("Einh.") },
                                singleLine = true,
                                modifier = Modifier.weight(0.9f)
                            )
                            if (ingredients.size > 1) {
                                IconButton(onClick = { ingredients.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Entfernen", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { ingredients.add(NewIngredientDraft("", "1", "Stück")) },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Zutat hinzufügen")
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Zubereitungsschritte (eine Zeile pro Schritt)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validIngredients = ingredients
                        .filter { it.name.isNotBlank() }
                        .map {
                            RecipeIngredient(
                                recipeId = 0,
                                name = it.name.trim(),
                                amount = it.amount.toDoubleOrNull() ?: 1.0,
                                unit = it.unit.trim()
                            )
                        }

                    if (title.isNotBlank() && validIngredients.isNotEmpty()) {
                        val prepTime = prepTimeText.toIntOrNull() ?: 20
                        val steps = if (instructions.isBlank()) "Zutaten mischen und nach Belieben zubereiten." else instructions
                        onSave(
                            title.trim(),
                            if (description.isBlank()) "Leckeres Gericht mit eigenen Zutaten" else description.trim(),
                            category.trim(),
                            prepTime,
                            "Einfach",
                            2,
                            steps,
                            validIngredients
                        )
                    }
                },
                enabled = title.isNotBlank() && ingredients.any { it.name.isNotBlank() }
            ) {
                Text("Rezept speichern")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
