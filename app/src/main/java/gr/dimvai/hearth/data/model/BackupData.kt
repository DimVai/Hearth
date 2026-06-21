package gr.dimvai.hearth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val connections: List<Connection>
)
