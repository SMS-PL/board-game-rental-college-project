package sms.pollub.gamerental.rental

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import sms.pollub.gamerental.client.Client
import sms.pollub.gamerental.game.enums.CopyCondition
import sms.pollub.gamerental.game.GameCopy
import sms.pollub.gamerental.rental.enums.RentalStatus
import sms.pollub.gamerental.shared.BaseEntity
import java.time.LocalDate

@Entity
@Table(name = "rentals")
@Access(AccessType.FIELD)
class Rental(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_copy_id", nullable = false)
    var gameCopy: GameCopy,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    var client: Client,

    @Column(nullable = false)
    var rentedFrom: LocalDate,

    @Column(nullable = false)
    var dueTo: LocalDate,

    var returnedAt: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    var conditionOnReturn: CopyCondition? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RentalStatus = RentalStatus.ACTIVE,

    @Column(columnDefinition = "TEXT")
    var notes: String? = null
) : BaseEntity()
