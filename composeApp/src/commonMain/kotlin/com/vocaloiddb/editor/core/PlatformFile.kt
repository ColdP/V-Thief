package btm.m.vthief.core

expect class PlatformFile(path: String) {
    val absolutePath: String
    val name: String
    val extension: String
    val length: Long
    val exists: Boolean
    val isDirectory: Boolean
    fun resolve(child: String): PlatformFile
    fun mkdirs()
    fun readBytes(): ByteArray
    fun writeBytes(bytes: ByteArray)
    fun walkFiles(): List<PlatformFile>
}

