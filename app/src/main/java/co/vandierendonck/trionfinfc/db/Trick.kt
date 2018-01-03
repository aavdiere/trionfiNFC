package co.vandierendonck.trionfinfc.db

import android.arch.persistence.room.*

@Entity(
        tableName = "trick",
        indices = [
            Index(
                    value = ["game_id"],
                    name = "idx_game"
            )
        ],
        foreignKeys = [
            ForeignKey(
                    entity = Game::class,
                    parentColumns = ["id"],
                    childColumns = ["game_id"],
                    onUpdate = ForeignKey.CASCADE,
                    onDelete = ForeignKey.CASCADE
            )
        ]
)
class Trick @Ignore constructor(@ColumnInfo(name = "game_id")  var gameId: Long,
                                @ColumnInfo(name = "number") var number: Int): Identifiable() {
    constructor() : this(0, 0)

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) override var id: Long = 0

    @ColumnInfo(name = "multiplier")
    var multiplier: Int = 1

    @ColumnInfo(name = "score1")
    var score1: Int = 0

    @ColumnInfo(name = "score2")
    var score2: Int = 0

    @ColumnInfo(name = "suit")
    @TypeConverters(SuitConverter::class)
    var trump: Suit = Suit.UNDEFINED

    @ColumnInfo(name = "winner")
    var winner: Int = -1
}