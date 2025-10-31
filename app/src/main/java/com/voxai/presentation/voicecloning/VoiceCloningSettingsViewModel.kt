package com.voxai.presentation.voicecloning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxai.domain.manager.VoiceCloningManager
import com.voxai.domain.manager.VoiceCloningConfig
import com.voxai.domain.model.*
import com.voxai.util.audio.AdvancedVoiceCloningProcessor
import com.voxai.util.audio.CustomVoiceModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceCloningSettingsViewModel @Inject constructor(
    private val voiceCloningManager: VoiceCloningManager,
    private val voiceCloningProcessor: AdvancedVoiceCloningProcessor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VoiceCloningSettingsUiState())
    val uiState: StateFlow<VoiceCloningSettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadPresetVoices()
        loadCustomModels()
    }
    
    private fun loadPresetVoices() {
        val presetVoices = listOf(
            PresetVoice(
                id = "lazy_cat_enhanced",
                name = "懒洋洋（增强版）",
                description = "更加懒散的语调，适合休闲场景",
                characteristics = VoiceCharacteristics(
                    emotion = Emotion.LAZY,
                    gender = Gender.MALE,
                    age = Age.ADULT,
                    formantShift = 0.85f
                ),
                voiceEffect = VoiceEffect.LAZY_CAT
            ),
            PresetVoice(
                id = "spongebob_pro",
                name = "海绵宝宝（专业版）",
                description = "更活泼的语调，充满活力",
                characteristics = VoiceCharacteristics(
                    emotion = Emotion.EXCITED,
                    gender = Gender.MALE,
                    age = Age.YOUNG_ADULT,
                    formantShift = 1.4f
                ),
                voiceEffect = VoiceEffect.SPONGEBOB
            ),
            PresetVoice(
                id = "elsa_frozen",
                name = "艾莎公主（冰雪版）",
                description = "优雅清冷的语调，如冰雪般纯净",
                characteristics = VoiceCharacteristics(
                    emotion = Emotion.ELEGANT,
                    gender = Gender.FEMALE,
                    age = Age.YOUNG_ADULT,
                    formantShift = 1.35f
                ),
                voiceEffect = VoiceEffect.ELSA
            ),
            PresetVoice(
                id = "optimus_prime_commander",
                name = "擎天柱（指挥官版）",
                description = "威严的指挥官语调，充满权威感",
                characteristics = VoiceCharacteristics(
                    emotion = Emotion.AUTHORITATIVE,
                    gender = Gender.MALE,
                    age = Age.ADULT,
                    formantShift = 0.65f
                ),
                voiceEffect = VoiceEffect.OPTIMUS_PRIME
            ),
            PresetVoice(
                id = "minion_banana",
                name = "小黄人（香蕉版）",
                description = "调皮可爱的语调，充满童趣",
                characteristics = VoiceCharacteristics(
                    emotion = Emotion.PLAYFUL,
                    gender = Gender.NEUTRAL,
                    age = Age.CHILD,
                    formantShift = 1.6f
                ),
                voiceEffect = VoiceEffect.MINION
            )
        )
        
        _uiState.value = _uiState.value.copy(presetVoices = presetVoices)
    }
    
    private fun loadCustomModels() {
        voiceCloningManager.loadSavedModels()
        _uiState.value = _uiState.value.copy(
            customModels = voiceCloningManager.getCustomModels()
        )
    }
    
    fun updateAdvancedSettings(settings: AdvancedVoiceSettings) {
        _uiState.value = _uiState.value.copy(advancedSettings = settings)
    }
    
    fun updateQualitySettings(settings: VoiceQualitySettings) {
        _uiState.value = _uiState.value.copy(qualitySettings = settings)
    }
    
    fun selectPresetVoice(voice: PresetVoice) {
        _uiState.value = _uiState.value.copy(selectedPresetVoice = voice)
    }
    
    fun selectCustomModel(model: CustomVoiceModel) {
        // 可以在这里添加选择自定义模型的逻辑
    }
    
    fun deleteCustomModel(modelId: String) {
        voiceCloningManager.deleteCustomModel(modelId)
        loadCustomModels()
    }
    
    fun showTrainingDialog() {
        _uiState.value = _uiState.value.copy(showTrainingDialog = true)
    }
    
    fun hideTrainingDialog() {
        _uiState.value = _uiState.value.copy(
            showTrainingDialog = false,
            isTraining = false,
            trainingProgress = 0f,
            trainingMessage = ""
        )
    }
    
    fun trainVoiceModel(
        name: String,
        characteristics: VoiceCharacteristics
    ) {
        _uiState.value = _uiState.value.copy(
            isTraining = true,
            trainingProgress = 0f,
            trainingMessage = "开始训练模型..."
        )
        
        viewModelScope.launch {
            // 模拟训练过程 - 实际实现中需要真实的音频样本
            val trainingSamples = generateMockTrainingSamples()
            
            voiceCloningManager.trainCustomVoiceModel(name, trainingSamples, characteristics)
                .onSuccess { model ->
                    _uiState.value = _uiState.value.copy(
                        isTraining = false,
                        trainingProgress = 1f,
                        trainingMessage = "训练完成！",
                        showTrainingDialog = false
                    )
                    loadCustomModels()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isTraining = false,
                        trainingProgress = 0f,
                        trainingMessage = "训练失败: ${error.message}"
                    )
                }
        }
    }
    
    private fun generateMockTrainingSamples(): List<ByteArray> {
        // 生成模拟的训练样本
        return (1..8).map {
            "mock_audio_sample_$it".toByteArray()
        }
    }
    
    fun getVoiceCloningConfig(): VoiceCloningConfig {
        return VoiceCloningConfig(
            minTrainingSamples = 5,
            maxTrainingSamples = 20,
            minSampleDurationMs = 3000,
            maxSampleDurationMs = 15000,
            supportedFormats = listOf("WAV", "PCM", "FLAC")
        )
    }
    
    /**
     * 优化声音克隆设置
     */
    fun optimizeSettings(targetVoice: VoiceCharacteristics) {
        val optimizedSettings = _uiState.value.advancedSettings.copy(
            enableRealTimeProcessing = targetVoice.emotion != Emotion.ANGRY,
            preserveEmotion = targetVoice.emotion != Emotion.NEUTRAL,
            voiceQuality = when (targetVoice.emotion) {
                Emotion.ELEGANT -> 1.0f
                Emotion.AUTHORITATIVE -> 0.9f
                Emotion.PLAYFUL -> 0.8f
                else -> 0.85f
            }
        )
        
        val optimizedQuality = _uiState.value.qualitySettings.copy(
            sampleRate = when (targetVoice.age) {
                Age.CHILD -> 22050
                Age.ELDERLY -> 16000
                else -> 44100
            },
            bitrate = when (targetVoice.gender) {
                Gender.FEMALE -> 128
                Gender.MALE -> 96
                Gender.NEUTRAL -> 112
            }
        )
        
        _uiState.value = _uiState.value.copy(
            advancedSettings = optimizedSettings,
            qualitySettings = optimizedQuality
        )
    }
    
    /**
     * 导出声音克隆配置
     */
    fun exportSettings(): String {
        val state = _uiState.value
        return """
            Voice Cloning Settings Export
            =============================
            
            Advanced Settings:
            - Real-time Processing: ${state.advancedSettings.enableRealTimeProcessing}
            - Noise Suppression: ${state.advancedSettings.enableNoiseSuppression}
            - Preserve Emotion: ${state.advancedSettings.preserveEmotion}
            - Voice Quality: ${(state.advancedSettings.voiceQuality * 100).toInt()}%
            
            Quality Settings:
            - Sample Rate: ${state.qualitySettings.sampleRate}Hz
            - Bitrate: ${state.qualitySettings.bitrate} kbps
            
            Selected Preset: ${state.selectedPresetVoice?.name ?: "None"}
            Custom Models Count: ${state.customModels.size}
        """.trimIndent()
    }
    
    /**
     * 导入声音克隆配置
     */
    fun importSettings(configJson: String): Boolean {
        return try {
            // 简化的导入逻辑
            // 实际实现应该解析JSON格式
            _uiState.value = _uiState.value.copy(
                advancedSettings = AdvancedVoiceSettings(
                    enableRealTimeProcessing = true,
                    enableNoiseSuppression = true,
                    preserveEmotion = true,
                    voiceQuality = 0.85f
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }
}