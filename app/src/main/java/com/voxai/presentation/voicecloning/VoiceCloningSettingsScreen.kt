package com.voxai.presentation.voicecloning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voxai.domain.model.*
import com.voxai.util.audio.CustomVoiceModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCloningSettingsScreen(
    viewModel: VoiceCloningSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("声音克隆设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 高级设置
            item {
                AdvancedSettingsCard(
                    settings = uiState.advancedSettings,
                    onSettingsChanged = viewModel::updateAdvancedSettings
                )
            }
            
            // 预设声音克隆效果
            item {
                PresetVoicesCard(
                    presetVoices = uiState.presetVoices,
                    selectedVoice = uiState.selectedPresetVoice,
                    onVoiceSelected = viewModel::selectPresetVoice
                )
            }
            
            // 自定义模型管理
            item {
                CustomModelsCard(
                    models = uiState.customModels,
                    onModelSelected = viewModel::selectCustomModel,
                    onModelDeleted = viewModel::deleteCustomModel,
                    onTrainNewModel = viewModel::showTrainingDialog
                )
            }
            
            // 质量设置
            item {
                QualitySettingsCard(
                    qualitySettings = uiState.qualitySettings,
                    onQualityChanged = viewModel::updateQualitySettings
                )
            }
        }
        
        // 训练对话框
        if (uiState.showTrainingDialog) {
            VoiceTrainingDialog(
                isVisible = uiState.showTrainingDialog,
                isTraining = uiState.isTraining,
                progress = uiState.trainingProgress,
                message = uiState.trainingMessage,
                onDismiss = viewModel::hideTrainingDialog,
                onTrain = viewModel::trainVoiceModel
            )
        }
    }
}

@Composable
fun AdvancedSettingsCard(
    settings: AdvancedVoiceSettings,
    onSettingsChanged: (AdvancedVoiceSettings) -> Unit
) {
    var localSettings by remember { mutableStateOf(settings) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "高级设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 实时处理
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "实时处理",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "降低延迟但可能影响质量",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = localSettings.enableRealTimeProcessing,
                    onCheckedChange = { 
                        localSettings = localSettings.copy(enableRealTimeProcessing = it)
                        onSettingsChanged(localSettings)
                    }
                )
            }
            
            // 噪音抑制
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "噪音抑制",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "自动过滤背景噪音",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = localSettings.enableNoiseSuppression,
                    onCheckedChange = { 
                        localSettings = localSettings.copy(enableNoiseSuppression = it)
                        onSettingsChanged(localSettings)
                    }
                )
            }
            
            // 情感保持
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "情感保持",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "保持原始情感表达",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = localSettings.preserveEmotion,
                    onCheckedChange = { 
                        localSettings = localSettings.copy(preserveEmotion = it)
                        onSettingsChanged(localSettings)
                    }
                )
            }
            
            // 音质滑块
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "音质",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${(localSettings.voiceQuality * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Slider(
                    value = localSettings.voiceQuality,
                    onValueChange = { 
                        localSettings = localSettings.copy(voiceQuality = it)
                        onSettingsChanged(localSettings)
                    },
                    valueRange = 0.1f..1.0f,
                    steps = 8
                )
            }
        }
    }
}

@Composable
fun PresetVoicesCard(
    presetVoices: List<PresetVoice>,
    selectedVoice: PresetVoice?,
    onVoiceSelected: (PresetVoice) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "预设声音克隆",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            presetVoices.forEach { voice ->
                PresetVoiceItem(
                    voice = voice,
                    isSelected = voice == selectedVoice,
                    onSelected = { onVoiceSelected(voice) }
                )
            }
        }
    }
}

@Composable
fun PresetVoiceItem(
    voice: PresetVoice,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelected() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = voice.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = voice.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                voice.characteristics.emotion.let { emotion ->
                    AssistChip(
                        onClick = {},
                        label = { Text(emotion.name) }
                    )
                }
                voice.characteristics.gender.let { gender ->
                    AssistChip(
                        onClick = {},
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
        }
    }
}

