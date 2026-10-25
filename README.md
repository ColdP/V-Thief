# V-Thief

一个以 Kotlin Multiplatform + Compose Multiplatform 编写、使用 [MIUIX](https://compose-miuix-ui.github.io/miuix/) 视觉组件的 VOCALOID 声库编辑器。桌面目标支持 Windows、macOS 和 Linux，核心数据层位于 `commonMain`，便于后续接入 Android/iOS。

应用名：**V-Thief** · Kotlin 包名：`btm.m.vthief`

## 当前能力

- 通过选择 `.ddi` 或 `.ddb` 打开工作区，并自动读取同目录的 `.vvd` 与 CompID。
- 解析 DDB 中的 `SND ` 块，显示采样率、声道、偏移和大小。
- 将单个采样或全部采样导出为标准 PCM WAV。
- 在保持二进制布局的前提下替换等长 PCM 采样，并支持导出新的 DDB。
- 从 DDI 中提取可识别音素，支持等字节长度的发音替换，避免破坏偏移表。
- 按 DevTool-All-in-One 的 DRP 算法生成 B 开头的 CompID，并为该 CompID 生成对应 KEYS 与试用期限配置。
- 按 DevTool-All-in-One 的原始 Base64 算法生成 XSY 字符串；XSY 与注册表无关。
- VVD 编解码和模板字段兼容 `D:\WebProject\vvd\index.html` 的映射。

## 关于 KEY

KEYS 与试用期限始终绑定到当前 CompID，并可导出为项目配置。它不会读取、写入或依赖 `VOCALOID4.reg`，也不会伪造厂商的官方授权/激活许可。

## 构建

安装 JDK 17 或更高版本后运行：

```bash
./gradlew :composeApp:run
```

Windows：

```powershell
.\gradlew.bat :composeApp:buildExe
```

可运行文件输出到 `composeApp/build/compose/binaries/main/app/V-Thief/V-Thief.exe`。后续构建测试使用 `buildExe`，项目不生成 MSI 安装包。

首次构建会从 Maven Central 下载 Compose Multiplatform 与 MIUIX 依赖。

