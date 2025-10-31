package com.voxai.presentation.voicechanger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxai.domain.manager.VoiceCloningManager
import com.voxai.domain.model.*
import com.voxai.util.audio.AudioPlayer
import com.voxai.util.audio.AudioRecorder
import com.voxai.util.audio.CustomVoiceModel
import com.voxai.util.audio.VoiceEffectProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VoiceChangerViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    private val voiceEffectProcessor: VoiceEffectProcessor,
    private val voiceCloningManager: VoiceCloningManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceChangerUiState())
    val uiState: StateFlow<VoiceChangerUiState> = _uiState.asStateFlow()

    private var recordingJob: Job? = null
    private val recordedDataStream = ByteArrayOutputStream()
    private var recordingStartTime = 0L
    
    init {
        loadCustomModels()
    }

    fun startRecording() {
        if (_uiState.value.isRecording) return

        recordedDataStream.reset()
        recordingStartTime = System.currentTimeMillis()
        
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            recordingDuration = 0L,
            error = null
        )

        recordingJob = viewModelScope.launch {
            audioRecorder.startRecording()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isRecording = false,
                        error = "录音失败: ${e.message}"
                    )
                }
                .collect { audioChunk ->
                    recordedDataStream.write(audioChunk)
                    val duration = System.currentTimeMillis() - recordingStartTime
                    _uiState.value = _uiState.value.copy(recordingDuration = duration)
                }
        }
    }

    fun stopRecording() {
        if (!_uiState.value.isRecording) return

        recordingJob?.cancel()
        audioRecorder.stopRecording()

        val audioData = AudioData(
            id = UUID.randomUUID().toString(),
            data = recordedDataStream.toByteArray(),
            sampleRate = AudioRecorder.DEFAULT_SAMPLE_RATE,
            durationMs = System.currentTimeMillis() - recordingStartTime
        )

        _uiState.value = _uiState.value.copy(
            isRecording = false,
            currentAudio = audioData,
            processedAudio = null
        )
    }

    fun applyEffect(effect: VoiceEffect) {
        val currentAudio = _uiState.value.currentAudio ?: return

        _uiState.value = _uiState.value.copy(
            selectedEffect = effect,
            isProcessing = true
        )

        viewModelScope.launch {
            try {
                val processedData = if (effect.effectType == VoiceEffectType.BASIC) {
                    // 使用基础处理器
                    voiceEffectProcessor.applyEffect(currentAudio.data, effect)
                } else {
                    // 使用高级声音克隆处理器
                    val customModel = if (effect == VoiceEffect.CUSTOM) {
                        _uiState.value.selectedCustomModel
                    } else null
                    
                    voiceEffectProcessor.applyAdvancedEffect(
                        currentAudio.data,
                        effect,
                        customModel
                    )
                }

                val processedAudio = AudioData(
                    id = UUID.randomUUID().toString(),
                    data = processedData,
                    sampleRate = currentAudio.sampleRate,
                    durationMs = currentAudio.durationMs,
                    effect = effect
                )

                _uiState.value = _uiState.value.copy(
                    processedAudio = processedAudio,
                    isProcessing = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "应用音效失败: ${e.message}"
                )
            }
        }
    }

    fun playAudio() {
        val audioToPlay = _uiState.value.processedAudio ?: _uiState.value.currentAudio
        if (audioToPlay == null) return

        _uiState.value = _uiState.value.copy(isPlaying = true)

        viewModelScope.launch {
            try {
                audioPlayer.play(audioToPlay.data)
                _uiState.value = _uiState.value.copy(isPlaying = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPlaying = false,
                    error = "播放失败: ${e.message}"
                )
            }
        }
    }

    fun stopPlaying() {
        audioPlayer.stop()
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }

    fun clearAudio() {
        stopPlaying()
        recordedDataStream.reset()
        _uiState.value = _uiState.value.copy(
            currentAudio = null,
            processedAudio = null,
            selectedEffect = VoiceEffect.NONE,
            recordingDuration = 0L
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    // 自定义声音模型相关方法
    fun loadCustomModels() {
        voiceCloningManager.loadSavedModels()
        _uiState.value = _uiState.value.copy(
            customModels = voiceCloningManager.getCustomModels()
        )
    }
    
    fun selectCustomModel(model: CustomVoiceModel?) {
        _uiState.value = _uiState.value.copy(
            selectedCustomModel = model,
            selectedEffect = if (model != null) VoiceEffect.CUSTOM else VoiceEffect.NONE
        )
    }
    
    fun deleteCustomModel(modelId: String) {
        voiceCloningManager.deleteCustomModel(modelId)
        loadCustomModels()
        
        // 如果删除的是当前选中的模型，清除选择
        if (_uiState.value.selectedCustomModel?.id == modelId) {
            _uiState.value = _uiState.value.copy(
                selectedCustomModel = null,
                selectedEffect = VoiceEffect.NONE
            )
        }
    }
    
    fun trainCustomVoiceModel(
        name: String,
        trainingSamples: List<ByteArray>,
        voiceCharacteristics: VoiceCharacteristics
    ) {
        _uiState.value = _uiState.value.copy(
            isTrainingModel = true,
            trainingProgress = 0f,
            trainingMessage = "开始训练..."
        )
        
        viewModelScope.launch {
            voiceCloningManager.trainCustomVoiceModel(name, trainingSamples, voiceCharacteristics)
                .onSuccess { model ->
                    _uiState.value = _uiState.value.copy(
                        isTrainingModel = false,
                        trainingProgress = 0f,
                        trainingMessage = "",
                        showCustomModelDialog = false
                    )
                    loadCustomModels()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isTrainingModel = false,
                        trainingProgress = 0f,
                        trainingMessage = "",
                        error = "训练失败: ${error.message}"
                    )
                }
        }
    }
    
    fun showCustomModelDialog() {
        _uiState.value = _uiState.value.copy(showCustomModelDialog = true)
    }
    
    fun hideCustomModelDialog() {
        _uiState.value = _uiState.value.copy(showCustomModelDialog = false)
    }
    
    fun updateTrainingProgress(progress: Float, message: String) {
        _uiState.value = _uiState.value.copy(
            trainingProgress = progress,
            trainingMessage = message
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.stopRecording()
        audioPlayer.stop()
    }
}

data class VoiceChangerUiState(
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val isProcessing: Boolean = false,
    val recordingDuration: Long = 0L,
    val currentAudio: AudioData? = null,
    val processedAudio: AudioData? = null,
    val selectedEffect: VoiceEffect = VoiceEffect.NONE,
    val selectedCustomModel: CustomVoiceModel? = null,
    val customModels: List<CustomVoiceModel> = emptyList(),
    val isTrainingModel: Boolean = false,
    val trainingProgress: Float = 0f,
    val trainingMessage: String = "",
    val showCustomModelDialog: Boolean = false,
    val error: String? = null
)
