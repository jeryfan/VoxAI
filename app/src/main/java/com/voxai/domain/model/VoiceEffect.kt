package com.voxai.domain.model

enum class VoiceEffect(val displayName: String, val pitchShift: Float, val speedMultiplier: Float) {
    NONE("原声", 1.0f, 1.0f),
    MALE("男声", 0.8f, 0.95f),
    FEMALE("女声", 1.3f, 1.05f),
    CHILD("儿童声", 1.5f, 1.1f),
    ROBOT("机器人", 1.0f, 1.0f),
    MONSTER("怪物", 0.6f, 0.9f),
    ALIEN("外星人", 1.8f, 1.15f),
    DEEP("低沉", 0.7f, 0.92f),
    CHIPMUNK("花栗鼠", 1.6f, 1.2f)
}
