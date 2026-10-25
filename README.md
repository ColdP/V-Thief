# V-Thief

一个以 Kotlin Multiplatform + Compose Multiplatform 编写、使用 [MIUIX](https://compose-miuix-ui.github.io/miuix/) 视觉组件的 VOCALOID 声库编辑器。桌面目标支持 Windows、macOS 和 Linux，核心数据层位于 `commonMain`，便于后续接入 Android/iOS。

应用名：**V-Thief** · Kotlin 包名：`btm.m.vthief`

## 当前能力

- 打开工作区并扫描 `.ddi`、`.ddb`、`.vvd` 文件。
- 解析 DDB 中的 `SND ` 块，显示采样率、声道、偏移和大小。
- 将单个采样或全部采样导出为标准 PCM WAV。
- 在保持二进制布局的前提下替换等长 PCM 采样，并支持导出新的 DDB。
- 从 DDI 中提取可识别音素，支持等字节长度的发音替换，避免破坏偏移表。
- 根据语言和声库元数据生成确定性的 CompID、完整性 KEY 和 XSY 配置草案。

## 关于 KEY

KEY 生成器用于用户拥有的声库的**完整性标识、构建追踪和项目元数据**。它不会生成或伪造 VOCALOID 官方授权/激活密钥，也不会绕过厂商授权校验；正版授权仍需使用厂商提供的许可。

## 构建

安装 JDK 17 或更高版本后运行：

```bash
./gradlew :composeApp:run
```

Windows：

```powershell
.\gradlew.bat :composeApp:run
```

首次构建会从 Maven Central 下载 Compose Multiplatform 与 MIUIX 依赖。

