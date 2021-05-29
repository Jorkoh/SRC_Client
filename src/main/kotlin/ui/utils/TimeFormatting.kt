package ui.utils

import kotlin.time.Duration

fun Duration.toSRCString() = toComponents { hours, minutes, seconds, nanoseconds ->
    val milliseconds = nanoseconds / 1_000_000

    if (hours == 0 && minutes == 0 && seconds == 0 && nanoseconds == 0) {
        null
    } else {
        buildList {
            if (hours != 0) add("${hours}h")
            if (minutes != 0) add("${minutes}m")
            if (seconds != 0) add("${seconds}s")
            if (milliseconds != 0) add("${milliseconds}ms")
        }.joinToString(separator = " ")
    }
}
