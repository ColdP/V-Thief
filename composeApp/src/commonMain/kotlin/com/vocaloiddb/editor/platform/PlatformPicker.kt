package btm.m.vthief.platform

expect object PlatformPicker {
    fun chooseDirectory(initial: String? = null): String?
    fun chooseFile(title: String, extensions: List<String>): String?
    fun chooseSaveFile(title: String, suggestedName: String, extension: String): String?
}

