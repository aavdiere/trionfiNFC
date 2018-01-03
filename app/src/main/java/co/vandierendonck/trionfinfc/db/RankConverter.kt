package co.vandierendonck.trionfinfc.db

import android.arch.persistence.room.TypeConverter

class RankConverter {
    @TypeConverter
    fun getRank(value: Int): Rank {
        return Rank.values().firstOrNull { it.getValue() == value } ?: Rank.NONE
    }

    @TypeConverter
    fun getValue(rank: Rank): Int {
        return rank.getValue()
    }
}