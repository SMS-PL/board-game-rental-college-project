package sms.pollub.gamerental.shared

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime
import java.util.UUID

@MappedSuperclass
@Access(AccessType.FIELD)
abstract class BaseEntity(
    @Id
    val id: UUID = UUID.randomUUID(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