@Composable
fun CustomModelsCard(
    models: List<CustomVoiceModel>,
    onModelSelected: (CustomVoiceModel) -> Unit,
    onModelDeleted: (String) -> Unit,
    onTrainNewModel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "自定义模型",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onTrainNewModel) {
                    Icon(Icons.Default.Add, contentDescription = "添加新模型")
                }
            }
            
            if (models.isEmpty()) {
                Text(
                    text = "暂无自定义模型\n点击 + 按钮创建新模型",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                models.forEach { model ->
                    CustomModelItem(
                        model = model,
                        onSelected = { onModelSelected(model) },
                        onDeleted = { onModelDeleted(model.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomModelItem(
    model: CustomVoiceModel,
    onSelected: () -> Unit,
    onDeleted: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.id,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "训练样本: ${model.trainingSamples} 个",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "创建时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(model.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onSelected) {
            Icon(Icons.Default.PlayArrow, contentDescription = "使用模型")
        }
        
        IconButton(onClick = onDeleted) {
            Icon(Icons.Default.Delete, contentDescription = "删除模型")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualitySettingsCard(
    qualitySettings: VoiceQualitySettings,
    onQualityChanged: (VoiceQualitySettings) -> Unit
) {
    var localSettings by remember { mutableStateOf(qualitySettings) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "质量设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 采样率
            Column {
                Text(
                    text = "采样率",
                    style = MaterialTheme.typography.bodyMedium
                )
                val sampleRates = listOf(16000, 22050, 44100, 48000)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleRates.forEach { rate ->
                        FilterChip(
                            selected = localSettings.sampleRate == rate,
                            onClick = { 
                                localSettings = localSettings.copy(sampleRate = rate)
                                onQualityChanged(localSettings)
                            },
                            label = { Text("${rate}Hz") }
                        )
                    }
                }
            }
            
            // 比特率
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "比特率",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${localSettings.bitrate} kbps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Slider(
                    value = localSettings.bitrate.toFloat(),
                    onValueChange = { 
                        localSettings = localSettings.copy(bitrate = it.toInt())
                        onQualityChanged(localSettings)
                    },
                    valueRange = 64f..320f,
                    steps = 7
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTrainingDialog(
    isVisible: Boolean,
    isTraining: Boolean,
    progress: Float,
    message: String,
    onDismiss: () -> Unit,
    onTrain: (String, VoiceCharacteristics) -> Unit
) {
    if (isVisible) {
        var modelName by remember { mutableStateOf("") }
        var selectedEmotion by remember { mutableStateOf(Emotion.NEUTRAL) }
        var selectedGender by remember { mutableStateOf(Gender.MALE) }
        var selectedAge by remember { mutableStateOf(Age.ADULT) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("训练自定义声音模型") },
            text = {
                if (isTraining) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
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
                        
                        // 情感选择
                        Text(
                            text = "情感特征",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Emotion.values().forEach { emotion ->
                                FilterChip(
                                    selected = selectedEmotion == emotion,
                                    onClick = { selectedEmotion = emotion },
                                    label = { Text(emotion.name) }
                                )
                            }
                        }
                        
                        // 性别选择
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
                        
                        // 年龄选择
                        Text(
                            text = "年龄",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Age.values().forEach { age ->
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
                            text = "请录制5-10个音频样本来训练模型",
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
                            onTrain(modelName, characteristics)
                        },
                        enabled = modelName.isNotBlank()
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

// 数据类
data class AdvancedVoiceSettings(
    val enableRealTimeProcessing: Boolean = true,
    val enableNoiseSuppression: Boolean = true,
    val preserveEmotion: Boolean = true,
    val voiceQuality: Float = 0.8f
)

data class VoiceQualitySettings(
    val sampleRate: Int = 44100,
    val bitrate: Int = 128
)

data class PresetVoice(
    val id: String,
    val name: String,
    val description: String,
    val characteristics: VoiceCharacteristics,
    val voiceEffect: VoiceEffect
)

data class VoiceCloningSettingsUiState(
    val advancedSettings: AdvancedVoiceSettings = AdvancedVoiceSettings(),
    val qualitySettings: VoiceQualitySettings = VoiceQualitySettings(),
    val presetVoices: List<PresetVoice> = emptyList(),
    val selectedPresetVoice: PresetVoice? = null,
    val customModels: List<CustomVoiceModel> = emptyList(),
    val showTrainingDialog: Boolean = false,
    val isTraining: Boolean = false,
    val trainingProgress: Float = 0f,
    val trainingMessage: String = ""
)