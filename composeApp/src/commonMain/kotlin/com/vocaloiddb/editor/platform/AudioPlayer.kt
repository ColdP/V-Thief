package btm.m.vthief.platform

expect object AudioPlayer {
    fun playWav(bytes: ByteArray)
    fun stop()
}
