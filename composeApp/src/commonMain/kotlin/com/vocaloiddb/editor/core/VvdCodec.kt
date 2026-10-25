package btm.m.vthief.core

/** The reversible character substitution used by the supplied VVD Editor. */
object VvdCodec {
    private val mapping: Map<Char, Char> = buildMap {
        val explicit = mapOf(
            'A' to '[', '[' to 'A', '_' to 'E', 'E' to '_', 'H' to 'R', 'R' to 'H', 'S' to 'I', 'I' to 'S',
            'O' to 'U', 'U' to 'O', 'J' to 'P', 'P' to 'J', 'K' to 'Q', 'Q' to 'K', 'W' to 'M', 'M' to 'W',
            '^' to 'D', 'D' to '^', '\\' to 'F', 'F' to '\\', ']' to 'G', 'G' to ']', '@' to 'Z', 'Z' to '@',
            'B' to 'X', 'X' to 'B', 'C' to 'Y', 'Y' to 'C', 'L' to 'V', 'V' to 'L', 'T' to 'N', 'N' to 'T',
            '+' to '1', '1' to '+', '(' to '2', '2' to '(', ')' to '3', '3' to ')', '.' to '4', '4' to '.',
            '/' to '5', '5' to '/', ',' to '6', '6' to ',', '-' to '7', '7' to '-', '"' to '8', '8' to '"',
            '#' to '9', '9' to '#', '*' to '0', '0' to '*', '\'' to '=', '=' to '\'',
            '{' to 'a', 'a' to '{', 'h' to 'r', 'r' to 'h', 'n' to 't', 't' to 'n', 'o' to 'u', 'u' to 'o',
            'g' to '}', '}' to 'g', 'j' to 'p', 'p' to 'j', 'k' to 'q', 'q' to 'k', 'i' to 's', 's' to 'i',
            'e' to '\u007f', '\u007f' to 'e', '~' to 'd', 'd' to '~', 'b' to 'x', 'x' to 'b', 'c' to 'y',
            'y' to 'c', 'l' to 'v', 'v' to 'l', '`' to 'z', 'z' to '`', 'm' to 'w', 'w' to 'm', 'f' to '|', '|' to 'f',
        )
        putAll(explicit)
    }

    fun transcode(value: String): String = buildString(value.length) {
        value.forEach { append(mapping[it] ?: it) }
    }

    fun generateTemplate(compId: String, voiceName: String): String {
        require(compId.length == 16 && compId.startsWith("B")) { "VVD 的 VOICEIDSTR 必须是 B 开头的 16 字符 CompID" }
        return listOf(
            "\"ID\" = \"VOCALOID VIRTUAL VOICE\"",
            "\"FORMAT\" = \"4.0.0.0\"",
            "\"VOICEIDSTR\" = \"$compId\"",
            "\"VOICENAME\" = \"${voiceName.replace("\"", "") }\"",
            "\"Breathiness\" = \"0\"", "\"Brightness\" = \"0\"", "\"Clearness\" = \"0\"",
            "\"Opening\" = \"0\"", "\"Gender Factor\" = \"0\"", "\"Growl\" = \"1\"",
        ).joinToString("\n")
    }
}
