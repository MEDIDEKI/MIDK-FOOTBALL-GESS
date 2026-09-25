package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlayerUsageEntity::class,
        GameRecordEntity::class,
        GameStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FootballGuessDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: FootballGuessDatabase? = null

        fun getInstance(context: Context): FootballGuessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FootballGuessDatabase::class.java,
                    "football_guess.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
