package co.vandierendonck.trionfinfc.db

enum class Suit(private var value: Int) {
    SPADES(0x00),
    HEARTS(0x01),
    CLUBS(0x02),
    DIAMONDS(0x03),
    NONE(0x04),
    UNDEFINED(0x05);

    fun getValue(): Int {
        return value
    }
}