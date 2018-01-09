package co.vandierendonck.trionfinfc.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "game")
class Game @Ignore constructor(@ColumnInfo(name = "name") var name: String): Identifiable() {
    constructor() : this("Undefined")

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true) override var id: Long = 0

    @ColumnInfo(name = "score1")
    var score1: Int = 0
    @ColumnInfo(name = "score2")
    var score2: Int = 0

    @ColumnInfo(name = "team1_name")
    var team1Name: String = "Team 1"
    @ColumnInfo(name = "team2_name")
    var team2Name: String = "Team 2"
}