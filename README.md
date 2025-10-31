# VoxAI - AI聊天变声器

一款集成AI聊天功能的Android变声器应用，让你的声音充满创意，与AI对话更有趣。

## 功能特性

### 🎙️ 变声器功能
- **实时录音变声** - 录制语音并应用实时变声效果
- **多种变声效果** - 提供男声、女声、儿童声、机器人、怪物、外星人等多种音效
- **音频文件导入** - 支持导入本地音频文件进行变声处理
- **音频导出分享** - 保存变声后的音频，分享到社交平台
- **实时预览** - 边录边听，即时调整效果

### 🤖 AI聊天功能
- **智能对话** - 集成先进的AI对话引擎，支持自然语言交流
- **语音交互** - AI回复支持文字转语音（TTS）
- **变声AI回复** - 可对AI的语音回复应用变声效果，打造个性化AI助手
- **对话历史** - 自动保存聊天记录，随时查看历史对话
- **多种AI角色** - 支持不同的AI角色设定

### 🎨 用户体验
- **现代化UI** - 使用Jetpack Compose构建流畅美观的界面
- **深色模式** - 支持浅色/深色主题切换
- **直观操作** - 简洁的设计，易于使用
- **离线功能** - 变声功能可离线使用

## 技术架构

### 核心技术栈
- **开发语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构模式**: MVVM (Model-View-ViewModel)
- **音频处理**: Android AudioRecord/AudioTrack
- **网络请求**: Retrofit + OkHttp
- **本地数据库**: Room
- **依赖注入**: Hilt
- **异步处理**: Kotlin Coroutines + Flow

### 音频处理
- **录音引擎**: AudioRecord API
- **播放引擎**: AudioTrack/MediaPlayer
- **音效算法**: 音调变换、共振峰调整、音速控制
- **音频格式**: PCM、WAV、MP3

### AI集成
- **AI服务**: 支持OpenAI GPT、Claude等主流API
- **TTS服务**: Google TTS / 自定义TTS引擎
- **流式响应**: 支持AI回复流式输出

## 项目结构

```
app/
├── src/main/
│   ├── java/com/voxai/
│   │   ├── data/              # 数据层
│   │   │   ├── local/         # 本地数据（Room数据库）
│   │   │   ├── remote/        # 远程数据（API服务）
│   │   │   └── repository/    # 数据仓库
│   │   ├── domain/            # 业务逻辑层
│   │   │   ├── model/         # 数据模型
│   │   │   └── usecase/       # 用例
│   │   ├── presentation/      # 表现层
│   │   │   ├── voicechanger/  # 变声器界面
│   │   │   ├── aichat/        # AI聊天界面
│   │   │   ├── settings/      # 设置界面
│   │   │   └── common/        # 通用组件
│   │   └── util/              # 工具类
│   │       ├── audio/         # 音频处理工具
│   │       └── permission/    # 权限管理
│   └── res/                   # 资源文件
└── build.gradle.kts
```

## 快速开始

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17 或更高版本
- Android SDK 24+ (Android 7.0+)
- Gradle 8.0+

### 构建步骤

1. 克隆项目
```bash
git clone https://github.com/yourusername/VoxAI.git
cd VoxAI
```

2. 配置API密钥（在 `local.properties` 中）
```properties
OPENAI_API_KEY=your_openai_api_key_here
```

3. 使用Android Studio打开项目

4. 同步Gradle依赖

5. 运行应用

## 权限说明

应用需要以下权限：
- `RECORD_AUDIO` - 录音功能
- `READ_EXTERNAL_STORAGE` - 读取音频文件
- `WRITE_EXTERNAL_STORAGE` - 保存变声音频
- `INTERNET` - AI聊天功能

## 开发计划

- [x] 项目架构设计
- [x] 基础UI框架搭建
- [x] 音频录制模块
- [x] 变声算法实现
- [x] AI聊天集成
- [x] TTS集成
- [ ] 音频效果优化
- [ ] 更多变声预设
- [ ] 云端同步
- [ ] 社交分享功能

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 联系方式

如有问题或建议，欢迎通过以下方式联系：
- 提交 [Issue](https://github.com/yourusername/VoxAI/issues)
- 邮件: support@voxai.app

---

**注意**: 本应用仅供娱乐和学习使用，请勿用于非法用途。
