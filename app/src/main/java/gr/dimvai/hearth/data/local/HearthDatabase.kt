package gr.dimvai.hearth.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import gr.dimvai.hearth.data.model.Connection

@Database(entities = [Connection::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HearthDatabase : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao

    companion object {
        @Volatile
        private var INSTANCE: HearthDatabase? = null

        fun getDatabase(context: Context): HearthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HearthDatabase::class.java,
                    "hearth_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
