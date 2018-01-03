package co.vandierendonck.trionfinfc.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import co.vandierendonck.trionfinfc.db.AppDatabase
import co.vandierendonck.trionfinfc.db.Card
import co.vandierendonck.trionfinfc.db.Suit
import co.vandierendonck.trionfinfc.db.Trick
import org.jetbrains.anko.doAsync

class HandViewModel(application: Application, trickId: Long): AndroidViewModel(application) {
    private val appDatabase = AppDatabase.getDatabase(application)
    private val cardList = appDatabase.cardDao().getCardsFromTrick(trickId)
    private val trick = appDatabase.trickDao().findTrickById(trickId)

    fun getCardList(): LiveData<List<Card>> { return cardList }

    fun deleteItem(card: Card) {
        doAsync { appDatabase.cardDao().deleteCard(card) }
    }

    fun insertItem(card: Card) {
        doAsync { appDatabase.cardDao().insertCard(card) }
    }

    fun updateItem(card: Card) {
        doAsync { appDatabase.cardDao().updateCard(card) }
    }

    fun updateScore(trick: Trick, score1: Int, score2: Int) {
        trick.score1 = score1
        trick.score2 = score2
        doAsync { appDatabase.trickDao().updateTrick(trick) }
    }

    fun updateTrump(trump: Suit) {
        val data = trick.value
        if (data != null) {
            data.trump = trump
            doAsync { appDatabase.trickDao().updateTrick(data) }
        }
    }

    fun incrementMultiplier() {
        val data = trick.value
        if (data != null) {
            data.multiplier *= 2
            if (data.multiplier > 4)
                data.multiplier = 1
            doAsync { appDatabase.trickDao().updateTrick(data) }
        }
    }

    fun getTrick(): LiveData<Trick> { return trick }

    class HandListModelFactory(private val app: Application, private val id: Long) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HandViewModel(app, id) as T
        }
    }
}