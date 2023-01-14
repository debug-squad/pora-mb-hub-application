package si.feri.timpra.mbhubapp.data

class CaptureSettings(
    val enabled: Boolean,
    val duration: Long,
    val interval: Long,
    val hideDuration: Boolean = false
) :
    java.io.Serializable {
    fun formatInterval(): String = format(interval)
    fun formatDuration(): String = format(duration)

    companion object {
        val DEFAULT_SOUND = CaptureSettings(false, 5000L, 1000L * 60L * 10L)
        val DEFAULT_ACCELEROMETER = CaptureSettings(false, 5000L, 1000L * 60L * 10L)
        val DEFAULT_IMAGE = CaptureSettings(false, 5000L, 1000L * 60L * 10L)


        val DEFAULT_SIM_SOUND = CaptureSettings
        val DEFAULT_SIM_ACCELEROMETER = CaptureSettings(false, 5000L, 1000L * 60L * 10L)
        val DEFAULT_SIM_IMAGE = CaptureSettings(false, 5000L, 1000L * 60L * 10L)

        fun parse(v: String): Long? {
            try {
                return v.removeSuffix("ms").toLong()
            } catch (e: java.lang.NumberFormatException) {
            }
            try {
                return v.removeSuffix("s").trim().toLong() * 1000L
            } catch (e: java.lang.NumberFormatException) {
            }
            try {
                return v.removeSuffix("sec").trim().toLong() * 1000L
            } catch (e: java.lang.NumberFormatException) {
            }
            try {
                return v.removeSuffix("min").trim().toLong() * 1000L * 60L
            } catch (e: java.lang.NumberFormatException) {
            }
            try {
                return v.removeSuffix("m").trim().toLong() * 1000L * 60L
            } catch (e: java.lang.NumberFormatException) {
            }
            try {
                return v.removeSuffix("h").trim().toLong() * 1000L * 60L * 60L
            } catch (e: java.lang.NumberFormatException) {
            }
            return null
        }

        fun format(v: Long): String {
            return if (v % (1000 * 60 * 60L) == 0L) {
                "${v / (1000 * 60 * 60L)} h"
            } else if (v % (1000 * 60) == 0L) {
                "${v / (1000 * 60)} min"
            } else if (v % (1000) == 0L) {
                "${v / (1000)} sec"
            } else {
                "$v ms"
            }
        }
    }


    fun setEnabled(enabled: Boolean) =
        CaptureSettings(enabled, duration = this.duration, interval = this.interval)
}