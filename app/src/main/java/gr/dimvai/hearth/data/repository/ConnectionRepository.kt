package gr.dimvai.hearth.data.repository

import android.app.Application
import gr.dimvai.hearth.data.local.ConnectionDao
import gr.dimvai.hearth.data.model.Connection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ConnectionRepository(
    val application: Application,
    private val connectionDao: ConnectionDao
) {
    val allConnections: Flow<List<Connection>> = connectionDao.getAllConnections()

    suspend fun getAllConnectionsOnce(): List<Connection> {
        return allConnections.first()
    }

    suspend fun insertConnections(connections: List<Connection>) {
        connections.forEach { connectionDao.insertConnection(it) }
    }

    suspend fun getConnectionById(id: String): Connection? {
        return connectionDao.getConnectionById(id)
    }

    suspend fun insert(connection: Connection) {
        connectionDao.insertConnection(connection)
    }

    suspend fun update(connection: Connection) {
        connectionDao.updateConnection(connection)
    }

    suspend fun delete(connection: Connection) {
        connectionDao.deleteConnection(connection)
    }
}
