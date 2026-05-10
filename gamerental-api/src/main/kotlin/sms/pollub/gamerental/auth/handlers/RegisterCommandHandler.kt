package sms.pollub.gamerental.auth.handlers

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import sms.pollub.gamerental.auth.AppUser
import sms.pollub.gamerental.auth.repository.AppUserRepository
import sms.pollub.gamerental.auth.commands.RegisterCommand

@Component
class RegisterCommandHandler(
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun handle(command: RegisterCommand) {
        if (appUserRepository.existsByLogin(command.username)) {
            throw IllegalStateException("Username '${command.username}' is already taken")
        }
        val user = AppUser(
            login = command.username,
            passwordHash = passwordEncoder.encode(command.password)!!
        )
        appUserRepository.save(user)
    }
}

