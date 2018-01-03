package co.vandierendonck.trionfinfc.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface GameDao {
    @Query("select * from game")
    fun getAllGames(): LiveData<List<Game>>

    @Query("select * from game where id = :id")
    fun findGameById(id: Long): LiveData<Game>

    @Insert(onConflict = REPLACE)
    fun insertGame(game: Game)

    @Update
    fun updateGame(game: Game)

    @Delete
    fun deleteGame(game: Game)
}