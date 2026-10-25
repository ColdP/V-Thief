package btm.m.vthief.core

/** Raw XSY payload codec used by DevTool-All-in-One. The result is plain Base64 text. */
object XsyCodec {
    fun encode(compId: String, groupName: String): String {
        require(compId.isNotBlank() && groupName.isNotBlank())
        val g = groupName.encodeToByteArray(); val c = compId.encodeToByteArray(); require(g.size < 256 && c.size < 256)
        val n = g.size + c.size
        val inter = ByteArray(n)
        var i3 = 0
        var i4 = n - 1
        while (i4 >= 0) {
            inter[i4] = c[c.size - i3 - 1]
            val ch = if (i3 < g.size) g[i3] else { i3++; c[c.size - i3 - 1] }
            inter[i4 - 1] = ch
            i3++; i4 -= 2
        }
        val diff = ByteArray(n)
        for (i in 1 until n) diff[i] = (inter[i - 1].toInt() xor inter[i].toInt()).toByte()
        diff[0] = (inter[0].toInt() xor diff[n - 1].toInt()).toByte()
        val out = ByteArray(n + 2)
        for (i in 0 until n) out[i] = diff[n - 1 - i]
        out[n] = g.size.toByte(); out[n + 1] = c.size.toByte()
        return Base64.encode(out)
    }
    fun decode(payload: String): Pair<String, String> {
        val raw = Base64.decode(payload.trim()); require(raw.size >= 2)
        val gl = raw[raw.size - 2].toInt() and 255; val cl = raw[raw.size - 1].toInt() and 255; val n = gl + cl
        require(raw.size == n + 2)
        val diff = ByteArray(n); for (i in 0 until n) diff[i] = raw[n - 1 - i]
        val inter = ByteArray(n); inter[0] = (diff[0].toInt() xor diff[n - 1].toInt()).toByte(); for (i in 1 until n) inter[i] = (diff[i].toInt() xor diff[i - 1].toInt()).toByte()
        val comp = ByteArray(cl); val group = ByteArray(gl); var i3 = 0; var i4 = n - 1
        while (i4 >= 0) { comp[cl - i3 - 1] = inter[i4]; if (i3 < gl) group[i3] = inter[i4 - 1] else { i3++; comp[cl - i3 - 1] = inter[i4 - 1] }; i3++; i4 -= 2 }
        return comp.decodeToString() to group.decodeToString()
    }
    private object Base64 {
        private const val ALPH = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        fun encode(bytes: ByteArray): String { val s=StringBuilder((bytes.size+2)/3*4); var i=0; while(i<bytes.size){val a=bytes[i++].toInt() and 255; val b=if(i<bytes.size) bytes[i++].toInt() and 255 else -1; val c=if(i<bytes.size) bytes[i++].toInt() and 255 else -1; s.append(ALPH[a ushr 2]); s.append(ALPH[((a and 3) shl 4) or if(b>=0) b ushr 4 else 0]); s.append(if(b>=0) ALPH[((b and 15) shl 2) or if(c>=0) c ushr 6 else 0] else '='); s.append(if(c>=0) ALPH[c and 63] else '=')}; return s.toString() }
        fun decode(text: String): ByteArray { val clean=text.filterNot { it.isWhitespace() }; val out=ArrayList<Byte>(); var i=0; while(i<clean.length){val a=valOf(clean[i++]); val b=valOf(clean[i++]); val ch=clean[i++]; val d=clean[i++]; val c=if(ch=='=') 0 else valOf(ch); val e=if(d=='=') 0 else valOf(d); out.add(((a shl 2) or (b ushr 4)).toByte()); if(ch!='=') out.add((((b and 15) shl 4) or (c ushr 2)).toByte()); if(d!='=') out.add((((c and 3) shl 6) or e).toByte())}; return out.toByteArray() }
        private fun valOf(ch: Char): Int = ALPH.indexOf(ch).also { require(it >= 0) { "无效 Base64" } }
    }
}
