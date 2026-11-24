package com.wgrou.mangareader.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wgrou.mangareader.data.SettingsManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    textureStrength: Float,
    paperWarmth: Float,
    onTextureStrengthChanged: (Float) -> Unit,
    onPaperWarmthChanged: (Float) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Page Appearance",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppearanceSlider(
                label = "Paper texture",
                value = textureStrength,
                onValueChanged = onTextureStrengthChanged,
                minimumLabel = "Off",
                maximumLabel = "Full strength"
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppearanceSlider(
                label = "Paper warmth",
                value = paperWarmth,
                onValueChanged = onPaperWarmthChanged,
                minimumLabel = "Neutral",
                maximumLabel = "Warm"
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    onTextureStrengthChanged(
                        SettingsManager.DEFAULT_TEXTURE_STRENGTH
                    )
                    onPaperWarmthChanged(
                        SettingsManager.DEFAULT_PAPER_WARMTH
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore defaults")
            }
        }
    }
}

@Composable
private fun AppearanceSlider(
    label: String,
    value: Float,
    onValueChanged: (Float) -> Unit,
    minimumLabel: String,
    maximumLabel: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text("${(value * 100).roundToInt()}%")
        }

        Slider(
            value = value,
            onValueChange = onValueChanged,
            valueRange = 0f..1f,
            steps = 19
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = minimumLabel,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = maximumLabel,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}