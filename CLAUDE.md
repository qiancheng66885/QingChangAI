# 清畅AI — 法律

> 项目结构和架构详见 [PROJECT.md](./PROJECT.md)
> PROJECT.md 是架构规范的**唯一来源**，所有目录结构和调用关系以它为准

## 架构规则
- **强制**：新增/移动文件必须遵循 PROJECT.md 的目录结构，不得自行创建 PROJECT.md 外的包
- **强制**：API 适配器（`business/adapter/`）必须同时兼容 OpenAI 格式和 Anthropic 格式，对接中转站后直接可用
- **强制**：所有 OkHttpClient 必须通过 `HttpClientFactory` 单例获取，禁止自行 new OkHttpClient
- **强制**：文件持久化统一用 `InMemoryStore`，禁止直接写文件
- 每次新增/删除/重命名文件、改变目录结构、调整调用链时，**必须同步更新 PROJECT.md**

## 提交前检查
> 以下操作必须在每次完成任务、准备提交代码前执行，不得跳过

1. **PROJECT.md 对齐**：对比 PROJECT.md 目录树与 `app/src/main/java/com/aiaggregator/app/` 磁盘文件，确认无遗漏、无多余
2. **孤儿目录清理**：检查是否存在空目录或仅含 `.gitkeep` 的空壳包，删除不属于 PROJECT.md 的孤儿目录
3. **构建验证**：`./gradlew compileDebugKotlin` 必须通过
4. **版本号检查**：如有功能变更，确认 `build.gradle.kts`、`UpdateChecker.getAppVersion()`、`SettingsScreen` 三处版本号一致

## 外部规则
- 推送与版本规则：见 memory 目录下 `feedback_no_auto_push.md` 和 `feedback_push_rules.md`（自动加载）

## Agent 提示词规则
- 写 Agent 提示词时，只传跟当前任务直接相关的信息（文件路径、当前问题、关键约束）
- 不要复述整个对话历史，更不要把十几轮以前的无关内容塞进去
- 判断标准：这段背景信息对当前任务有没有直接帮助？没有就不传
- 例外：用户明确要求全面检查整个项目时，可以全面传

## 辅助优化
- 文件编辑优先用 Edit（只传 diff），避免 Write 整个文件
- 回复简短直接，不写多段落总结和废话开场白
- 不要写三个以上并行的 Agent

## 用户提醒
在以下关键时刻简短提醒用户：
- 任务完成后：提醒可以关会话，别挂一天
- 任务切换时：提醒开新会话更省 token
- 需要大量搜索时：提醒用 Explore Agent
- 对话明显变长时：提醒可以 /clear 或开新会话
