# 项目框架文档

## 基本信息
- **项目名**：清畅AI (QingChangAI)
- **路径**：`D:\DevWork\MyApplication\MyApplication\PrivateApp_Studio`
- **类型**：Android 原生 App
- **语言**：Kotlin
- **UI 框架**：Jetpack Compose + Material 3
- **网络层**：OkHttp SSE + kotlinx.serialization
- **架构**：单模块，MVVM 分层

## 目录结构

```
PrivateApp_Studio/
├── app/src/main/java/com/aiaggregator/app/
│   ├── MainActivity.kt                    # 唯一 Activity，全部 Ui 入口
│   │
│   ├── base/constants/
│   │   ├── ApiConstants.kt                # API 超时等常量
│   │   ├── StorageConstants.kt            # 存储 key
│   │   └── UiConstants.kt                 # UI 常量
│   ├── base/ext/
│   │   ├── ContextExt.kt
│   │   └── StringExt.kt
│   ├── base/utils/
│   │   ├── EncryptionUtil.kt              # AES 加解密
│   │   ├── LogUtil.kt
│   │   └── TimeUtil.kt
│   │
│   ├── business/adapter/                  ← API 适配器
│   │   ├── AiAdapter.kt                   # 聊天接口 + ChatMessageItem/Request/Response/Chunk
│   │   ├── AdapterUtils.kt                # 公共工具方法（validateJsonContentType / errorBodySummary）
│   │   ├── OpenAiAdapter.kt               # OpenAI 聊天（SSE 流式）
│   │   ├── AnthropicAdapter.kt            # Anthropic 聊天（SSE 流式）
│   │   ├── ImageGenAdapter.kt             # 图片接口 + ImageGenRequest/EditRequest/Result/ImagePart
│   │   └── OpenAiImageGenAdapter.kt       # OpenAI 图片（generations + edits）
│   ├── business/chat/
│   │   └── ChatService.kt                 # 门面层
│   │
│   ├── data/local/
│   │   ├── ApiKeyStore.kt                 # 加密存储 API 密钥
│   │   ├── InMemoryStore.kt               # JSON 文件持久化（会话 + 消息）
│   │   └── SettingsStore.kt               # DataStore 偏好设置
│   ├── data/model/
│   │   ├── ApiConfig.kt                   # ApiConfig / ModelConfig / ModelCategory / ApiFormatType
│   │   ├── AppSettings.kt                 # AppLanguage / ThemeMode
│   │   ├── Message.kt                     # Message / MessageRole / ContentType / MessageStatus
│   │   └── Session.kt
│   ├── data/remote/
│   │   └── HttpClientFactory.kt           # OkHttp 客户端工厂
│   │
│   ├── ui/chat/
│   │   └── ChatViewModel.kt               ← 核心：sendMessage / generateImageFlow / streamChat
│   ├── ui/common/
│   │   ├── ConfirmDialog.kt
│   │   ├── ErrorView.kt
│   │   └── LoadingDialog.kt
│   ├── ui/config/
│   │   └── ConfigViewModel.kt
│   ├── ui/settings/
│   │   ├── SettingsScreen.kt              # 设置主页
│   │   ├── AboutScreen.kt                 # 关于页（含 GitHub/Gitee 下载）
│   │   ├── DataScreen.kt                  # 数据管理
│   │   ├── SupportScreen.kt               # 软件支持与教程（教程 + FAQ）
│   │   └── UpdateChecker.kt               # 版本更新检查
│   └── ui/theme/
│       ├── Color.kt
│       └── Theme.kt
│
├── app/src/main/res/                      # Android 资源
├── screenshots/
├── README.md
└── build.gradle.kts
```

## 架构关系

```
MainActivity (UI Composable)
  ↓ 调用
ChatViewModel / ConfigViewModel
  ↓ 调用
ChatService (门面)
  ↓ 委托
OpenAiAdapter / AnthropicAdapter  ← 聊天（根据 ApiFormatType 动态选）
OpenAiImageGenAdapter             ← 图片（硬编码，不走 formatType 分支）
```

- 图片生成/编辑 **不走** ApiFormatType 分支，统一用 OpenAI 兼容接口
- 聊天则根据 `ApiFormatType` 动态选择 OpenAI 或 Anthropic 适配器

## 数据模型速查

| 类 | 关键字段 |
|---|---|
| `ApiConfig` | baseUrl + apiKey + formatType (OPENAI_COMPATIBLE / ANTHROPIC_COMPATIBLE) |
| `ModelConfig` | modelName + category (CHAT/IMAGE) + 关联 platformId |
| `Message` | role + content + imageUrl + contentType (TEXT/IMAGE/FILE/ERROR) + status |
| `Session` | id + title + lastActiveAt |

## 变更记录
> 每次框架或目录变动后追加一行

- **2026-05-10**：架构修复 — 删除 4 个空壳目录（business/config、intent、session、ui/update）；InMemoryStore 线程安全重写；HttpClientFactory 单例化；networkSecurityConfig 支持 HTTP 明文代理；AnthropicAdapter/OpenAiAdapter 多项 SSE 兼容修复；新增 AdapterUtils 提取公共方法
- **2026-05-10**：多图编辑重构 — ImageGenAdapter 新增 ImagePart、ImageEditRequest.images 改为 List<ImagePart>、OpenAiImageGenAdapter 支持多图 multipart + 全参数 + b64_json 解析、ChatViewModel.generateImageFlow 合并编辑+上传逻辑、MainActivity 多文件选择 + LazyRow 预览
- **2026-05-10**：SupportScreen 删除"开源下载"卡片（统一到 AboutScreen）
- **2026-05-10**：AboutScreen 增加 GitHub/Gitee 下载入口
