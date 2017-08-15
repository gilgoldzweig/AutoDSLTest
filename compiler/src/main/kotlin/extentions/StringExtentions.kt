package extentions

/**
 * Created by gilgoldzweig on 13/08/2017.
 */
fun String.toTitle() = changeCharCase(0, true)
fun String.changeCharCase(charPosition: Int, upperCase: Boolean? = null): String {
    if (isEmpty() || charPosition !in 0 until length) {
        return ""
    } else {
        return when(upperCase) {
            null -> this
            true -> this.replaceFirst(this[charPosition], this[charPosition].toUpperCase())
            false -> this.replaceFirst(this[charPosition], this[charPosition].toLowerCase())
        }
    }
}

operator fun CharSequence.get(indexOfChar: Char) = this.indexOf(indexOfChar)
operator fun String.get(indexOfChar: Char) = this.indexOf(indexOfChar)
