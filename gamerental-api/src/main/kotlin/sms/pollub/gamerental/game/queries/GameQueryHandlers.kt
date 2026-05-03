package sms.pollub.gamerental.game.queries

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import sms.pollub.gamerental.game.GameCopy
import sms.pollub.gamerental.game.GameCopyRepository
import sms.pollub.gamerental.game.GameRepository
import java.util.UUID

@Component
class GetGamesQueryHandler(
    private val gameRepository: GameRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional(readOnly = true)
    fun handle(query: GetGamesQuery): List<GameDto> {
        val games = if (query.tag != null) {
            gameRepository.findByTagsContaining(query.tag)
        } else {
            gameRepository.findAll()
        }

        val sorted = when (query.sortBy.lowercase()) {
            "date", "createdat" -> games.sortedBy { it.createdAt }
            else -> games.sortedBy { it.title.lowercase() }
        }

        return sorted.map { game ->
            val available = gameCopyRepository.countByGameIdAndIsAvailableTrue(game.id)
            GameDto(
                id = game.id,
                title = game.title,
                description = game.description,
                tags = game.tags.toSet(),
                totalCopies = game.totalCopies,
                availableCopies = available,
                createdAt = game.createdAt
            )
        }
    }
}

@Component
class GetGameQueryHandler(
    private val gameRepository: GameRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional(readOnly = true)
    fun handle(gameId: UUID): GameDetailDto {
        val game = gameRepository.findById(gameId)
            .orElseThrow { NoSuchElementException("Game with id '$gameId' not found") }

        val copies = gameCopyRepository.findByGameId(game.id)
            .sortedBy { it.copyNumber }
            .map { copy ->
                GameCopyDto(
                    id = copy.id,
                    copyNumber = copy.copyNumber,
                    condition = copy.condition,
                    isAvailable = copy.isAvailable
                )
            }

        return GameDetailDto(
            id = game.id,
            title = game.title,
            description = game.description,
            tags = game.tags.toSet(),
            totalCopies = game.totalCopies,
            copies = copies,
            createdAt = game.createdAt
        )
    }
}

@Component
class GetAvailableCopiesQueryHandler(
    private val gameRepository: GameRepository,
    private val gameCopyRepository: GameCopyRepository
) {
    @Transactional(readOnly = true)
    fun handle(gameId: UUID): List<GameCopyDto> {
        if (!gameRepository.existsById(gameId)) {
            throw NoSuchElementException("Game with id '$gameId' not found")
        }
        return gameCopyRepository.findByGameIdAndIsAvailableTrue(gameId)
            .sortedBy { it.copyNumber }
            .map { copy ->
                GameCopyDto(
                    id = copy.id,
                    copyNumber = copy.copyNumber,
                    condition = copy.condition,
                    isAvailable = copy.isAvailable
                )
            }
    }
}

