package sms.pollub.gamerental.game

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import sms.pollub.gamerental.shared.BaseEntity

@Entity
@Table(name = "games")
@Access(AccessType.FIELD)
class Game(
    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var description: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_tags", joinColumns = [JoinColumn(name = "game_id")])
    @Column(name = "tag")
    @Enumerated(EnumType.STRING)
    var tags: MutableSet<GameTag> = mutableSetOf(),

    @Column(nullable = false)
    var totalCopies: Int = 0
) : BaseEntity()

