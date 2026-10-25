package btm.m.vthief.core

import java.io.File

actual class PlatformFile actual constructor(private val path: String) {
    private val file get() = File(path)
    actual val absolutePath get() = file.absolutePath
    actual val name get() = file.name
    actual val extension get() = file.extension
    actual val length get() = file.length()
    actual val exists get() = file.exists()
    actual val isDirectory get() = file.isDirectory
    actual fun resolve(child: String) = PlatformFile(File(file, child).path)
    actual fun mkdirs() { file.mkdirs() }
    actual fun readBytes() = file.readBytes()
    actual fun writeBytes(bytes: ByteArray) { file.parentFile?.mkdirs(); file.writeBytes(bytes) }
    actual fun walkFiles(): List<PlatformFile> = if (file.isDirectory) file.walkTopDown().filter { it.isFile }.map { PlatformFile(it.path) }.toList() else emptyList()
}

