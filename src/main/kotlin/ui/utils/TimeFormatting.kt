package ui.utils

import kotlin.math.roundToInt
import kotlin.time.Duration

fun Duration.toSRCString() = toComponents { hours, minutes, seconds, nanoseconds ->
    // TODO added rounding because of some floating point weirdness with Duration, should be fixed properly
    val milliseconds = (nanoseconds / 1000000.0).roundToInt()

    StringBuilder()
        .append(if (hours != 0) "${hours}h " else "")
        .append(if (minutes != 0) "${minutes}m " else "")
        .append(if (seconds != 0) "${seconds}s " else "")
        .append(if (milliseconds != 0) "${milliseconds}ms" else "")
        .toString()
}