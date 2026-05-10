package sms.pollub.gamerental.game.repository

import org.springframework.data.jpa.repository.JpaRepository
import sms.pollub.gamerental.game.GameCopy
import java.util.UUID

interface GameCopyRepository : JpaRepository<GameCopy, UUID> {
    fun findByGameId(gameId: UUID): List<GameCopy>
    fun findByGameIdAndIsAvailableTrue(gameId: UUID): List<GameCopy>
    fun countByGameIdAndIsAvailableTrue(gameId: UUID): Int
    fun countByGameIdAndIsAvailableFalse(gameId: UUID): Int
    fun findByGameIdOrderByCopyNumberDesc(gameId: UUID): List<GameCopy>
}

