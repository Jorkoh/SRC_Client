package ui.utils

import kotlin.math.roundToInt
import kotlin.time.Duration

fun Duration.toSRCString() = toComponents { hours, minutes, seconds, nanoseconds ->
    // TODO added rounding because of some floating point weirdness with Duration, should be fixed properly
    val milliseconds = (nanoseconds / 1000000.0).roundToInt()

    if (hours == 0 && minutes == 0 && seconds == 0 && nanoseconds == 0) {
        "-"
    } else {
        buildList {
            if (hours != 0) add("${hours}h")
            if (minutes != 0) add("${minutes}m")
            if (seconds != 0) add("${seconds}s")
            if (milliseconds != 0) add("${milliseconds}ms")
        }.joinToString(separator = " ")
    }
}
