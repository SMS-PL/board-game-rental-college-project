package sms.pollub.gamerental.auth.handlers

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import sms.pollub.gamerental.auth.repository.AppUserRepository
import sms.pollub.gamerental.auth.service.JwtService
import sms.pollub.gamerental.auth.commands.LoginCommand
import sms.pollub.gamerental.auth.commands.TokenResponse

@Component
class LoginCommandHandler(
    private val appUserRepository: AppUserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {
    fun handle(command: LoginCommand): TokenResponse {
        val user = appUserRepository.findByLogin(command.username)
            ?: throw IllegalArgumentException("Invalid username or password")

        if (!passwordEncoder.matches(command.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid username or password")
        }

        return TokenResponse(jwtService.generateToken(user.login))
    }
}

