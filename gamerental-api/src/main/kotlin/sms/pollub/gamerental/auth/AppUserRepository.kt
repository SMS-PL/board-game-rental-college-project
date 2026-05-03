package sms.pollub.gamerental.auth

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AppUserRepository : JpaRepository<AppUser, UUID> {
    fun findByLogin(login: String): AppUser?
    fun existsByLogin(login: String): Boolean
}
