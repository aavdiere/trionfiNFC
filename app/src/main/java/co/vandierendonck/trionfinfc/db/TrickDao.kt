package co.vandierendonck.trionfinfc.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface TrickDao {
    @Query("select * from trick where game_id = :gameId order by number")
    fun getTricksFromGame(gameId: Long): LiveData<List<Trick>>

    @Query("select * from trick where id = :id")
    fun findTrickById(id: Long): LiveData<Trick>

    @Insert(onConflict = REPLACE)
    fun insertTrick(trick: Trick)

    @Update
    fun updateTrick(trick: Trick)

    @Delete
    fun deleteTrick(trick: Trick)
}