package sms.pollub.gamerental.game

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface GameCopyRepository : JpaRepository<GameCopy, UUID> {
    fun findByGameId(gameId: UUID): List<GameCopy>
    fun findByGameIdAndIsAvailableTrue(gameId: UUID): List<GameCopy>
    fun countByGameIdAndIsAvailableTrue(gameId: UUID): Int
    fun countByGameIdAndIsAvailableFalse(gameId: UUID): Int
    fun findByGameIdOrderByCopyNumberDesc(gameId: UUID): List<GameCopy>
}

