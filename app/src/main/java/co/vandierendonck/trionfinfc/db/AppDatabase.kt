package co.vandierendonck.trionfinfc.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

@Database(entities = [Game::class, Trick::class, Card::class], version = 1, exportSchema = false)
@TypeConverters(RankConverter::class, SuitConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun cardDao()  : CardDao
    abstract fun trickDao() : TrickDao
    abstract fun gameDao()  : GameDao

    companion object {
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context.applicationContext,
                                                AppDatabase::class.java,
                                                "trionfi_db"
                                                ).build()
            }
            return instance!!
        }

        fun destroyInstance() {
            instance = null
        }
    }
}