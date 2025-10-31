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
                selectedCustomModel = uiState.selectedCustomModel,
                isEnabled = uiState.currentAudio != null && !uiState.isRecording,
                onEffectSelected = { viewModel.applyEffect(it) },
                onCustomModelSelected = { model -> viewModel.selectCustomModel(model) },
                onAddCustomModel = { viewModel.showCustomModelDialog() },
                customModels = uiState.customModels
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
        
        // 自定义模型训练对话框
        CustomModelTrainingDialog(
            isVisible = uiState.showCustomModelDialog,
            isTraining = uiState.isTrainingModel,
            progress = uiState.trainingProgress,
            message = uiState.trainingMessage,
            onDismiss = { viewModel.hideCustomModelDialog() },
            onTrain = { name, samples, characteristics ->
                viewModel.trainCustomVoiceModel(name, samples, characteristics)
            }
        )
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
    selectedCustomModel: com.voxai.util.audio.CustomVoiceModel?,
    isEnabled: Boolean,
    onEffectSelected: (VoiceEffect) -> Unit,
    onCustomModelSelected: (com.voxai.util.audio.CustomVoiceModel?) -> Unit,
    onAddCustomModel: () -> Unit,
    customModels: List<com.voxai.util.audio.CustomVoiceModel>
) {
    Column {
        Text(
            text = "选择音效",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 基础音效
        Text(
            text = "基础音效",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VoiceEffect.values().filter { it.effectType == VoiceEffectType.BASIC }) { effect ->
                EffectChip(
                    effect = effect,
                    isSelected = effect == selectedEffect && selectedEffect.effectType == VoiceEffectType.BASIC,
                    isEnabled = isEnabled,
                    onClick = { onEffectSelected(effect) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 高级声音克隆效果
        Text(
            text = "高级声音克隆",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(VoiceEffect.values().filter { it.effectType == VoiceEffectType.CLONED }) { effect ->
                EffectChip(
                    effect = effect,
                    isSelected = effect == selectedEffect && selectedEffect.effectType == VoiceEffectType.CLONED,
                    isEnabled = isEnabled,
                    onClick = { onEffectSelected(effect) }
                )
            }
            
            // 自定义模型按钮
            item {
                CustomModelChip(
                    isSelected = selectedEffect == VoiceEffect.CUSTOM,
                    selectedModel = selectedCustomModel,
                    isEnabled = isEnabled,
                    onClick = { 
                        if (customModels.isNotEmpty()) {
                            onCustomModelSelected(selectedCustomModel?.let { null } ?: customModels.first())
                        } else {
                            onAddCustomModel()
                        }
                    },
                    onAddModel = onAddCustomModel
                )
            }
        }

        // 显示已选择的自定义模型
        if (selectedEffect == VoiceEffect.CUSTOM && selectedCustomModel != null) {
            Spacer(modifier = Modifier.height(8.dp))
            SelectedCustomModelCard(
                model = selectedCustomModel,
                onChangeModel = { onCustomModelSelected(null) }
            )
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

@Composable
fun CustomModelChip(
    isSelected: Boolean,
    selectedModel: com.voxai.util.audio.CustomVoiceModel?,
    isEnabled: Boolean,
    onClick: () -> Unit,
    onAddModel: () -> Unit
) {
    Row {
        FilterChip(
            selected = isSelected,
            onClick = onClick,
            enabled = isEnabled,
            label = { 
                Text(if (selectedModel != null) selectedModel.id else "自定义") 
            },
            leadingIcon = if (isSelected) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
        
        IconButton(
            onClick = onAddModel,
            enabled = isEnabled
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加自定义模型",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SelectedCustomModelCard(
    model: com.voxai.util.audio.CustomVoiceModel,
    onChangeModel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "当前模型: ${model.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "训练样本: ${model.trainingSamples} 个",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            TextButton(onClick = onChangeModel) {
                Text("切换")
            }
        }
    }
}

@Composable
fun CustomModelTrainingDialog(
    isVisible: Boolean,
    isTraining: Boolean,
    progress: Float,
    message: String,
    onDismiss: () -> Unit,
    onTrain: (String, List<ByteArray>, VoiceCharacteristics) -> Unit
) {
    if (isVisible) {
        var modelName by remember { mutableStateOf("") }
        var selectedEmotion by remember { mutableStateOf(Emotion.NEUTRAL) }
        var selectedGender by remember { mutableStateOf(Gender.MALE) }
        var selectedAge by remember { mutableStateOf(Age.ADULT) }
        var trainingSamples by remember { mutableStateOf<List<ByteArray>>(emptyList()) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("训练自定义声音模型") },
            text = {
                if (isTraining) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(progress = progress)
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text("模型名称") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            text = "情感特征",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(Emotion.values()) { emotion ->
                                FilterChip(
                                    selected = selectedEmotion == emotion,
                                    onClick = { selectedEmotion = emotion },
                                    label = { Text(emotion.name) }
                                )
                            }
                        }
                        
                        Text(
                            text = "性别",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Gender.values().forEach { gender ->
                                FilterChip(
                                    selected = selectedGender == gender,
                                    onClick = { selectedGender = gender },
                                    label = { 
                                        Text(when (gender) {
                                            Gender.MALE -> "男声"
                                            Gender.FEMALE -> "女声"
                                            Gender.NEUTRAL -> "中性"
                                        })
                                    }
                                )
                            }
                        }
                        
                        Text(
                            text = "年龄",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(Age.values()) { age ->
                                FilterChip(
                                    selected = selectedAge == age,
                                    onClick = { selectedAge = age },
                                    label = { 
                                        Text(when (age) {
                                            Age.CHILD -> "儿童"
                                            Age.TEENAGER -> "青少年"
                                            Age.YOUNG_ADULT -> "青年"
                                            Age.ADULT -> "成年"
                                            Age.ELDERLY -> "老年"
                                        })
                                    }
                                )
                            }
                        }
                        
                        Text(
                            text = "需要至少5个录音样本来训练模型",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                if (!isTraining) {
                    Button(
                        onClick = {
                            val characteristics = VoiceCharacteristics(
                                emotion = selectedEmotion,
                                gender = selectedGender,
                                age = selectedAge
                            )
                            onTrain(modelName, trainingSamples, characteristics)
                        },
                        enabled = modelName.isNotBlank() && trainingSamples.size >= 5
                    ) {
                        Text("开始训练")
                    }
                }
            },
            dismissButton = {
                if (!isTraining) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                }
            }
        )
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
