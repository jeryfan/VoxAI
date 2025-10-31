package com.voxai.domain.model

enum class VoiceEffect(
    val displayName: String, 
    val pitchShift: Float, 
    val speedMultiplier: Float,
    val effectType: VoiceEffectType = VoiceEffectType.BASIC,
    val voiceCharacteristics: VoiceCharacteristics? = null
) {
    // 基础音效
    NONE("原声", 1.0f, 1.0f, VoiceEffectType.BASIC),
    MALE("男声", 0.8f, 0.95f, VoiceEffectType.BASIC),
    FEMALE("女声", 1.3f, 1.05f, VoiceEffectType.BASIC),
    CHILD("儿童声", 1.5f, 1.1f, VoiceEffectType.BASIC),
    ROBOT("机器人", 1.0f, 1.0f, VoiceEffectType.BASIC),
    MONSTER("怪物", 0.6f, 0.9f, VoiceEffectType.BASIC),
    ALIEN("外星人", 1.8f, 1.15f, VoiceEffectType.BASIC),
    DEEP("低沉", 0.7f, 0.92f, VoiceEffectType.BASIC),
    CHIPMUNK("花栗鼠", 1.6f, 1.2f, VoiceEffectType.BASIC),
    
    // 高级声音克隆效果
    LAZY_CAT("懒洋洋", 0.85f, 0.8f, VoiceEffectType.CLONED, 
        VoiceCharacteristics(emotion = Emotion.LAZY, gender = Gender.MALE, age = Age.ADULT)),
    
    SPONGEBOB("海绵宝宝", 1.4f, 1.25f, VoiceEffectType.CLONED,
        VoiceCharacteristics(emotion = Emotion.EXCITED, gender = Gender.MALE, age = Age.YOUNG_ADULT)),
    
    PORKY_PIG("猪猪侠", 0.9f, 1.1f, VoiceEffectType.CLONED,
        VoiceCharacteristics(emotion = Emotion.CHEERFUL, gender = Gender.MALE, age = Age.CHILD)),
    
    ELSA("艾莎公主", 1.35f, 1.05f, VoiceEffectType.CLONED,
        VoiceCharacteristics(emotion = Emotion.ELEGANT, gender = Gender.FEMALE, age = Age.YOUNG_ADULT)),
    
    OPTIMUS_PRIME("擎天柱", 0.65f, 0.85f, VoiceEffectType.CLONED,
        VoiceCharacteristics(emotion = Emotion.AUTHORITATIVE, gender = Gender.MALE, age = Age.ADULT)),
    
    MINION("小黄人", 1.6f, 1.3f, VoiceEffectType.CLONED,
        VoiceCharacteristics(emotion = Emotion.PLAYFUL, gender = Gender.NEUTRAL, age = Age.CHILD)),
    
    CUSTOM("自定义", 1.0f, 1.0f, VoiceEffectType.CUSTOM)
}

enum class VoiceEffectType {
    BASIC,    // 基础音高和速度调整
    CLONED,   // 声音克隆效果
    CUSTOM    // 用户自定义训练的声音
}

data class VoiceCharacteristics(
    val emotion: Emotion,
    val gender: Gender,
    val age: Age,
    val accent: String? = null,
    val pitch: ClosedFloatingPointRange<Float> = 0.5f..2.0f,
    val formantShift: Float = 1.0f
)

enum class Emotion {
    NEUTRAL, HAPPY, SAD, ANGRY, EXCITED, LAZY, CHEERFUL, ELEGANT, AUTHORITATIVE, PLAYFUL
}

enum class Gender {
    MALE, FEMALE, NEUTRAL
}

enum class Age {
    CHILD, TEENAGER, YOUNG_ADULT, ADULT, ELDERLY
}
