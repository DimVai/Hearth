package gr.dimvai.hearth

import android.app.Application
import gr.dimvai.hearth.data.local.HearthDatabase
import gr.dimvai.hearth.data.repository.ConnectionRepository

class HearthApplication : Application() {
    val database by lazy { HearthDatabase.getDatabase(this) }
    val repository by lazy { ConnectionRepository(database.connectionDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
