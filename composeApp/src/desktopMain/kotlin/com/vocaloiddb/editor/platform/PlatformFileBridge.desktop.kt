package btm.m.vthief.platform

import java.io.File

actual object PlatformFileBridge {
    actual fun writeText(path: String, value: String) { File(path).apply { parentFile?.mkdirs() }.writeText(value) }
}

