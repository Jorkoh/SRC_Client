package ui.utils

fun String.toFlagEmoji(): String {
    val countryCode = squashIndependenceMovements().toUpperCase()
    require(countryCode.length == 2 && countryCode[0].isLetter() && countryCode[1].isLetter())

    val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

private fun String.squashIndependenceMovements() = split('/')[0]