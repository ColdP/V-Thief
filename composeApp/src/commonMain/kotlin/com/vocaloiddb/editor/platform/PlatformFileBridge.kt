package btm.m.vthief.platform

expect object PlatformFileBridge {
    fun writeText(path: String, value: String)
    fun readText(path: String): String
}

