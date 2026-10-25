package btm.m.vthief.core

/** License-safe identifiers for owned/editable voicebank projects. */
object IdentityGenerator {
    private val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun generate(profile: VoicebankProfile, phonemes: List<String>, salt: String = "") : KeyBundle {
        val canonical = buildString {
            append(profile.name.trim().lowercase()).append('|')
            append(profile.language.trim().lowercase()).append('|')
            append(profile.phonemeCount).append('|').append(profile.sampleCount).append('|').append(profile.bytes).append('|')
            phonemes.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted().forEach { append(it).append(',') }
            append('|').append(salt)
        }
        val digest = digest(canonical.encodeToByteArray())
        val compPrefix = profile.language.trim().uppercase().replace(Regex("[^A-Z]"), "").take(3).padEnd(3, 'X')
        val compId = "V4-$compPrefix-${base32(digest).take(16)}"
        val integrity = base32(digest).chunked(5).take(6).joinToString("-")
        val values = mapOf(
            "breathiness" to digest[0].toUByte().toInt() % 128,
            "brightness" to digest[1].toUByte().toInt() % 128,
            "gender" to digest[2].toUByte().toInt() % 128,
            "clearness" to digest[3].toUByte().toInt() % 128,
            "opening" to digest[4].toUByte().toInt() % 128,
            "portamento" to digest[5].toUByte().toInt() % 128,
        )
        return KeyBundle(compId, integrity, XsyProfile(profile.language, profile.phonemeCount, profile.sampleCount, values))
    }

    fun xsyJson(profile: XsyProfile): String = buildString {
        appendLine("{")
        appendLine("  \"format\": \"xsy-profile-v1\",")
        appendLine("  \"language\": \"${escape(profile.language)}\",")
        appendLine("  \"phonemeCount\": ${profile.phonemeCount},")
        appendLine("  \"sampleCount\": ${profile.sampleCount},")
        appendLine("  \"values\": {")
        profile.values.entries.forEachIndexed { index, (key, value) ->
            append("    \"$key\": $value").append(if (index == profile.values.size - 1) "\n" else ",\n")
        }
        appendLine("  }")
        appendLine("}")
    }

    // A small platform-neutral digest is sufficient for stable project identifiers. It is not
    // intended to be a cryptographic license token; use a real signature service for that.
    private fun digest(input: ByteArray): ByteArray {
        var a = 0x243f6a88L; var b = 0x85a308d3L; var c = 0x13198a2eL; var d = 0x03707344L
        input.forEachIndexed { index, byte ->
            val x = byte.toLong() and 0xff
            a = (a xor (x + index)) * 0x100000001b3L
            b = (b + (a xor x)) * -7046029254386353131L
            c = (c xor (b ushr 7)) * -4417276706812531889L
            d = (d + (c xor (a ushr 11))) * 0x165667b19e3779f9L
        }
        val out = ByteArray(32)
        val words = longArrayOf(a, b, c, d)
        words.forEachIndexed { wi, word -> repeat(8) { bi -> out[wi * 8 + bi] = (word ushr (bi * 8)).toByte() } }
        return out
    }
    private fun base32(bytes: ByteArray): String {
        var buffer = 0; var bits = 0; val out = StringBuilder()
        for (byte in bytes) {
            buffer = (buffer shl 8) or byte.toUByte().toInt(); bits += 8
            while (bits >= 5) { bits -= 5; out.append(alphabet[(buffer ushr bits) and 31]) }
        }
        if (bits > 0) out.append(alphabet[(buffer shl (5 - bits)) and 31])
        return out.toString()
    }
    private fun escape(value: String) = value.replace("\\", "\\\\").replace("\"", "\\\"")
}

