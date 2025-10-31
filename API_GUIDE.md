# VoxAI - API集成指南

## OpenAI API配置

### 1. 获取API密钥

1. 访问 [OpenAI Platform](https://platform.openai.com/)
2. 注册或登录账号
3. 进入 [API keys页面](https://platform.openai.com/api-keys)
4. 点击 "Create new secret key"
5. 为密钥命名(例如: "VoxAI-Dev")
6. 复制生成的密钥(只显示一次)

### 2. 配置密钥

在项目根目录创建 `local.properties` 文件:

```properties
sdk.dir=/path/to/your/android/sdk
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**重要**: 
- `local.properties` 已在 `.gitignore` 中,不会被提交到版本控制
- 切勿将API密钥硬编码在源代码中
- 切勿将API密钥提交到公开仓库

### 3. API使用限制

#### 免费层级
- 每月$5美元额度(前3个月)
- 速率限制: 3 RPM (每分钟请求数)
- 模型: GPT-3.5 Turbo

#### 付费层级
根据使用量收费:
- GPT-3.5 Turbo: $0.0015 / 1K tokens (输入), $0.002 / 1K tokens (输出)
- GPT-4: $0.03 / 1K tokens (输入), $0.06 / 1K tokens (输出)

详见: [OpenAI Pricing](https://openai.com/pricing)

### 4. API请求示例

#### 基本聊天请求

```kotlin
val request = OpenAIRequest(
    model = "gpt-3.5-turbo",
    messages = listOf(
        Message(role = "system", content = "你是一个友好的AI助手"),
        Message(role = "user", content = "你好!")
    ),
    temperature = 0.7f,
    maxTokens = 150
)

val response = apiService.chatCompletion(request)
val reply = response.choices.first().message.content
```

#### 带上下文的对话

```kotlin
val conversationHistory = listOf(
    Message(role = "user", content = "我喜欢编程"),
    Message(role = "assistant", content = "太棒了!你喜欢哪种编程语言?"),
    Message(role = "user", content = "Kotlin和Python")
)

val request = OpenAIRequest(
    model = "gpt-3.5-turbo",
    messages = conversationHistory + Message(role = "user", content = "推荐一些学习资源"),
    temperature = 0.7f
)
```

### 5. 参数说明

#### model (必需)
- `gpt-3.5-turbo`: 快速,性价比高,推荐用于大多数场景
- `gpt-4`: 更强大,更准确,但成本更高
- `gpt-3.5-turbo-16k`: 支持更长的上下文(16K tokens)

#### messages (必需)
消息数组,每条消息包含:
- `role`: "system"(系统提示) | "user"(用户) | "assistant"(AI)
- `content`: 消息内容

#### temperature (可选, 默认1.0)
控制回复的随机性:
- 0.0-0.3: 更确定,更一致
- 0.4-0.7: 平衡创造性和一致性(推荐)
- 0.8-1.0: 更有创造性,更随机
- 1.0-2.0: 非常随机(不推荐常规使用)

#### max_tokens (可选)
限制回复的最大token数:
- 不设置: 使用模型默认值
- 设置较小值(如150-500): 获得简短回复,节省成本
- 设置较大值: 允许更详细的回复

### 6. 错误处理

#### 常见错误码

**401 Unauthorized**
```
原因: API密钥无效或未提供
解决: 检查local.properties中的API密钥是否正确
```

**429 Too Many Requests**
```
原因: 超过速率限制
解决: 
- 实现请求队列
- 添加重试逻辑
- 升级到付费计划
```

**500 Internal Server Error**
```
原因: OpenAI服务器错误
解决: 稍后重试,检查OpenAI状态页
```

**503 Service Unavailable**
```
原因: 服务暂时不可用(通常是负载过高)
解决: 实现指数退避重试策略
```

#### 错误处理实现

```kotlin
suspend fun sendMessageWithRetry(
    message: String,
    maxRetries: Int = 3
): Result<ChatMessage> {
    var lastException: Exception? = null
    
    repeat(maxRetries) { attempt ->
        try {
            return chatRepository.sendMessage(message)
        } catch (e: HttpException) {
            when (e.code()) {
                429 -> {
                    // 速率限制,等待后重试
                    delay(1000L * (attempt + 1))
                }
                500, 503 -> {
                    // 服务器错误,指数退避
                    delay(1000L * (2.0.pow(attempt)).toLong())
                }
                else -> {
                    // 其他错误不重试
                    return Result.failure(e)
                }
            }
            lastException = e
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    return Result.failure(lastException ?: Exception("Unknown error"))
}
```

### 7. 最佳实践

#### 优化Token使用
```kotlin
// ✅ 好的做法: 限制上下文历史
val recentMessages = allMessages.takeLast(10)

// ❌ 不好的做法: 发送所有历史消息
val allMessages = chatHistory.getAllMessages()
```

#### 系统提示优化
```kotlin
// ✅ 清晰的系统提示
Message(
    role = "system",
    content = "你是VoxAI助手,一个友好、幽默的AI。请用简洁的语言回答问题。"
)

// ❌ 过长或模糊的系统提示
Message(
    role = "system", 
    content = "你是一个非常智能的助手,要回答所有问题..."
)
```

#### 缓存和节流
```kotlin
// 防抖: 用户停止输入后才发送
var job: Job? = null
fun onMessageChange(text: String) {
    job?.cancel()
    job = viewModelScope.launch {
        delay(500) // 等待500ms
        // 执行搜索建议等操作
    }
}
```

### 8. 安全建议

1. **保护API密钥**
   - 使用环境变量或安全存储
   - 不要在客户端硬编码
   - 考虑使用后端代理

2. **输入验证**
   ```kotlin
   fun validateMessage(message: String): Boolean {
       return message.isNotBlank() && 
              message.length <= 2000 &&
              !message.contains(suspiciousPatterns)
   }
   ```

3. **速率限制**
   ```kotlin
   private var lastRequestTime = 0L
   private val minRequestInterval = 1000L // 1秒
   
   fun canSendRequest(): Boolean {
       val now = System.currentTimeMillis()
       return now - lastRequestTime >= minRequestInterval
   }
   ```

4. **日志脱敏**
   ```kotlin
   // ❌ 不要记录完整API密钥
   Log.d(TAG, "API Key: $apiKey")
   
   // ✅ 记录脱敏版本
   Log.d(TAG, "API Key: ${apiKey.take(8)}...")
   ```

### 9. 生产环境配置

对于生产环境,建议:

1. **使用后端代理**
   ```
   客户端 → 你的后端服务器 → OpenAI API
   ```
   
   优势:
   - 保护API密钥
   - 实现自定义速率限制
   - 添加使用统计
   - 过滤不当内容

2. **监控和日志**
   - 记录API调用次数
   - 监控响应时间
   - 追踪错误率
   - 设置成本告警

3. **备选方案**
   - 准备降级策略
   - 实现离线模式
   - 缓存常见回复

### 10. 替代API提供商

如果需要,可以集成其他AI服务:

#### Anthropic Claude
```kotlin
interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun sendMessage(@Body request: ClaudeRequest): ClaudeResponse
}
```

#### Google PaLM
```kotlin
interface PaLMApiService {
    @POST("v1beta2/models/chat-bison-001:generateMessage")
    suspend fun generateMessage(@Body request: PaLMRequest): PaLMResponse
}
```

#### 本地模型
- Ollama (运行本地LLM)
- llama.cpp (移动端优化)

### 11. 成本优化建议

1. **使用流式响应** (降低感知延迟)
2. **实现智能缓存** (相同问题不重复请求)
3. **选择合适的模型** (不是所有场景都需要GPT-4)
4. **限制回复长度** (设置max_tokens)
5. **压缩对话历史** (总结旧对话,减少tokens)

### 12. 测试API集成

#### 使用Mock数据测试

```kotlin
class MockChatRepository : ChatRepository {
    override suspend fun sendMessage(message: String): Result<ChatMessage> {
        delay(1000) // 模拟网络延迟
        return Result.success(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                content = "这是模拟回复: $message",
                isUser = false
            )
        )
    }
}
```

#### 单元测试

```kotlin
@Test
fun `test API request format`() = runTest {
    val request = OpenAIRequest(
        model = "gpt-3.5-turbo",
        messages = listOf(
            Message(role = "user", content = "Hello")
        )
    )
    
    assertEquals("gpt-3.5-turbo", request.model)
    assertEquals(1, request.messages.size)
}
```

## 技术支持

如有问题,请参考:
- [OpenAI API文档](https://platform.openai.com/docs/api-reference)
- [OpenAI社区](https://community.openai.com/)
- [项目Issues](https://github.com/yourusername/VoxAI/issues)
