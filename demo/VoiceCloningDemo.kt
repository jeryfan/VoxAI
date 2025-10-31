package demo

import com.voxai.domain.model.*
import com.voxai.util.audio.*
import kotlinx.coroutines.runBlocking

/**
 * VoxAI 高级声音克隆功能演示
 * 展示如何使用各种声音克隆技术和自定义模型
 */
object VoiceCloningDemo {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        println("🎙️ VoxAI 高级声音克隆功能演示")
        println("=" * 50)

        // 1. 基础音效演示
        demonstrateBasicEffects()
        
        // 2. 高级声音克隆演示
        demonstrateAdvancedVoiceCloning()
        
        // 3. 自定义模型训练演示
        demonstrateCustomModelTraining()
        
        // 4. 声音特征转换演示
        demonstrateVoiceFeatureConversion()
        
        // 5. 质量优化演示
        demonstrateQualityOptimization()
        
        println("\n✨ 演示完成！VoxAI 让声音更有创意！")
    }

    private suspend fun demonstrateBasicEffects() {
        println("\n🎯 1. 基础音效演示")
        println("-" * 30)
        
        val processor = VoiceEffectProcessor()
        val testAudio = createTestAudio("Hello, this is a test audio.")
        
        // 测试各种基础音效
        val basicEffects = listOf(
            VoiceEffect.NONE,
            VoiceEffect.MALE,
            VoiceEffect.FEMALE,
            VoiceEffect.CHILD,
            VoiceEffect.ROBOT,
            VoiceEffect.MONSTER
        )
        
        basicEffects.forEach { effect ->
            val processedAudio = processor.applyEffect(testAudio, effect)
            println("✓ ${effect.displayName} 效果应用成功")
        }
    }

    private suspend fun demonstrateAdvancedVoiceCloning() {
        println("\n🌟 2. 高级声音克隆演示")
        println("-" * 30)
        
        val processor = AdvancedVoiceCloningProcessor()
        val testAudio = createTestAudio("Welcome to the future of voice technology!")
        
        // 演示预设角色声音
        val clonedEffects = listOf(
            VoiceEffect.LAZY_CAT to "懒洋洋",
            VoiceEffect.SPONGEBOB to "海绵宝宝",
            VoiceEffect.ELSA to "艾莎公主",
            VoiceEffect.OPTIMUS_PRIME to "擎天柱",
            VoiceEffect.MINION to "小黄人",
            VoiceEffect.PORKY_PIG to "猪猪侠"
        )
        
        clonedEffects.forEach { (effect, name) ->
            val processedAudio = processor.applyAdvancedEffect(testAudio, effect)
            println("✓ $name 声音克隆成功")
            
            // 显示声音特征
            effect.voiceCharacteristics?.let { characteristics ->
                println("   - 情感: ${characteristics.emotion}")
                println("   - 性别: ${characteristics.gender}")
                println("   - 年龄: ${characteristics.age}")
            }
        }
    }

    private suspend fun demonstrateCustomModelTraining() {
        println("\n🎓 3. 自定义模型训练演示")
        println("-" * 30)
        
        val processor = AdvancedVoiceCloningProcessor()
        
        // 创建训练样本
        val trainingSamples = listOf(
            createTestAudio("这是第一个训练样本"),
            createTestAudio("这是第二个训练样本"),
            createTestAudio("这是第三个训练样本"),
            createTestAudio("这是第四个训练样本"),
            createTestAudio("这是第五个训练样本")
        )
        
        // 定义目标声音特征
        val targetCharacteristics = VoiceCharacteristics(
            emotion = Emotion.CHEERFUL,
            gender = Gender.FEMALE,
            age = Age.YOUNG_ADULT,
            formantShift = 1.2f
        )
        
        // 训练自定义模型
        println("🔄 开始训练自定义声音模型...")
        val customModel = processor.trainCustomVoiceModel(
            name = "欢快女声模型",
            trainingSamples = trainingSamples,
            voiceCharacteristics = targetCharacteristics
        )
        
        println("✓ 自定义模型训练成功!")
        println("   - 模型ID: ${customModel.id}")
        println("   - 训练样本数: ${customModel.trainingSamples}")
        println("   - 目标情感: ${targetCharacteristics.emotion}")
        
        // 使用自定义模型
        val testAudio = createTestAudio("使用自定义模型测试声音转换效果")
        val processedAudio = processor.applyAdvancedEffect(
            testAudio, 
            VoiceEffect.CUSTOM, 
            customModel
        )
        
        println("✓ 自定义模型应用成功!")
    }

    private suspend fun demonstrateVoiceFeatureConversion() {
        println("\n🔧 4. 声音特征转换演示")
        println("-" * 30)
        
        val converter = RealTimeVoiceConverter()
        val extractor = VoiceFeatureExtractor()
        
        // 提取原始声音特征
        val originalAudio = createTestAudio("原始声音样本")
        val originalFeatures = extractor.extractFeatures(originalAudio)
        
        println("📊 原始声音特征:")
        println("   - 基频: ${originalFeatures.pitch} Hz")
        println("   - 能量: ${originalFeatures.energy}")
        println("   - 语速: ${originalFeatures.speakingRate}")
        
        // 转换为不同特征
        val conversions = listOf(
            VoiceCharacteristics(Emotion.HAPPY, Gender.FEMALE, Age.YOUNG_ADULT) to "快乐女性青年",
            VoiceCharacteristics(Emotion.AUTHORITATIVE, Gender.MALE, Age.ADULT) to "权威男性成年",
            VoiceCharacteristics(Emotion.PLAYFUL, Gender.NEUTRAL, Age.CHILD) to "调皮中性儿童"
        )
        
        conversions.forEach { (characteristics, description) ->
            val convertedFeatures = converter.convertVoice(
                sourceFeatures = originalFeatures,
                targetCharacteristics = characteristics,
                pitchShift = 1.0f,
                speedMultiplier = 1.0f
            )
            
            println("✓ 转换为 $description:")
            println("   - 基频变化: ${originalFeatures.pitch} → ${convertedFeatures.pitch} Hz")
            println("   - 能量变化: ${originalFeatures.energy} → ${convertedFeatures.energy}")
            println("   - 语速变化: ${originalFeatures.speakingRate} → ${convertedFeatures.speakingRate}")
        }
    }

    private suspend fun demonstrateQualityOptimization() {
        println("\n⚡ 5. 质量优化演示")
        println("-" * 30)
        
        val processor = AdvancedVoiceCloningProcessor()
        val testAudio = createTestAudio("质量优化测试音频")
        
        // 不同质量设置的处理
        val qualitySettings = listOf(
            VoiceQualitySettings(16000, 64) to "基础质量",
            VoiceQualitySettings(22050, 128) to "标准质量",
            VoiceQualitySettings(44100, 256) to "高质量",
            VoiceQualitySettings(48000, 320) to "极高质量"
        )
        
        qualitySettings.forEach { (settings, description) ->
            println("🎵 $description 处理:")
            println("   - 采样率: ${settings.sampleRate} Hz")
            println("   - 比特率: ${settings.bitrate} kbps")
            
            // 模拟质量优化处理
            val processedAudio = processor.applyAdvancedEffect(testAudio, VoiceEffect.ELSA)
            println("   ✓ 处理完成，音频大小: ${processedAudio.size} bytes")
        }
        
        // 高级设置优化
        val advancedSettings = AdvancedVoiceSettings(
            enableRealTimeProcessing = true,
            enableNoiseSuppression = true,
            preserveEmotion = true,
            voiceQuality = 0.95f
        )
        
        println("\n🔬 高级设置优化:")
        println("   - 实时处理: ${advancedSettings.enableRealTimeProcessing}")
        println("   - 噪音抑制: ${advancedSettings.enableNoiseSuppression}")
        println("   - 情感保持: ${advancedSettings.preserveEmotion}")
        println("   - 音质: ${(advancedSettings.voiceQuality * 100).toInt()}%")
    }

    private fun createTestAudio(text: String): ByteArray {
        // 创建模拟音频数据
        val duration = text.length * 100 // 每个字符100ms
        val sampleRate = 44100
        val samples = (duration * sampleRate / 1000)
        val audioData = ByteArray(samples * 2)
        
        // 生成基于文本内容的音频波形
        for (i in 0 until samples) {
            val time = i.toFloat() / sampleRate
            val frequency = 200f + (text.hashCode() % 200) // 基于文本生成不同频率
            val amplitude = Math.sin(2 * Math.PI * frequency * time).toFloat()
            val sample = (amplitude * Short.MAX_VALUE * 0.3).toInt()
            
            audioData[i * 2] = (sample and 0xFF).toByte()
            audioData[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
        }
        
        return audioData
    }
}

/**
 * 声音质量设置数据类
 */
data class VoiceQualitySettings(
    val sampleRate: Int,
    val bitrate: Int
)

/**
 * 高级声音设置数据类
 */
data class AdvancedVoiceSettings(
    val enableRealTimeProcessing: Boolean,
    val enableNoiseSuppression: Boolean,
    val preserveEmotion: Boolean,
    val voiceQuality: Float
)