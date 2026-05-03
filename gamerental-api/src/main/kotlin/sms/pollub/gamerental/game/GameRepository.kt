package sms.pollub.gamerental.game

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GameRepository : JpaRepository<Game, UUID> {
    fun findByTagsContaining(tag: GameTag): List<Game>
}

