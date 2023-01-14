package si.feri.timpra.mbhubapp.data

import si.feri.timpra.mbhubapp.R

class AccSettings(
    val enabled: Boolean,
    val interval: Long,
    val file: Int = R.raw.calm
) :
    java.io.Serializable {
    fun formatInterval(): String = CaptureSettings.format(interval)
    fun setEnabled(enabled: Boolean) =
        AccSettings(enabled, interval = this.interval, file = file)


    fun text() = when (file) {
        R.raw.calm -> "calm"
        R.raw.active -> "active"
        else -> "unknown"
    }

    companion object {
        val DEFAULT: AccSettings = AccSettings(false, 5000L)
    }
}