package btm.m.vthief.platform

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

actual object AudioPlayer {
    private var clip: Clip? = null

    actual fun playWav(bytes: ByteArray) {
        stop()
        val next = AudioSystem.getClip()
        next.open(AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes)))
        clip = next
        next.start()
    }

    actual fun stop() {
        clip?.stop()
        clip?.close()
        clip = null
    }
}
