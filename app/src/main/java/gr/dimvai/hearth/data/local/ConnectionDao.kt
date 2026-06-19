package gr.dimvai.hearth.data.local

import androidx.room.*
import gr.dimvai.hearth.data.model.Connection
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Query("SELECT * FROM connections")
    fun getAllConnections(): Flow<List<Connection>>

    @Query("SELECT * FROM connections WHERE id = :id")
    suspend fun getConnectionById(id: String): Connection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: Connection)

    @Update
    suspend fun updateConnection(connection: Connection)

    @Delete
    suspend fun deleteConnection(connection: Connection)
}
