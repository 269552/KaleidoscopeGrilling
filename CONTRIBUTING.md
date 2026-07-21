# 参与开发

感谢你为《森罗物语：烟火》提交问题或改进。

## 开发环境

- Forge 1.20.1 使用 Java 17。
- NeoForge 1.21.1 使用 Java 21。
- 依赖由 Gradle 从公开 Maven 仓库解析，不要提交本地模组 JAR、运行目录或构建输出。

## 修改原则

1. 同一玩法应在 Forge 与 NeoForge 中保持一致。
2. 加载器差异应限制在注册、事件、网络和能力接口层。
3. 公共资源放在 `common/src/main/resources`；仅平台专用资源放在对应模块。
4. 不要直接读写未在 `COMPATIBILITY.md` 中公开的兼容数据。
5. 新增玩家可见文本时同时更新 `zh_cn.json` 与 `en_us.json`。
6. 新增资源时检查模型、贴图、声音和数据包引用，避免缺失资源回退。

## 提交前检查

```powershell
.\gradlew.bat -p neoforge-1.21.1 build
Set-Location forge-1.20.1
.\gradlew.bat build
```

同时确认：

- `git diff --check` 无空白错误。
- 没有提交 `build/`、`bin/`、`runs/`、日志、崩溃转储或本地 JAR。
- Forge 与 NeoForge 均能进入游戏并完成所修改功能的实际操作。

## 许可证

提交代码即表示你同意代码按 BSD 3-Clause 发布；提交模型、贴图、音频或其他资源即表示你同意相关资源按 CC BY-NC-SA 4.0 发布。
