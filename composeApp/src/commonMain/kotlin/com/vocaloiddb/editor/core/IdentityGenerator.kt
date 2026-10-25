package btm.m.vthief.core

/** CompID/KEY helpers. CompID and XSY follow the formats used by DevTool-All-in-One. */
object IdentityGenerator {
    fun generateFromCodes(vendor: String, product: String, language: String, version: String, trialExpiresOn: String = ""): KeyBundle {
        val drp = CompIdCodec.drp(vendor, product, language, version)
        val compId = CompIdCodec.calculate(drp)
        return bundleFor(compId, trialExpiresOn)
    }
    fun bundleFor(compId: String, trialExpiresOn: String = ""): KeyBundle {
        require(compId.length == 16 && compId.startsWith("B")) { "CompID 必须是 B 开头的 16 字符值" }
        return KeyBundle(compId, localKey(compId, trialExpiresOn), XsyProfile("", "", listOf(compId)), trialExpiresOn)
    }
    private fun localKey(compId: String, expiry: String): String {
        val d = digest("V-THIEF|$compId|$expiry".encodeToByteArray())
        return d.take(16).joinToString("") { it.toUByte().toString(16).padStart(2, '0').uppercase() }
    }
    fun generateXsy(compIds: List<String>, groupName: String): XsyProfile {
        val members = compIds.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        require(members.size >= 2) { "XSY 至少需要两个 CompID" }
        require(members.all { it.length == 16 && it.startsWith("B") }) { "XSY 成员必须是 B 开头的 CompID" }
        require(groupName.isNotBlank()) { "XSY 组名不能为空" }
        return XsyProfile(members.joinToString("\n") { XsyCodec.encode(it, groupName) }, groupName, members)
    }
    private fun digest(input: ByteArray): ByteArray {
        var a = 0x243f6a88L; var b = 0x85a308d3L; var c = 0x13198a2eL; var d = 0x03707344L
        input.forEachIndexed { index, byte -> val x = byte.toLong() and 0xff; a = (a xor (x + index)) * 0x100000001b3L; b = (b + (a xor x)) * -7046029254386353131L; c = (c xor (b ushr 7)) * -4417276706812531889L; d = (d + (c xor (a ushr 11))) * 0x165667b19e3779f9L }
        val out = ByteArray(32); longArrayOf(a, b, c, d).forEachIndexed { wi, word -> repeat(8) { bi -> out[wi * 8 + bi] = (word ushr (bi * 8)).toByte() } }; return out
    }
}
