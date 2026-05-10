package sms.pollub.gamerental.rental.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import sms.pollub.gamerental.rental.Rental
import sms.pollub.gamerental.rental.enums.RentalStatus
import java.time.LocalDate
import java.util.UUID

interface RentalRepository : JpaRepository<Rental, UUID> {
    fun findByClientId(clientId: UUID): List<Rental>
    fun findByGameCopyGameId(gameId: UUID): List<Rental>
    fun findByStatus(status: RentalStatus): List<Rental>
    fun countByClientIdAndStatus(clientId: UUID, status: RentalStatus): Long

    @Query("SELECT r FROM Rental r WHERE r.status = 'ACTIVE' AND r.dueTo < :today")
    fun findOverdue(@Param("today") today: LocalDate): List<Rental>

    @Query("""
        SELECT r FROM Rental r 
        WHERE (:clientId IS NULL OR r.client.id = :clientId)
          AND (:gameId IS NULL OR r.gameCopy.game.id = :gameId)
    """)
    fun findByFilters(
        @Param("clientId") clientId: UUID?,
        @Param("gameId") gameId: UUID?
    ): List<Rental>
}

