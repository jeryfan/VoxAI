package com.voxai.presentation.voicechanger

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.voxai.domain.model.VoiceEffect

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceChangerScreen(
    viewModel: VoiceChangerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "变声器",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!permissionsState.allPermissionsGranted) {
            PermissionRequiredCard(
                onRequestPermission = {
                    permissionsState.launchMultiplePermissionRequest()
                }
            )
        } else {
            RecordingSection(
                isRecording = uiState.isRecording,
                recordingDuration = uiState.recordingDuration,
                hasAudio = uiState.currentAudio != null,
                onStartRecording = { viewModel.startRecording() },
                onStopRecording = { viewModel.stopRecording() },
                onClearAudio = { viewModel.clearAudio() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            VoiceEffectsSection(
                selectedEffect = uiState.selectedEffect,
                isEnabled = uiState.currentAudio != null && !uiState.isRecording,
                onEffectSelected = { viewModel.applyEffect(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            PlaybackSection(
                isPlaying = uiState.isPlaying,
                isProcessing = uiState.isProcessing,
                hasAudio = uiState.currentAudio != null,
                onPlay = { viewModel.playAudio() },
                onStop = { viewModel.stopPlaying() }
            )
        }

        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("确定")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun PermissionRequiredCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "需要录音权限",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请授予录音权限以使用变声功能",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text("授予权限")
            }
        }
    }
}

@Composable
fun RecordingSection(
    isRecording: Boolean,
    recordingDuration: Long,
    hasAudio: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onClearAudio: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (isRecording) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    .clickable {
                        if (isRecording) onStopRecording() else onStartRecording()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "停止录音" else "开始录音",
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isRecording) "录音中..." else if (hasAudio) "已录制" else "点击录音",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isRecording || recordingDuration > 0) {
                Text(
                    text = formatDuration(recordingDuration),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (hasAudio && !isRecording) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onClearAudio) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清除")
                }
            }
        }
    }
}

@Composable
fun VoiceEffectsSection(
    selectedEffect: VoiceEffect,
    isEnabled: Boolean,
    onEffectSelected: (VoiceEffect) -> Unit
) {
    Column {
        Text(
            text = "选择音效",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VoiceEffect.values().toList()) { effect ->
                EffectChip(
                    effect = effect,
                    isSelected = effect == selectedEffect,
                    isEnabled = isEnabled,
                    onClick = { onEffectSelected(effect) }
                )
            }
        }
    }
}

@Composable
fun EffectChip(
    effect: VoiceEffect,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        enabled = isEnabled,
        label = { Text(effect.displayName) },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null
    )
}

@Composable
fun PlaybackSection(
    isPlaying: Boolean,
    isProcessing: Boolean,
    hasAudio: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { if (isPlaying) onStop() else onPlay() },
                enabled = hasAudio && !isProcessing,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPlaying) "暂停" else "播放")
            }
        }

        if (isProcessing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
