# VoxAI - 开发指南

## 环境搭建

### 1. 安装必要工具

#### Android Studio
- 下载并安装 [Android Studio Hedgehog (2023.1.1)](https://developer.android.com/studio) 或更高版本
- 安装Android SDK平台工具
- 配置Android SDK: Tools → SDK Manager
  - SDK Platforms: Android 14.0 (API 34)
  - SDK Tools: Android SDK Build-Tools, Platform-Tools

#### JDK
- 安装JDK 17或更高版本
- 配置JAVA_HOME环境变量

#### Git
- 安装Git版本控制工具
- 配置Git用户信息

### 2. 克隆项目

```bash
git clone https://github.com/yourusername/VoxAI.git
cd VoxAI
```

### 3. 配置API密钥

创建 `local.properties` 文件(复制 `local.properties.example`):

```properties
sdk.dir=/path/to/your/android/sdk
OPENAI_API_KEY=your_actual_openai_api_key_here
```

**获取OpenAI API密钥**:
1. 访问 [OpenAI Platform](https://platform.openai.com/)
2. 注册/登录账号
3. 进入 API keys 页面
4. 创建新的API密钥
5. 复制密钥到 `local.properties`

### 4. 打开项目

1. 启动Android Studio
2. 选择 "Open" 打开项目
3. 等待Gradle同步完成
4. 如果遇到依赖下载问题，检查网络连接或配置镜像

## 项目结构详解

### 目录说明

```
VoxAI/
├── app/                                    # 应用模块
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/voxai/
│   │   │   │   ├── VoxAIApplication.kt    # Application类
│   │   │   │   ├── MainActivity.kt        # 主Activity
│   │   │   │   ├── data/                  # 数据层
│   │   │   │   │   ├── local/             # 本地数据
│   │   │   │   │   │   ├── entity/        # Room实体类
│   │   │   │   │   │   ├── ChatMessageDao.kt
│   │   │   │   │   │   ├── VoxAIDatabase.kt
│   │   │   │   │   │   └── Converters.kt
│   │   │   │   │   ├── remote/            # 远程数据
│   │   │   │   │   │   ├── dto/           # 数据传输对象
│   │   │   │   │   │   └── OpenAIApiService.kt
│   │   │   │   │   └── repository/        # 仓库实现
│   │   │   │   │       └── ChatRepositoryImpl.kt
│   │   │   │   ├── domain/                # 领域层
│   │   │   │   │   ├── model/             # 领域模型
│   │   │   │   │   │   ├── VoiceEffect.kt
│   │   │   │   │   │   ├── AudioData.kt
│   │   │   │   │   │   └── ChatMessage.kt
│   │   │   │   │   └── repository/        # 仓库接口
│   │   │   │   │       └── ChatRepository.kt
│   │   │   │   ├── presentation/          # 表现层
│   │   │   │   │   ├── VoxAIApp.kt        # 主导航
│   │   │   │   │   ├── theme/             # UI主题
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Type.kt
│   │   │   │   │   │   └── Theme.kt
│   │   │   │   │   ├── voicechanger/      # 变声器模块
│   │   │   │   │   │   ├── VoiceChangerViewModel.kt
│   │   │   │   │   │   └── VoiceChangerScreen.kt
│   │   │   │   │   ├── aichat/            # AI聊天模块
│   │   │   │   │   │   ├── AIChatViewModel.kt
│   │   │   │   │   │   └── AIChatScreen.kt
│   │   │   │   │   └── settings/          # 设置模块
│   │   │   │   │       └── SettingsScreen.kt
│   │   │   │   ├── util/                  # 工具类
│   │   │   │   │   └── audio/             # 音频处理
│   │   │   │   │       ├── AudioRecorder.kt
│   │   │   │   │       ├── AudioPlayer.kt
│   │   │   │   │       └── VoiceEffectProcessor.kt
│   │   │   │   └── di/                    # 依赖注入
│   │   │   │       └── AppModule.kt
│   │   │   ├── res/                       # 资源文件
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   └── test/                          # 单元测试
│   └── build.gradle.kts                   # 应用级构建脚本
├── gradle/                                # Gradle配置
│   └── wrapper/
├── build.gradle.kts                       # 项目级构建脚本
├── settings.gradle.kts                    # Gradle设置
├── gradle.properties                      # Gradle属性
├── local.properties.example               # 本地配置示例
├── .gitignore
├── README.md                              # 项目说明
├── REQUIREMENTS.md                        # 需求文档
└── DEVELOPMENT.md                         # 开发指南(本文件)
```

## 架构说明

### MVVM架构

项目采用MVVM(Model-View-ViewModel)架构模式:

```
View (Composable) ←→ ViewModel ←→ Repository ←→ Data Source
                         ↑
                      Domain Model
```

#### 各层职责

1. **View (Composable)**
   - 显示UI
   - 响应用户交互
   - 观察ViewModel的状态
   - 不包含业务逻辑

2. **ViewModel**
   - 持有UI状态
   - 处理用户交互
   - 调用Repository获取/更新数据
   - 将数据转换为UI状态

3. **Repository**
   - 作为数据源的抽象层
   - 协调多个数据源(本地/远程)
   - 实现数据缓存策略

4. **Data Source**
   - 本地: Room数据库
   - 远程: Retrofit API服务

### 依赖注入

使用Hilt进行依赖注入:

```kotlin
// 在Application类上标注
@HiltAndroidApp
class VoxAIApplication : Application()

// 在Activity上标注
@AndroidEntryPoint
class MainActivity : ComponentActivity()

// 在ViewModel中注入依赖
@HiltViewModel
class VoiceChangerViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer
) : ViewModel()

// 定义依赖提供模块
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAudioRecorder(): AudioRecorder = AudioRecorder()
}
```

## 核心模块开发指南

### 1. 变声器模块

#### 音频录制

```kotlin
// AudioRecorder使用示例
val audioRecorder = AudioRecorder()

// 开始录制
lifecycleScope.launch {
    audioRecorder.startRecording()
        .collect { audioChunk ->
            // 处理音频数据块
            processAudioChunk(audioChunk)
        }
}

// 停止录制
audioRecorder.stopRecording()
```

#### 音频变声

```kotlin
// VoiceEffectProcessor使用示例
val processor = VoiceEffectProcessor()

// 应用变声效果
val processedAudio = processor.applyEffect(
    audioData = originalAudio,
    effect = VoiceEffect.ROBOT
)
```

#### 音频播放

```kotlin
// AudioPlayer使用示例
val audioPlayer = AudioPlayer()

lifecycleScope.launch {
    audioPlayer.play(audioData)
}
```

### 2. AI聊天模块

#### 发送消息

```kotlin
// 在ViewModel中
fun sendMessage(content: String) {
    viewModelScope.launch {
        chatRepository.sendMessage(content)
            .onSuccess { aiMessage ->
                // 处理成功响应
            }
            .onFailure { error ->
                // 处理错误
            }
    }
}
```

#### 消息存储

```kotlin
// Room数据库自动处理
// 消息通过Flow自动更新UI
chatRepository.getAllMessages()
    .collect { messages ->
        // UI自动更新
    }
```

### 3. 添加新功能

#### 步骤1: 定义领域模型

```kotlin
// domain/model/NewFeature.kt
data class NewFeature(
    val id: String,
    val data: String
)
```

#### 步骤2: 创建Repository接口

```kotlin
// domain/repository/NewFeatureRepository.kt
interface NewFeatureRepository {
    suspend fun getFeature(): Result<NewFeature>
}
```

#### 步骤3: 实现Repository

```kotlin
// data/repository/NewFeatureRepositoryImpl.kt
class NewFeatureRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : NewFeatureRepository {
    override suspend fun getFeature(): Result<NewFeature> {
        return try {
            val response = apiService.getFeature()
            Result.success(response.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

#### 步骤4: 创建ViewModel

```kotlin
// presentation/newfeature/NewFeatureViewModel.kt
@HiltViewModel
class NewFeatureViewModel @Inject constructor(
    private val repository: NewFeatureRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NewFeatureUiState())
    val uiState: StateFlow<NewFeatureUiState> = _uiState.asStateFlow()
    
    fun loadFeature() {
        viewModelScope.launch {
            repository.getFeature()
                .onSuccess { feature ->
                    _uiState.value = _uiState.value.copy(
                        feature = feature,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
        }
    }
}

data class NewFeatureUiState(
    val feature: NewFeature? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

#### 步骤5: 创建UI

```kotlin
// presentation/newfeature/NewFeatureScreen.kt
@Composable
fun NewFeatureScreen(
    viewModel: NewFeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column {
        when {
            uiState.isLoading -> CircularProgressIndicator()
            uiState.error != null -> Text("Error: ${uiState.error}")
            else -> Text("Feature: ${uiState.feature}")
        }
    }
}
```

## 调试技巧

### 1. Logcat使用

```kotlin
// 使用Android Log
import android.util.Log

private const val TAG = "VoiceChanger"

Log.d(TAG, "Debug message")
Log.e(TAG, "Error message", exception)
```

### 2. 调试变声效果

在`VoiceEffectProcessor`中添加日志:

```kotlin
Log.d(TAG, "Input samples: ${samples.size}")
Log.d(TAG, "Pitch shift: ${effect.pitchShift}")
Log.d(TAG, "Output samples: ${output.size}")
```

### 3. 网络请求调试

已配置OkHttp logging interceptor，在Logcat中过滤"OkHttp"查看网络请求详情。

### 4. 数据库调试

使用Android Studio的Database Inspector:
1. 运行应用
2. View → Tool Windows → App Inspection
3. 选择Database Inspector标签页
4. 查看voxai_database

## 测试

### 运行单元测试

```bash
./gradlew test
```

### 运行UI测试

```bash
./gradlew connectedAndroidTest
```

### 测试覆盖率

```bash
./gradlew testDebugUnitTestCoverage
```

## 构建与发布

### Debug构建

```bash
./gradlew assembleDebug
```

生成的APK位于: `app/build/outputs/apk/debug/`

### Release构建

1. 配置签名密钥:

```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore/voxai-release.jks")
            storePassword = "YOUR_STORE_PASSWORD"
            keyAlias = "voxai"
            keyPassword = "YOUR_KEY_PASSWORD"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

2. 构建Release APK:

```bash
./gradlew assembleRelease
```

生成的APK位于: `app/build/outputs/apk/release/`

### 构建AAB (Android App Bundle)

```bash
./gradlew bundleRelease
```

生成的AAB位于: `app/build/outputs/bundle/release/`

## 常见问题

### Q: Gradle同步失败
A: 
1. 检查网络连接
2. 尝试使用VPN或配置镜像
3. 清理Gradle缓存: `./gradlew clean`

### Q: 编译错误 "Cannot find symbol"
A:
1. 清理并重新构建: Build → Clean Project → Rebuild Project
2. 使缓存失效: File → Invalidate Caches / Restart

### Q: 运行时出现权限错误
A:
1. 检查AndroidManifest.xml中是否声明了权限
2. 检查运行时权限请求代码
3. 在设置中手动授予权限

### Q: AI聊天返回401错误
A:
1. 检查API密钥是否正确配置
2. 确认API密钥有效且有足够配额
3. 检查网络连接

### Q: 录音没有声音
A:
1. 检查是否授予了录音权限
2. 检查设备麦克风是否正常
3. 尝试在其他应用中录音测试

### Q: 变声效果不明显
A:
1. 检查音频数据是否正确录制
2. 调整变声参数(音调、速度)
3. 查看Logcat中的音频处理日志

## 代码规范

### Kotlin编码规范

遵循[Kotlin官方编码规范](https://kotlinlang.org/docs/coding-conventions.html):

1. **命名**:
   - 类名: PascalCase (VoiceChanger)
   - 函数名: camelCase (startRecording)
   - 变量名: camelCase (audioData)
   - 常量: UPPER_SNAKE_CASE (MAX_DURATION)

2. **格式化**:
   - 缩进: 4个空格
   - 行长度: 最多120字符
   - 使用Android Studio自动格式化

3. **注释**:
   - 对复杂逻辑添加注释
   - 公共API使用KDoc注释

### Compose编码规范

1. **Composable命名**: 使用PascalCase
   ```kotlin
   @Composable
   fun VoiceChangerScreen() { }
   ```

2. **参数顺序**:
   - 普通参数
   - Modifier (默认值 = Modifier)
   - lambda参数

3. **状态提升**: 将状态提升到合适的层级

### Git提交规范

使用语义化提交消息:

```
类型(范围): 简短描述

详细描述(可选)

BREAKING CHANGE: 描述(可选)
```

类型:
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建/工具配置

示例:
```
feat(voice-changer): 添加机器人变声效果

实现了基于调制的机器人音效算法,
提供30Hz的调制频率产生机械感。
```

## 性能优化

### 1. 音频处理优化
- 使用协程避免阻塞主线程
- 对大音频文件进行分块处理
- 缓存已处理的音频数据

### 2. UI性能优化
- 避免在Composable中进行重计算
- 使用`remember`缓存计算结果
- 合理使用`LaunchedEffect`和`DisposableEffect`

### 3. 网络请求优化
- 实现请求缓存
- 使用OkHttp的连接池
- 合并多个请求

### 4. 数据库优化
- 使用索引提高查询速度
- 批量操作使用事务
- 定期清理旧数据

## 贡献指南

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 资源链接

### 官方文档
- [Android Developer](https://developer.android.com/)
- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [OpenAI API](https://platform.openai.com/docs)

### 学习资源
- [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Architecture Components](https://developer.android.com/topic/libraries/architecture)

## 支持与反馈

- **问题报告**: [GitHub Issues](https://github.com/yourusername/VoxAI/issues)
- **功能建议**: [GitHub Discussions](https://github.com/yourusername/VoxAI/discussions)
- **Email**: support@voxai.app

---

祝开发愉快! 🚀
