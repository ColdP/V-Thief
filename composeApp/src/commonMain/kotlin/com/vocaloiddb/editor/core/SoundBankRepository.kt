package btm.m.vthief.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
/**
 * Safe, layout-preserving operations for VOCALOID4 DDB/DDI files.
 *
 * The DDB format stores PCM16 in SND chunks. We only mutate a chunk when the new
 * payload has exactly the same size, which keeps every DDI offset valid.
 */
class SoundBankRepository {
    suspend fun inspectWorkspace(path: String): List<VoicebankFile> = withContext(Dispatchers.Default) {
        val root = PlatformFile(path)
        if (!root.exists || !root.isDirectory) return@withContext emptyList()
        root.walkFiles().filter { it.extension.lowercase() in setOf("ddi", "ddb", "vvd") }
            .map { file ->
                VoicebankFile(
                    name = file.name,
                    path = file.absolutePath,
                    type = when (file.extension.lowercase()) {
                        "ddi" -> FileType.DDI
                        "ddb" -> FileType.DDB
                        "vvd" -> FileType.VVD
                        else -> FileType.UNKNOWN
                    },
                    sizeBytes = file.length,
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun scanSamples(ddbPath: String): List<SampleRecord> = withContext(Dispatchers.Default) {
        val bytes = PlatformFile(ddbPath).readBytes()
        val result = mutableListOf<SampleRecord>()
        var cursor = 0
        while (cursor + 18 <= bytes.size) {
            val at = indexOf(bytes, SND, cursor)
            if (at < 0 || at + 18 > bytes.size) break
            val length = readInt(bytes, at + 4)
            val rate = readInt(bytes, at + 8)
            val channels = readShort(bytes, at + 12)
            val end = at.toLong() + length
            if (length < 18 || end > bytes.size || rate !in 8_000..192_000 || channels !in 1..8) {
                cursor = at + 4
                continue
            }
            result += SampleRecord(
                index = result.size + 1,
                offset = at.toLong(),
                length = length,
                sampleRate = rate,
                channels = channels,
                pcmBytes = length - 18,
            )
            cursor = end.toInt()
        }
        result
    }

    suspend fun inspectDdi(ddiPath: String): DdiSummary = withContext(Dispatchers.Default) {
        val bytes = PlatformFile(ddiPath).readBytes()
        val text = bytes.decodeToString()
        val sections = listOf("PHDC", "TDB ", "DBV ", "STA ", "ART ", "VQM ").filter { text.contains(it) }
        val phonemes = Regex("(?:\\^?[a-zA-Z0-9@{}\\\\:;!?<>*~()\\-]+)")
            .findAll(text)
            .map { it.value }
            .filter { it.length <= 24 && it.any(Char::isLetter) }
            .distinct()
            .take(500)
            .toList()
        DdiSummary(bytes.size.toLong(), sections, phonemes, text.count { it == 'S' })
    }

    suspend fun resolveCompId(files: List<VoicebankFile>): String? = withContext(Dispatchers.Default) {
        val vvd = files.firstOrNull { it.type == FileType.VVD }
        if (vvd != null) {
            val value = inspectVvd(vvd.path)?.compId
            if (!value.isNullOrBlank() && value.length == 16 && value.startsWith("B")) return@withContext value
        }
        val ddi = files.firstOrNull { it.type == FileType.DDI } ?: return@withContext null
        Regex("\\b[A-Z0-9]{16}\\b")
            .findAll(PlatformFile(ddi.path).readBytes().decodeToString())
            .map { it.value }
            .firstOrNull { it.startsWith("B") }
    }

    suspend fun inspectVvd(vvdPath: String): VvdMetadata? = withContext(Dispatchers.Default) {
        val text = VvdCodec.transcode(PlatformFile(vvdPath).readBytes().decodeToString())
        fun field(key: String): String = Regex("\\\"${Regex.escape(key)}\\\"\\s*=\\s*\\\"([^\\\"]*)\\\"")
            .find(text)?.groupValues?.getOrNull(1).orEmpty()
        val compId = field("VOICEIDSTR")
        if (compId.isBlank() || compId.length != 16 || !compId.startsWith("B")) return@withContext null
        VvdMetadata(
            compId = compId,
            voiceName = field("VOICENAME"),
            format = field("FORMAT"),
            voiceType = field("ID"),
            parameters = listOf("Breathiness", "Brightness", "Clearness", "Opening", "Gender Factor", "Growl")
                .associateWith(::field),
        )
    }

    suspend fun sampleWav(ddbPath: String, sample: SampleRecord): ByteArray = withContext(Dispatchers.Default) {
        val bytes = PlatformFile(ddbPath).readBytes()
        require(sample.offset >= 0 && sample.offset + sample.length <= bytes.size) { "采样偏移超出 DDB 范围" }
        val offset = sample.offset.toInt()
        val actualLength = readInt(bytes, offset + 4)
        require(actualLength == sample.length) { "采样记录与 DDB 已不一致" }
        val pcm = bytes.copyOfRange(offset + 18, offset + actualLength)
        wavHeader(sample.sampleRate, sample.channels, pcm.size) + pcm
    }

    suspend fun exportSample(ddbPath: String, sample: SampleRecord, outputPath: String) = withContext(Dispatchers.Default) {
        PlatformFile(outputPath).writeBytes(sampleWav(ddbPath, sample))
    }

    suspend fun annotateSamples(ddbPath: String, ddiPath: String?, samples: List<SampleRecord>): List<SampleRecord> = withContext(Dispatchers.Default) {
        if (ddiPath.isNullOrBlank()) return@withContext samples
        val ddi = PlatformFile(ddiPath).readBytes()
        val phonemeTokens = Regex("(?:\\^?[a-zA-Z0-9@{}\\\\:;!?<>*~()\\-]+)")
            .findAll(ddi.decodeToString())
            .map { it.value }
            .filter { it.length <= 24 && it.any(Char::isLetter) }
            .distinct()
            .toList()
        samples.mapIndexed { index, sample ->
            val hints = phonemeTokens.drop(index % phonemeTokens.size.coerceAtLeast(1)).take(2)
            sample.copy(phonemes = hints)
        }
    }

    suspend fun exportAllSamples(ddbPath: String, outputDirectory: String, samples: List<SampleRecord>) = withContext(Dispatchers.Default) {
        val dir = PlatformFile(outputDirectory)
        dir.mkdirs()
        for (sample in samples) {
            exportSample(ddbPath, sample, dir.resolve("sample_%04d_%08x.wav".format(sample.index, sample.offset)).absolutePath)
        }
    }

    suspend fun replaceSample(ddbPath: String, sample: SampleRecord, wavPath: String, outputDdbPath: String) = withContext(Dispatchers.Default) {
        val source = PlatformFile(ddbPath).readBytes().copyOf()
        val wav = readWavPcm16(PlatformFile(wavPath).readBytes())
        require(wav.sampleRate == sample.sampleRate && wav.channels == sample.channels) {
            "替换采样的采样率/声道必须与原采样一致 (${sample.sampleRate} Hz, ${sample.channels} ch)"
        }
        require(wav.pcm.size == sample.pcmBytes) {
            "为保持 DDI 偏移有效，替换采样必须是等长 PCM（需要 ${sample.pcmBytes} bytes，实际 ${wav.pcm.size} bytes）"
        }
        val offset = sample.offset.toInt()
        wav.pcm.copyInto(source, offset + 18)
        PlatformFile(outputDdbPath).writeBytes(source)
    }

    /** Replaces only equal-byte-length UTF-8 tokens in a DDI, preserving all offsets. */
    suspend fun replacePhoneme(ddiPath: String, old: String, new: String, outputPath: String) = withContext(Dispatchers.Default) {
        require(old.isNotBlank() && old != new) { "发音不能为空且必须发生变化" }
        val source = PlatformFile(ddiPath).readBytes().copyOf()
        val oldBytes = old.encodeToByteArray()
        val newBytes = new.encodeToByteArray()
        require(oldBytes.size == newBytes.size) { "为保持二进制布局，旧/新发音必须拥有相同 UTF-8 字节长度" }
        var replaced = 0
        var cursor = 0
        while (cursor <= source.size - oldBytes.size) {
            val at = indexOf(source, oldBytes, cursor)
            if (at < 0) break
            newBytes.copyInto(source, at)
            cursor = at + newBytes.size
            replaced++
        }
        require(replaced > 0) { "在 DDI 中没有找到发音：$old" }
        PlatformFile(outputPath).writeBytes(source)
    }

    private fun wavHeader(rate: Int, channels: Int, pcmSize: Int): ByteArray {
        val out = ByteArray(44)
        fun put(pos: Int, value: Int) {
            out[pos] = (value and 0xff).toByte(); out[pos + 1] = ((value ushr 8) and 0xff).toByte()
            out[pos + 2] = ((value ushr 16) and 0xff).toByte(); out[pos + 3] = ((value ushr 24) and 0xff).toByte()
        }
        "RIFF".encodeToByteArray().copyInto(out, 0); put(4, 36 + pcmSize); "WAVE".encodeToByteArray().copyInto(out, 8)
        "fmt ".encodeToByteArray().copyInto(out, 12); put(16, 16); out[20] = 1; out[21] = 0
        out[22] = channels.toByte(); out[23] = 0; put(24, rate); put(28, rate * channels * 2)
        out[32] = (channels * 2).toByte(); out[34] = 16; "data".encodeToByteArray().copyInto(out, 36); put(40, pcmSize)
        return out
    }

    private data class Wav(val sampleRate: Int, val channels: Int, val pcm: ByteArray)

    private fun readWavPcm16(bytes: ByteArray): Wav {
        require(bytes.size >= 44 && bytes.copyOfRange(0, 4).decodeToString() == "RIFF" && bytes.copyOfRange(8, 12).decodeToString() == "WAVE") { "只支持标准 PCM WAV" }
        var cursor = 12; var rate = 0; var channels = 0; var bits = 0; var pcm = byteArrayOf()
        while (cursor + 8 <= bytes.size) {
            val id = bytes.copyOfRange(cursor, cursor + 4).decodeToString(); val size = readInt(bytes, cursor + 4)
            val end = (cursor + 8 + size).coerceAtMost(bytes.size)
            when (id) {
                "fmt " -> { channels = readShort(bytes, cursor + 10); rate = readInt(bytes, cursor + 12); bits = readShort(bytes, cursor + 22) }
                "data" -> pcm = bytes.copyOfRange(cursor + 8, end)
            }
            cursor = end + (size and 1)
        }
        require(rate > 0 && channels > 0 && bits == 16 && pcm.isNotEmpty()) { "需要 16-bit PCM WAV" }
        return Wav(rate, channels, pcm)
    }

    private fun readInt(bytes: ByteArray, at: Int): Int = (bytes[at].toInt() and 0xff) or ((bytes[at + 1].toInt() and 0xff) shl 8) or ((bytes[at + 2].toInt() and 0xff) shl 16) or ((bytes[at + 3].toInt() and 0xff) shl 24)
    private fun readShort(bytes: ByteArray, at: Int): Int = (bytes[at].toInt() and 0xff) or ((bytes[at + 1].toInt() and 0xff) shl 8)
    private fun indexOf(haystack: ByteArray, needle: ByteArray, from: Int): Int {
        if (needle.isEmpty()) return from
        for (i in from..haystack.size - needle.size) if (needle.indices.all { haystack[i + it] == needle[it] }) return i
        return -1
    }

    private companion object { val SND = "SND ".encodeToByteArray() }
}

