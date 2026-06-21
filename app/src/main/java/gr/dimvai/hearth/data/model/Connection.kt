package gr.dimvai.hearth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.util.UUID

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

@Entity(tableName = "connections")
@Serializable
data class Connection(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val frequencyDays: Int,
    @Serializable(with = LocalDateSerializer::class)
    val lastCommunicationDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    val scheduledNextDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
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

    fun getDaysUntilNext(): Long {
        val next = calculateNextCommunicationDate()
        val today = LocalDate.now()
        return java.time.temporal.ChronoUnit.DAYS.between(today, next)
    }

    fun getFrequencyLabel(): String {
        return when (frequencyDays) {
            1 -> "Κάθε μέρα"
            2 -> "Κάθε 2 μέρες"
            7 -> "Κάθε εβδομάδα"
            14 -> "Κάθε 2 εβδομάδες"
            30 -> "Κάθε μήνα"
            60 -> "Κάθε 2 μήνες"
            else -> "Κάθε $frequencyDays μέρες"
        }
    }
}
