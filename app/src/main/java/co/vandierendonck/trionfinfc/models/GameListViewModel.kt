package co.vandierendonck.trionfinfc.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import co.vandierendonck.trionfinfc.db.AppDatabase
import co.vandierendonck.trionfinfc.db.Game
import org.jetbrains.anko.doAsync

class GameListViewModel(application: Application): AndroidViewModel(application) {
    private val appDatabase = AppDatabase.getDatabase(application)
    private val gameList = appDatabase.gameDao().getAllGames()

    fun getGameList(): LiveData<List<Game>> { return gameList }

    fun changeName(game: Game, newTitle: String) {
        game.name = newTitle
        doAsync { appDatabase.gameDao().updateGame(game) }
    }
    fun deleteItem(game: Game) {
        doAsync { appDatabase.gameDao().deleteGame(game) }
    }
    fun insertItem(game: Game) {
        doAsync { appDatabase.gameDao().insertGame(game) }
    }
}