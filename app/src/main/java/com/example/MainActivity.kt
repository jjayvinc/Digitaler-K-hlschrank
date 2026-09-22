package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddFridgeItemDialog
import com.example.ui.components.AddRecipeDialog
import com.example.ui.components.AiScanDialog
import com.example.ui.components.RecipeDetailSheet
import com.example.ui.screens.FridgeScreen
import com.example.ui.screens.RecipesScreen
import com.example.ui.screens.ShoppingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.FridgeViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: FridgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: FridgeViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val fridgeItems by viewModel.fridgeItems.collectAsStateWithLifecycle()
    val cookableCount by viewModel.cookableRecipesCount.collectAsStateWithLifecycle()
    val shoppingCount by viewModel.shoppingItemsCount.collectAsStateWithLifecycle()

    val showAddFridgeDialog by viewModel.showAddFridgeDialog.collectAsStateWithLifecycle()
    val prefilledFridgeName by viewModel.prefilledFridgeName.collectAsStateWithLifecycle()
    val selectedRecipe by viewModel.selectedRecipe.collectAsStateWithLifecycle()
    val showAddRecipeDialog by viewModel.showAddRecipeDialog.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Listen to ViewModel snackbar events
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            AppTab.FRIDGE -> "Digitaler Kühlschrank"
                            AppTab.RECIPES -> "Rezept-Vorschläge"
                            AppTab.SHOPPING -> "Einkaufsliste"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                // Tab 1: Kühlschrank
                NavigationBarItem(
                    selected = currentTab == AppTab.FRIDGE,
                    onClick = { viewModel.selectTab(AppTab.FRIDGE) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (fridgeItems.isNotEmpty()) {
                                    Badge { Text("${fridgeItems.size}") }
                                }
                            }
                        ) {
                            Icon(
                                if (currentTab == AppTab.FRIDGE) Icons.Filled.Kitchen else Icons.Outlined.Kitchen,
                                contentDescription = "Kühlschrank"
                            )
                        }
                    },
                    label = { Text("Kühlschrank") },
                    modifier = Modifier.testTag("nav_fridge")
                )

                // Tab 2: Rezepte
                NavigationBarItem(
                    selected = currentTab == AppTab.RECIPES,
                    onClick = { viewModel.selectTab(AppTab.RECIPES) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cookableCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("$cookableCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                if (currentTab == AppTab.RECIPES) Icons.Filled.RestaurantMenu else Icons.Outlined.RestaurantMenu,
                                contentDescription = "Rezepte"
                            )
                        }
                    },
                    label = { Text("Rezepte") },
                    modifier = Modifier.testTag("nav_recipes")
                )

                // Tab 3: Einkaufsliste
                NavigationBarItem(
                    selected = currentTab == AppTab.SHOPPING,
                    onClick = { viewModel.selectTab(AppTab.SHOPPING) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (shoppingCount > 0) {
                                    Badge(containerColor = MaterialTheme.colorScheme.secondary) {
                                        Text("$shoppingCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                if (currentTab == AppTab.SHOPPING) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                contentDescription = "Einkauf"
                            )
                        }
                    },
                    label = { Text("Einkauf") },
                    modifier = Modifier.testTag("nav_shopping")
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.testTag("app_snackbar_host")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.FRIDGE -> FridgeScreen(viewModel = viewModel)
                AppTab.RECIPES -> RecipesScreen(viewModel = viewModel)
                AppTab.SHOPPING -> ShoppingScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Sheet for Recipe Detail & Cooking Action
    if (selectedRecipe != null) {
        RecipeDetailSheet(
            matchResult = selectedRecipe!!,
            sheetState = sheetState,
            onDismiss = { viewModel.closeRecipeDetail() },
            onCookRecipe = { match -> viewModel.cookRecipe(match) },
            onAddMissingToShoppingList = { match -> viewModel.addMissingToShoppingList(match) }
        )
    }

    // AI Scanner Dialog (Fridge scan / Grocery scan)
    val showScanDialog by viewModel.showScanDialog.collectAsStateWithLifecycle()
    showScanDialog?.let { scanType ->
        AiScanDialog(
            scanType = scanType,
            aiService = viewModel.aiService,
            onDismiss = { viewModel.closeScanDialog() },
            onConfirmAdd = { items, replaceFridge ->
                viewModel.applyScannedIngredients(items, replaceFridge, scanType)
            }
        )
    }

    // Dialog for Adding/Editing Fridge Item
    if (showAddFridgeDialog) {
        AddFridgeItemDialog(
            prefilledName = prefilledFridgeName,
            onDismiss = { viewModel.closeAddFridgeDialog() },
            onConfirm = { name, amount, unit, category ->
                viewModel.addFridgeItem(name, amount, unit, category)
                viewModel.closeAddFridgeDialog()
            },
            onOpenScan = { scanType ->
                viewModel.openScanDialog(scanType)
            }
        )
    }

    // Dialog for Adding Custom Recipe
    if (showAddRecipeDialog) {
        AddRecipeDialog(
            onDismiss = { viewModel.closeAddRecipeDialog() },
            onSave = { title, desc, cat, time, diff, serv, steps, ings ->
                viewModel.addCustomRecipe(title, desc, cat, time, diff, serv, steps, ings)
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

