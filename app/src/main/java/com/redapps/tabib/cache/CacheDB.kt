package com.redapps.tabib.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.redapps.tabib.model.Treatment

@Database(entities = arrayOf(Treatment::class), version = 1, exportSchema = false)
@TypeConverters(Converters::class)
public abstract class CacheDB : RoomDatabase() {

    abstract fun treatmentDAO(): TreatmentDAO

    companion object {
        @Volatile
        private var INSTANCE: CacheDB? = null

        fun getDatabase(context: Context): CacheDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CacheDB::class.java,
                    "cache"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
