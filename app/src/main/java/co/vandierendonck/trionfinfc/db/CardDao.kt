package co.vandierendonck.trionfinfc.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface CardDao {
    @Query("select * from card where trick_id = :trickId order by number")
    fun getCardsFromTrick(trickId: Long): LiveData<List<Card>>

    @Query("select * from card where id = :id")
    fun findCardById(id: Long): LiveData<Card>

    @Insert(onConflict = REPLACE)
    fun insertCard(card: Card)

    @Update
    fun updateCard(card: Card)

    @Delete
    fun deleteCard(card: Card)
}