package gr.dimvai.hearth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "connections")
data class Connection(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val frequencyDays: Int,
    val lastCommunicationDate: LocalDate? = null,
    val scheduledNextDate: LocalDate? = null,
    val createdAt: LocalDate = LocalDate.now()
) {
    /**
     * Business Logic for calculating next communication date.
     * 1. If scheduledNextDate is set, it overrides everything.
     * 2. Otherwise, add frequencyDays to lastCommunicationDate.
     * 3. If lastCommunicationDate is null, add frequencyDays to createdAt.
     */
    fun calculateNextCommunicationDate(): LocalDate {
        scheduledNextDate?.let { return it }
        
        val baseDate = lastCommunicationDate ?: createdAt
        return baseDate.plusDays(frequencyDays.toLong())
    }

    val isOverdue: Boolean
        get() = calculateNextCommunicationDate().isBefore(LocalDate.now())

    val isToday: Boolean
        get() = calculateNextCommunicationDate().isEqual(LocalDate.now())
}
