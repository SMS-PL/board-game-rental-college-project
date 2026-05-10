package sms.pollub.gamerental.game

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
import sms.pollub.gamerental.game.enums.CopyCondition
import sms.pollub.gamerental.shared.BaseEntity

@Entity
@Table(name = "game_copies")
@Access(AccessType.FIELD)
class GameCopy(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    var game: Game,

    @Column(nullable = false)
    var copyNumber: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var condition: CopyCondition = CopyCondition.NEW,

    @Column(name = "is_available", nullable = false)
    var isAvailable: Boolean = true
) : BaseEntity()
