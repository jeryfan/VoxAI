package com.voxai.util.audio

import com.voxai.domain.model.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

class VoiceCloningProcessorTest {

    @Test
    fun `test basic voice effect processing`() = runTest {
        val processor = VoiceEffectProcessor()
        val testData = createTestAudioData()
        
        // 测试基础音效
        val robotEffect = processor.applyEffect(testData, VoiceEffect.ROBOT)
        assertNotNull(robotEffect)
        assertTrue(robotEffect.isNotEmpty())
        
        // 测试原声（应该返回原始数据）
        val noneEffect = processor.applyEffect(testData, VoiceEffect.NONE)
        assertArrayEquals(testData, noneEffect)
    }

    @Test
    fun `test advanced voice cloning processing`() = runTest {
        val processor = AdvancedVoiceCloningProcessor()
        val testData = createTestAudioData()
        
        // 测试懒洋洋效果
        val lazyCatEffect = processor.applyAdvancedEffect(testData, VoiceEffect.LAZY_CAT)
        assertNotNull(lazyCatEffect)
        assertTrue(lazyCatEffect.isNotEmpty())
        
        // 测试海绵宝宝效果
        val spongebobEffect = processor.applyAdvancedEffect(testData, VoiceEffect.SPONGEBOB)
        assertNotNull(spongebobEffect)
        assertTrue(spongebobEffect.isNotEmpty())
        
        // 测试自定义模型
        val customModel = createTestCustomModel()
        val customEffect = processor.applyAdvancedEffect(testData, VoiceEffect.CUSTOM, customModel)
        assertNotNull(customEffect)
        assertTrue(customEffect.isNotEmpty())
    }

    @Test
    fun `test voice characteristics conversion`() {
        val converter = RealTimeVoiceConverter()
        val sourceFeatures = createTestVoiceFeatures()
        
        // 测试性别转换
        val maleFeatures = converter.applyGenderConversion(sourceFeatures, Gender.MALE)
        val femaleFeatures = converter.applyGenderConversion(sourceFeatures, Gender.FEMALE)
        
        // 女声的共振峰应该比男声高
        assertTrue(femaleFeatures.formants.average() > maleFeatures.formants.average())
        
        // 测试年龄转换
        val childFeatures = converter.applyAgeConversion(sourceFeatures, Age.CHILD)
        val adultFeatures = converter.applyAgeConversion(sourceFeatures, Age.ADULT)
        
        // 儿童的音高应该比成人高
        assertTrue(childFeatures.pitch > adultFeatures.pitch)
        
        // 测试情感转换
        val happyFeatures = converter.applyEmotionConversion(sourceFeatures, Emotion.HAPPY)
        val sadFeatures = converter.applyEmotionConversion(sourceFeatures, Emotion.SAD)
        
        // 快乐的能量应该比悲伤高
        assertTrue(happyFeatures.energy > sadFeatures.energy)
    }

    @Test
    fun `test voice feature extraction`() {
        val extractor = VoiceFeatureExtractor()
        val testData = createTestAudioData()
        
        val features = extractor.extractFeatures(testData)
        
        assertNotNull(features)
        assertTrue(features.pitch > 0f)
        assertTrue(features.formants.isNotEmpty())
        assertTrue(features.spectralEnvelope.isNotEmpty())
        assertTrue(features.energy > 0f)
        assertTrue(features.mfcc.isNotEmpty())
    }

    @Test
    fun `test custom voice model training`() = runTest {
        val processor = AdvancedVoiceCloningProcessor()
        val trainingSamples = listOf(
            createTestAudioData(),
            createTestAudioData(),
            createTestAudioData(),
            createTestAudioData(),
            createTestAudioData()
        )
        
        val characteristics = VoiceCharacteristics(
            emotion = Emotion.HAPPY,
            gender = Gender.MALE,
            age = Age.YOUNG_ADULT
        )
        
        val model = processor.trainCustomVoiceModel("test_model", trainingSamples, characteristics)
        
        assertNotNull(model)
        assertEquals("test_model", model.id)
        assertEquals(characteristics, model.characteristics)
        assertEquals(5, model.trainingSamples)
        assertTrue(model.createdAt > 0)
    }

    @Test
    fun `test voice effect types`() {
        // 测试基础音效类型
        val basicEffects = VoiceEffect.values().filter { it.effectType == VoiceEffectType.BASIC }
        assertTrue(basicEffects.contains(VoiceEffect.NONE))
        assertTrue(basicEffects.contains(VoiceEffect.ROBOT))
        
        // 测试克隆音效类型
        val clonedEffects = VoiceEffect.values().filter { it.effectType == VoiceEffectType.CLONED }
        assertTrue(clonedEffects.contains(VoiceEffect.LAZY_CAT))
        assertTrue(clonedEffects.contains(VoiceEffect.SPONGEBOB))
        
        // 测试自定义音效类型
        val customEffect = VoiceEffect.CUSTOM
        assertEquals(VoiceEffectType.CUSTOM, customEffect.effectType)
    }

    @Test
    fun `test voice characteristics validation`() {
        val characteristics = VoiceCharacteristics(
            emotion = Emotion.HAPPY,
            gender = Gender.MALE,
            age = Age.ADULT,
            accent = "Chinese",
            pitch = 0.8f..1.2f,
            formantShift = 1.0f
        )
        
        assertEquals(Emotion.HAPPY, characteristics.emotion)
        assertEquals(Gender.MALE, characteristics.gender)
        assertEquals(Age.ADULT, characteristics.age)
        assertEquals("Chinese", characteristics.accent)
        assertEquals(0.8f, characteristics.pitch.start)
        assertEquals(1.2f, characteristics.pitch.endInclusive)
        assertEquals(1.0f, characteristics.formantShift)
    }

    private fun createTestAudioData(): ByteArray {
        // 创建测试音频数据（1秒的44.1kHz 16位音频）
        val samples = 44100
        val audioData = ByteArray(samples * 2)
        
        // 生成简单的正弦波
        for (i in 0 until samples) {
            val sample = (Math.sin(2 * Math.PI * 440 * i / 44100) * Short.MAX_VALUE).toInt()
            audioData[i * 2] = (sample and 0xFF).toByte()
            audioData[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
        }
        
        return audioData
    }

    private fun createTestVoiceFeatures(): VoiceFeatures {
        return VoiceFeatures(
            pitch = 200f,
            formants = listOf(500f, 1500f, 2500f, 3500f),
            spectralEnvelope = FloatArray(25) { 0.5f },
            energy = 1000f,
            jitter = 0.01f,
            shimmer = 0.02f,
            spectralTilt = -6f,
            speakingRate = 4.5f,
            mfcc = FloatArray(13) { 0.1f }
        )
    }

    private fun createTestCustomModel(): CustomVoiceModel {
        return CustomVoiceModel(
            id = "test_model",
            characteristics = VoiceCharacteristics(
                emotion = Emotion.HAPPY,
                gender = Gender.MALE,
                age = Age.YOUNG_ADULT
            ),
            modelData = "test_model_data".toByteArray(),
            trainingSamples = 10,
            createdAt = System.currentTimeMillis()
        )
    }
}