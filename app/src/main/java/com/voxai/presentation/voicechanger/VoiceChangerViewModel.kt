package com.voxai.presentation.voicechanger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxai.domain.model.AudioData
import com.voxai.domain.model.VoiceEffect
import com.voxai.util.audio.AudioPlayer
import com.voxai.util.audio.AudioRecorder
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
    private val voiceEffectProcessor: VoiceEffectProcessor
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceChangerUiState())
    val uiState: StateFlow<VoiceChangerUiState> = _uiState.asStateFlow()

    private var recordingJob: Job? = null
    private val recordedDataStream = ByteArrayOutputStream()
    private var recordingStartTime = 0L

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
                val processedData = voiceEffectProcessor.applyEffect(
                    currentAudio.data,
                    effect
                )

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
    val error: String? = null
)
