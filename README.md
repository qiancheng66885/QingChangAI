# 清畅AI — 开源多平台 AI 聚合客户端

清畅AI 是一款开源免费的 Android AI 聚合客户端，支持 OpenAI 兼容格式和 Anthropic 兼容格式的 AI 模型。无需自行搭建复杂基础设施，通过第三方 API 或中转站即可调用各种最新 AI 模型。

---

## 特性

### 聊天

- [x] 流式对话 (SSE)，支持 OpenAI + Anthropic 格式
- [x] 多轮对话，上下文自动截断（超过 40 条取最近）
- [x] 多模态识图（GPT-4V / Claude Vision / GLM-4V 等视觉模型）
- [x] 联网搜索（需模型支持，OpenAI 搜索模型）
- [x] 重新生成回复
- [x] JSON 结构化输出模式
- [x] Token 用量统计显示（输入/输出）
- [x] 当前模型名显示
- [x] 停止生成
- [x] 消息复制 / 分享（系统分享面板）
- [x] Markdown 渲染（彩色标题、粗体、斜体、代码块、列表、引用、表格、待办、分隔线）

### 图片

- [x] 图片生成（DALL·E / gpt-image-1 兼容）
- [x] 图片编辑（DALL·E 2 兼容格式）
- [x] 图片下载保存到相册
- [x] 图片全屏查看 + 双指缩放
- [x] 拍照即时上传
- [x] 相册选择上传

### 模型管理

- [x] 多平台 / 多模型配置
- [x] 厂商预设（OpenAI / Anthropic / DeepSeek / 自定义）
- [x] 一键复制模型配置（自动副本编号）
- [x] 编辑 / 删除模型
- [x] 模型连通性测试

### 数据管理

- [x] 对话历史本地持久化（JSON 加密存储）
- [x] 多会话管理（新建 / 切换 / 删除）
- [x] 按时间范围清除记录（今天 / 近7天 / 近30天 / 近6月 / 近1年 / 全部）
- [x] 缓存清理

### UI / 体验

- [x] Material 3 设计系统
- [x] 深色 / 浅色主题
- [x] 中英文切换
- [x] 打字动画 + 加载进度条
- [x] 流式光标闪烁
- [x] 键盘自适应（发送后收起、空白区点击收起）

---

## 还未支持

- [ ] Function Calling / Tool Use
- [ ] 视频生成
- [ ] 音频生成 (TTS)
- [ ] 语音输入 (STT)
- [ ] PDF / Word / Excel 文件上传
- [ ] MCP 协议
- [ ] Prompt Caching
- [ ] 对话导出
- [ ] 消息长按菜单
- [ ] 对话分支 / 编辑上一条消息
- [ ] 云同步

---

## 支持的模型平台

| 平台 | 格式 | 聊天 | 识图 | 生图 | JSON |
|------|------|:--:|:--:|:--:|:--:|
| **OpenAI** | OpenAI | ✅ | ✅ | ✅ | ✅ |
| **DeepSeek** | OpenAI | ✅ | ❌ | ❌ | ✅ |
| **Anthropic** | Anthropic | ✅ | ✅ | ❌ | ❌ |
| **通义千问** | OpenAI | ✅ | ✅ | ❌ | ✅ |
| **智谱 GLM** | OpenAI | ✅ | ✅ | ❌ | ✅ |
| **Moonshot Kimi** | OpenAI + Anthropic | ✅ | ✅ | ❌ | ❌ |
| **豆包** | OpenAI | ✅ | ✅ | ❌ | ✅ |
| **零一万物** | OpenAI | ✅ | ✅ | ❌ | ❌ |
| **MiniMax** | OpenAI | ✅ | ✅ | ❌ | ❌ |
| **阶跃星辰** | OpenAI | ✅ | ✅ | ❌ | ❌ |
| **百川** | OpenAI | ✅ | ❌ | ❌ | ❌ |

通过 One API / New API / OpenRouter 等中转平台可间接支持 300+ 模型。

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 网络 | OkHttp 4 + SSE |
| 序列化 | kotlinx.serialization |
| 图片加载 | Coil |
| 加密存储 | EncryptedSharedPreferences |
| 设置存储 | DataStore Preferences |
| 数据持久化 | JSON 文件本地存储 |
| 构建 | Gradle 9 + AGP 9 |

---

## 构建

```bash
# 需要 Android Studio Hedgehog+ 和 JDK 17+

# 克隆项目
git clone https://github.com/your-username/QingChangAI.git

# 构建
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

**最低 Android 版本**: API 26 (Android 8.0)  
**目标版本**: API 36 (Android 15)

---

## 项目结构

```
app/src/main/java/com/aiaggregator/app/
├── ui/              # UI 层 — Compose 界面
│   ├── chat/        #   ChatViewModel
│   ├── config/      #   ConfigViewModel
│   ├── settings/    #   设置 / 数据管理 / 关于
│   └── theme/       #   主题配色
├── business/        # 业务层
│   ├── adapter/     #   API 适配器 (OpenAI / Anthropic / ImageGen)
│   └── chat/        #   ChatService
├── data/            # 数据层
│   ├── local/       #   本地存储 (InMemoryStore / ApiKeyStore / SettingsStore)
│   ├── remote/      #   网络 (HttpClientFactory / SSE)
│   └── model/       #   数据模型
├── base/            # 基础层
│   ├── utils/       #   工具类
│   ├── ext/         #   扩展函数
│   └── constants/   #   常量
└── MainActivity.kt  # 入口
```

---

## 许可

开源免费，数据全本地加密存储，无广告、无付费、无追踪。

## 作者

迁城

---

**清畅AI — 方便在手机上用最新的各种模型。**
