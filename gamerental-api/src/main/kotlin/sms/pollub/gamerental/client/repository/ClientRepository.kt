package sms.pollub.gamerental.client.repository

import org.springframework.data.jpa.repository.JpaRepository
import sms.pollub.gamerental.client.Client
import java.util.UUID

interface ClientRepository : JpaRepository<Client, UUID> {
    fun findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        firstName: String,
        lastName: String
    ): List<Client>
}

