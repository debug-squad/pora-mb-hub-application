package si.feri.timpra.mbhubapp.data

import si.feri.timpra.mbhubapp.R

class ImgSettings(
    val enabled: Boolean,
    val interval: Long,
    val file: Int = R.raw.full
) :
    java.io.Serializable {
    fun formatInterval(): String = CaptureSettings.format(interval)
    fun setEnabled(enabled: Boolean) = ImgSettings(enabled, interval = this.interval, file = file)

    fun text() = when (file) {
        R.raw.full -> "full"
        R.raw.empty -> "empty"
        else -> "unknown"
    }

    companion object {
        val DEFAULT: ImgSettings = ImgSettings(false, 5000L)
    }
}