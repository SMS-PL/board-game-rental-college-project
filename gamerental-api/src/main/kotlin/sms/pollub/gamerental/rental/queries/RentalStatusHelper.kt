package sms.pollub.gamerental.rental.queries

import sms.pollub.gamerental.rental.enums.RentalStatus
import java.time.LocalDate

fun computeEffectiveStatus(status: RentalStatus, dueTo: LocalDate): RentalStatus =
    if (status == RentalStatus.ACTIVE && dueTo.isBefore(LocalDate.now())) RentalStatus.OVERDUE else status

