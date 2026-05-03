package sms.pollub.gamerental.auth

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

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

@Component
class UserDetailsServiceImpl(
    private val appUserRepository: AppUserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        appUserRepository.findByLogin(username)
            ?: throw UsernameNotFoundException("User '$username' not found")
}
