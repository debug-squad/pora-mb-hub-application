package si.feri.timpra.mbhubapp.data

import si.feri.timpra.mbhubapp.R

class SoundSettings(
    val enabled: Boolean,
    val interval: Long,
    val file: Int = R.raw.quiet
) :
    java.io.Serializable {
    fun formatInterval(): String = CaptureSettings.format(interval)
    fun setEnabled(enabled: Boolean) = SoundSettings(enabled, interval = this.interval, file = file)

    fun text() = when (file) {
        R.raw.quiet -> "quiet"
        R.raw.noisy -> "noisy"
        else -> "unknown"
    }

    companion object {
        val DEFAULT: SoundSettings = SoundSettings(false, 5000L)
    }
}