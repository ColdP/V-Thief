package btm.m.vthief.core

data class VoicebankFile(
    val name: String,
    val path: String,
    val type: FileType,
    val sizeBytes: Long,
)

enum class FileType { DDI, DDB, VVD, UNKNOWN }

data class SampleRecord(
    val index: Int,
    val offset: Long,
    val length: Int,
    val sampleRate: Int,
    val channels: Int,
    val pcmBytes: Int,
    val phonemes: List<String> = emptyList(),
) {
    val durationSeconds: Double
        get() = if (sampleRate > 0 && channels > 0) pcmBytes.toDouble() / sampleRate / 2.0 / channels else 0.0
}

data class DdiSummary(
    val sizeBytes: Long,
    val sections: List<String>,
    val phonemes: List<String>,
    val estimatedEntries: Int,
)

data class VvdMetadata(
    val compId: String,
    val voiceName: String,
    val format: String,
    val voiceType: String,
    val parameters: Map<String, String>,
)

data class VoicebankProfile(
    val name: String,
    val language: String,
    val phonemeCount: Int,
    val sampleCount: Int,
    val bytes: Long,
)

data class XsyProfile(
    val payload: String,
    val groupName: String,
    val members: List<String>,
)

data class KeyBundle(
    val compId: String,
    val integrityKey: String,
    val xsy: XsyProfile,
    val trialExpiresOn: String = "",
)

data class TrialPolicy(
    val enabled: Boolean,
    val expiresOn: String,
)

