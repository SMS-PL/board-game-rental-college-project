package sms.pollub.gamerental.game.repository

import org.springframework.data.jpa.repository.JpaRepository
import sms.pollub.gamerental.game.Game
import sms.pollub.gamerental.game.enums.GameTag
import java.util.UUID

interface GameRepository : JpaRepository<Game, UUID> {
    fun findByTagsContaining(tag: GameTag): List<Game>
}

