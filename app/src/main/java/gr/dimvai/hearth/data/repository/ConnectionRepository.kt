package gr.dimvai.hearth.data.repository

import gr.dimvai.hearth.data.local.ConnectionDao
import gr.dimvai.hearth.data.model.Connection
import kotlinx.coroutines.flow.Flow

class ConnectionRepository(private val connectionDao: ConnectionDao) {
    val allConnections: Flow<List<Connection>> = connectionDao.getAllConnections()

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
