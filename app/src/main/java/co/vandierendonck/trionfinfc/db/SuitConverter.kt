package co.vandierendonck.trionfinfc.db

import android.arch.persistence.room.TypeConverter

class SuitConverter {
    @TypeConverter
    fun getSuit(value: Int): Suit {
        return Suit.values().firstOrNull { it.getValue() == value } ?: Suit.UNDEFINED
    }

    @TypeConverter
    fun getValue(suit: Suit): Int {
        return suit.getValue()
    }
}