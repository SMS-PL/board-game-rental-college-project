package sms.pollub.gamerental.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import sms.pollub.gamerental.auth.AppUser
import java.util.UUID

interface AppUserRepository : JpaRepository<AppUser, UUID> {
    fun findByLogin(login: String): AppUser?
    fun existsByLogin(login: String): Boolean
}

