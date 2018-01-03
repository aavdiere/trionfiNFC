package co.vandierendonck.trionfinfc.db

enum class Rank(private var value: Int) {
    SEVEN(0x00),
    EIGHT(0x01),
    NINE(0x02),
    JACK(0x03),
    QUEEN(0x04),
    KING(0x05),
    ACE(0x06),
    MANILLE(0x07),
    NONE(-1);

    fun getValue(): Int {
        return value
    }
}
