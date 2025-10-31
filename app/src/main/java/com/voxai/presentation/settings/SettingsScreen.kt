package com.voxai.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        )

        SettingsSection(title = "通用设置") {
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "主题",
                subtitle = "跟随系统",
                onClick = { }
            )
            SettingsItem(
                icon = Icons.Default.Language,
                title = "语言",
                subtitle = "简体中文",
                onClick = { }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        SettingsSection(title = "音频设置") {
            var audioQuality by remember { mutableStateOf(1) }
            
            SettingsSliderItem(
                icon = Icons.Default.GraphicEq,
                title = "音频质量",
                value = audioQuality,
                valueRange = 0f..2f,
                steps = 1,
                onValueChange = { audioQuality = it.toInt() },
                valueLabel = when (audioQuality) {
                    0 -> "低"
                    1 -> "中"
                    else -> "高"
                }
            )

            var enableNoiseCancellation by remember { mutableStateOf(true) }
            SettingsSwitchItem(
                icon = Icons.Default.VolumeOff,
                title = "降噪",
                subtitle = "减少背景噪音",
                checked = enableNoiseCancellation,
                onCheckedChange = { enableNoiseCancellation = it }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        SettingsSection(title = "AI设置") {
            SettingsItem(
                icon = Icons.Default.SmartToy,
                title = "AI模型",
                subtitle = "GPT-3.5 Turbo",
                onClick = { }
            )

            var enableTTS by remember { mutableStateOf(true) }
            SettingsSwitchItem(
                icon = Icons.Default.RecordVoiceOver,
                title = "语音回复",
                subtitle = "AI回复时使用语音",
                checked = enableTTS,
                onCheckedChange = { enableTTS = it }
            )

            var applyVoiceEffect by remember { mutableStateOf(false) }
            SettingsSwitchItem(
                icon = Icons.Default.Mic,
                title = "变声AI回复",
                subtitle = "对AI语音回复应用变声效果",
                checked = applyVoiceEffect,
                onCheckedChange = { applyVoiceEffect = it }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        SettingsSection(title = "关于") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "版本",
                subtitle = "1.0.0",
                onClick = { }
            )
            SettingsItem(
                icon = Icons.Default.Code,
                title = "开源许可",
                subtitle = "查看第三方库",
                onClick = { }
            )
            SettingsItem(
                icon = Icons.Default.BugReport,
                title = "反馈问题",
                subtitle = "报告bug或建议",
                onClick = { }
            )
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSliderItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    valueLabel: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}
