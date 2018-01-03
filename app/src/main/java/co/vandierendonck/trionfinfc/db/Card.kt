package co.vandierendonck.trionfinfc.db

import android.arch.persistence.room.*

@Entity(
        tableName = "card",
        indices = [
            Index(
                    value = ["trick_id"],
                    name = "idx_trick"
            )
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Trick::class,
                    parentColumns = ["id"],
                    childColumns = ["trick_id"],
                    onUpdate = ForeignKey.CASCADE,
                    onDelete = ForeignKey.CASCADE
            )
        ]
)
class Card @Ignore constructor(@ColumnInfo(name = "trick_id") var trickId: Long,
                               @ColumnInfo(name = "number") var number: Int,
                               @ColumnInfo(name = "player_id") var playerId: Int): Identifiable() {
    constructor() : this(0,0, 0)

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) override var id: Long = 0

    @ColumnInfo(name = "rank")
    @TypeConverters(RankConverter::class)
    var rank: Rank = Rank.NONE

    @ColumnInfo(name = "suit")
    @TypeConverters(SuitConverter::class)
    var suit: Suit = Suit.UNDEFINED

    @Ignore
    fun getScore(): Int {
        return when(rank) {

            Rank.JACK -> 1
            Rank.QUEEN -> 2
            Rank.KING -> 3
            Rank.ACE -> 4
            Rank.MANILLE -> 5
            else -> 0
        }
    }
}