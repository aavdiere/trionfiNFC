package co.vandierendonck.trionfinfc.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import co.vandierendonck.trionfinfc.db.AppDatabase
import co.vandierendonck.trionfinfc.db.Game
import co.vandierendonck.trionfinfc.db.Trick
import org.jetbrains.anko.doAsync

class TrickListViewModel(application: Application, gameId: Long): AndroidViewModel(application) {
    private val appDatabase = AppDatabase.getDatabase(application)
    private val trickList = appDatabase.trickDao().getTricksFromGame(gameId)
    private val game = appDatabase.gameDao().findGameById(gameId)

    fun getTrickList(): LiveData<List<Trick>> { return trickList }

    fun deleteItem(trick: Trick) {
        doAsync { appDatabase.trickDao().deleteTrick(trick) }
    }
    fun insertItem(trick: Trick) {
        doAsync { appDatabase.trickDao().insertTrick(trick) }
    }

    fun getGame(): LiveData<Game> { return game }

    fun updateScore(game: Game, score1: Int, score2: Int) {
        game.score1 = score1
        game.score2 = score2
        doAsync { appDatabase.gameDao().updateGame(game) }
    }

    fun updateTeamName(game: Game, teamName: String, teamId: Int) {
        if (teamId == 0) game.team1Name = teamName
        else game.team2Name = teamName
        doAsync { appDatabase.gameDao().updateGame(game) }
    }

    class TrickListModelFactory(private val app: Application, private val id: Long) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T: ViewModel?> create(modelClass: Class<T>): T {
            return TrickListViewModel(app, id) as T
        }
    }
}