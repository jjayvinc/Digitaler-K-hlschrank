package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.AiVisionService
import com.example.data.ai.ScanResult
import com.example.data.ai.ScanType
import com.example.data.ai.ScannedIngredient
import com.example.data.repository.FridgeRecipeRepository
import kotlinx.coroutines.launch

@Composable
fun AiScanDialog(
    scanType: ScanType,
    aiService: AiVisionService,
    onDismiss: () -> Unit,
    onConfirmAdd: (List<ScannedIngredient>, Boolean) -> Unit // items, replaceFridge(for fridge scan)
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<ScanResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showProviderSettings by remember { mutableStateOf(false) }
    var showDatabaseSearchDialog by remember { mutableStateOf(false) }
    var replaceExistingFridge by remember { mutableStateOf(false) }

    val scannedItems = remember { mutableStateListOf<ScannedIngredient>() }

    // Detect if running on an emulator
    val isLikelyEmulator = remember {
        Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("google_sdk") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for") ||
        Build.HARDWARE.contains("goldfish") ||
        Build.HARDWARE.contains("ranchu")
    }

    // Check if camera hardware / intent is supported on this device
    val isCameraAvailable = remember {
        val hasFeature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val canResolve = cameraIntent.resolveActivity(context.packageManager) != null
        hasFeature || canResolve
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            isScanning = true
            errorMessage = null
            coroutineScope.launch {
                try {
                    val res = aiService.scanImage(bitmap, scanType)
                    scanResult = res
                    scannedItems.clear()
                    scannedItems.addAll(res.ingredients)
                } catch (e: Exception) {
                    errorMessage = "Erkennung fehlgeschlagen: ${e.localizedMessage ?: "Unbekannter Fehler"}"
                } finally {
                    isScanning = false
                }
            }
        } else {
            // User backed out or emulator camera produced no output
            errorMessage = "Kein Foto aufgenommen. Auf dem Emulator kannst du gerne die Galerie oder das Beispiel verwenden."
        }
    }

    // Safe camera launch helper
    fun tryLaunchCamera() {
        errorMessage = null
        try {
            cameraLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
            errorMessage = "Keine Kamera-App gefunden. Auf diesem Emulator ist keine Kamera eingerichtet – bitte wähle ein Bild aus der Galerie oder probiere das Beispiel aus."
        } catch (e: SecurityException) {
            errorMessage = "Kamerazugriff wurde vom System verweigert (${e.localizedMessage})."
        } catch (e: Exception) {
            errorMessage = "Kamera konnte nicht gestartet werden: ${e.localizedMessage ?: "Unbekannter Fehler"}"
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tryLaunchCamera()
        } else {
            errorMessage = "Kamera-Berechtigung wurde nicht erteilt. Du kannst stattdessen ein Foto aus der Galerie wählen oder das Beispiel testen."
        }
    }

    fun onCameraClick() {
        errorMessage = null
        if (!isCameraAvailable && isLikelyEmulator) {
            errorMessage = "Im Emulator steht leider keine Kamera bereit. Nutze einfach die Galerie oder das interaktive Beispiel!"
            return
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            tryLaunchCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
                if (bitmap != null) {
                    selectedBitmap = bitmap
                    isScanning = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val res = aiService.scanImage(bitmap, scanType)
                            scanResult = res
                            scannedItems.clear()
                            scannedItems.addAll(res.ingredients)
                        } catch (e: Exception) {
                            errorMessage = "Erkennung fehlgeschlagen: ${e.localizedMessage ?: "Unbekannter Fehler"}"
                        } finally {
                            isScanning = false
                        }
                    }
                } else {
                    errorMessage = "Bild konnte nicht dekodiert werden."
                }
            } catch (e: Exception) {
                errorMessage = "Bild konnte nicht geladen werden: ${e.localizedMessage}"
            }
        }
    }

    // Helper to run demo scan (ideal for emulator)
    fun runDemoScan() {
        val demoBitmap = createDemoBitmap(scanType)
        selectedBitmap = demoBitmap
        isScanning = true
        errorMessage = null
        coroutineScope.launch {
            kotlinx.coroutines.delay(1000)
            val res = aiService.generateDemoExample(scanType)
            scanResult = res
            scannedItems.clear()
            scannedItems.addAll(res.ingredients)
            isScanning = false
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (scanType == ScanType.FRIDGE) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (scanType == ScanType.FRIDGE) "📸" else "🧾",
                            fontSize = 22.sp
                        )
                    }
                    Column {
                        Text(
                            text = if (scanType == ScanType.FRIDGE) "Kühlschrank scannen" else "Einkauf scannen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (scanType == ScanType.FRIDGE)
                                "Zutaten werden automatisch erkannt"
                            else
                                "Artikel werden dem Kühlschrank hinzugefügt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showProviderSettings = true },
                        modifier = Modifier.testTag("scan_settings_button")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "KI-Einstellungen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Clear, contentDescription = "Schließen")
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Error message banner
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { errorText ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⚠️", fontSize = 18.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = errorText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    if (errorText.contains("API-Schlüssel", ignoreCase = true) || errorText.contains("Einstellungen", ignoreCase = true)) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { showProviderSettings = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Einstellungen öffnen", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { errorMessage = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Schließen",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 1. Photo selection if no image yet
                if (selectedBitmap == null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Active Provider Indicator
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showProviderSettings = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(aiService.activeProvider.badge, fontSize = 14.sp)
                                        Text(
                                            text = "KI: ${aiService.activeProvider.displayName}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "⚙️ Anpassen",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Text(
                                text = if (scanType == ScanType.FRIDGE)
                                    "Mache ein Foto von deinem geöffneten Kühlschrank oder wähle ein Bild aus der Galerie."
                                else
                                    "Mache ein Foto deines Kassenbons oder deines Einkaufs auf der Küchentheke.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isLikelyEmulator && !isCameraAvailable) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("💡", fontSize = 14.sp)
                                        Text(
                                            text = "Im Emulator verfügbar: Galerie oder das interaktive Beispiel!",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onCameraClick() },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scan_camera_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kamera", maxLines = 1)
                                }

                                OutlinedButton(
                                    onClick = {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scan_gallery_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Galerie", maxLines = 1)
                                }
                            }

                            // Quick Demo Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { runDemoScan() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("✨", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                                    Text(
                                        text = if (scanType == ScanType.FRIDGE) "Beispiel-Kühlschrank ausprobieren" else "Beispiel-Kassenbon ausprobieren",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Image preview with scanning animation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Scan-Vorschau",
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isScanning) {
                            // Animated Radar Scanning Line
                            val infiniteTransition = rememberInfiniteTransition(label = "scan_beam")
                            val beamProgress by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "beam_progress"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.TopCenter)
                                    .padding(top = (beamProgress * 126).dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.primary,
                                                Color.White,
                                                MaterialTheme.colorScheme.primary,
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // Status badge
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Black.copy(alpha = 0.85f),
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = "🔍 KI analysiert dein Foto...",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = aiService.activeProvider.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        } else {
                            // Retake button on top-right
                            IconButton(
                                onClick = {
                                    selectedBitmap = null
                                    scanResult = null
                                    scannedItems.clear()
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Neues Foto", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Scan Result List
                    if (!isScanning && scannedItems.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gefundene Zutaten (${scannedItems.count { it.isSelected }}/${scannedItems.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "✨ Automatisch erkannt",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (scanType == ScanType.GROCERY_PURCHASE) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🛒", fontSize = 14.sp)
                                    Text(
                                        text = "Mengen werden automatisch auf deinen vorhandenen Vorrat aufaddiert!",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Scrollable list of recognized ingredients
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(scannedItems) { index, item ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (item.isSelected) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (item.fromDatabase) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Checkbox(
                                                checked = item.isSelected,
                                                onCheckedChange = { checked ->
                                                    scannedItems[index] = item.copy(isSelected = checked)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(text = IngredientCatalog.getEmojiFor(item.name), fontSize = 20.sp)
                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = item.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (item.fromDatabase) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = MaterialTheme.colorScheme.secondaryContainer
                                                        ) {
                                                            Text(
                                                                text = "Ergänzt",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontSize = 9.sp,
                                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = item.category,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Quantity controls + Delete
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            FilledTonalIconButton(
                                                onClick = {
                                                    val step = if (item.unit == "g" || item.unit == "ml") 50.0 else 1.0
                                                    val newAmt = (item.amount - step).coerceAtLeast(1.0)
                                                    scannedItems[index] = item.copy(amount = newAmt)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
                                            }

                                            Text(
                                                text = "${FridgeRecipeRepository.formatAmount(item.amount)} ${item.unit}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            )

                                            FilledTonalIconButton(
                                                onClick = {
                                                    val step = if (item.unit == "g" || item.unit == "ml") 50.0 else 1.0
                                                    scannedItems[index] = item.copy(amount = item.amount + step)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            }

                                            IconButton(
                                                onClick = { scannedItems.removeAt(index) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Clear,
                                                    contentDescription = "Entfernen",
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // "Fehlt noch eine Zutat?" Section
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("missing_scanned_items_section")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("🔍", fontSize = 18.sp)
                                    Column {
                                        Text(
                                            text = "Fehlt noch eine Zutat?",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Nicht alles auf dem Foto? Finde und ergänze fehlende Zutaten einfach aus der Liste.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = { showDatabaseSearchDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_database_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Zutat aus Liste ergänzen",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Empty scan state: Photo was taken but no ingredients recognized
                    if (!isScanning && scannedItems.isEmpty() && selectedBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🤔", fontSize = 24.sp)
                                Text(
                                    text = "Keine Zutaten erkannt",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Auf dem Foto konnten leider keine Lebensmittel erkannt werden. Wähle deine Zutaten einfach aus der Liste:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = { showDatabaseSearchDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("search_database_empty_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Zutat aus Liste ergänzen",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (scannedItems.any { it.isSelected } && !isScanning) {
                Button(
                    onClick = {
                        val selectedList = scannedItems.filter { it.isSelected }
                        onConfirmAdd(selectedList, replaceExistingFridge)
                    },
                    modifier = Modifier.testTag("confirm_scanned_ingredients_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (scanType == ScanType.FRIDGE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (scanType == ScanType.FRIDGE)
                            "${scannedItems.count { it.isSelected }} Zutaten in Kühlschrank legen"
                        else
                            "${scannedItems.count { it.isSelected }} Artikel hinzurechnen"
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Schließen")
            }
        }
    )

    // Database search sub-dialog for missing items
    if (showDatabaseSearchDialog) {
        IngredientDatabaseDialog(
            onDismiss = { showDatabaseSearchDialog = false },
            onAddIngredient = { newIng ->
                val existingIndex = scannedItems.indexOfFirst { it.name.equals(newIng.name, ignoreCase = true) }
                if (existingIndex >= 0) {
                    val cur = scannedItems[existingIndex]
                    scannedItems[existingIndex] = cur.copy(
                        amount = cur.amount + newIng.amount,
                        isSelected = true,
                        fromDatabase = true
                    )
                } else {
                    scannedItems.add(newIng)
                }
            }
        )
    }

    // Provider settings sub-dialog
    if (showProviderSettings) {
        AiProviderSettingsDialog(
            aiService = aiService,
            onDismiss = { showProviderSettings = false },
            onSaved = { showProviderSettings = false }
        )
    }
}

private fun createDemoBitmap(scanType: ScanType): Bitmap {
    val width = 480
    val height = 360
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply { isAntiAlias = true }

    // Gradient background
    paint.color = if (scanType == ScanType.FRIDGE) 0xFFDBEAFE.toInt() else 0xFFFEF3C7.toInt()
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    paint.color = if (scanType == ScanType.FRIDGE) 0xFF2563EB.toInt() else 0xFFD97706.toInt()
    paint.strokeWidth = 6f
    paint.style = android.graphics.Paint.Style.STROKE
    canvas.drawRoundRect(16f, 16f, width - 16f, height - 16f, 24f, 24f, paint)

    paint.style = android.graphics.Paint.Style.FILL
    paint.color = 0xFF1E293B.toInt()
    paint.textSize = 28f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    val title = if (scanType == ScanType.FRIDGE) "📸 Scan: Digitaler Kühlschrank" else "🧾 Scan: Kassenbon & Einkauf"
    canvas.drawText(title, width / 2f, height / 2f - 15f, paint)

    paint.textSize = 18f
    paint.color = 0xFF64748B.toInt()
    val subtitle = if (scanType == ScanType.FRIDGE) "Gemüse, Milchprodukte & Vorräte" else "Supermarkt-Einkauf mit Mengenangaben"
    canvas.drawText(subtitle, width / 2f, height / 2f + 30f, paint)

    return bitmap
}
