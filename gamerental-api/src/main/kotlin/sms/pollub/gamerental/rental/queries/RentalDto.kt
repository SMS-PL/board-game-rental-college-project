package sms.pollub.gamerental.rental.queries

import sms.pollub.gamerental.game.enums.CopyCondition
import sms.pollub.gamerental.rental.enums.RentalStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class RentalDto(
    val id: UUID,
    val gameCopyId: UUID,
    val gameTitle: String,
    val copyNumber: Int,
    val clientId: UUID,
    val clientName: String,
    val rentedFrom: LocalDate,
    val dueTo: LocalDate,
    val returnedAt: LocalDate?,
    val conditionOnReturn: CopyCondition?,
    val status: RentalStatus,
    val notes: String?,
    val createdAt: LocalDateTime
)

