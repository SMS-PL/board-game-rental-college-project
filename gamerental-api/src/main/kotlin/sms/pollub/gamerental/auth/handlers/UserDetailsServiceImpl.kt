package sms.pollub.gamerental.auth.handlers

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import sms.pollub.gamerental.auth.repository.AppUserRepository

@Component
class UserDetailsServiceImpl(
    private val appUserRepository: AppUserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
        appUserRepository.findByLogin(username)
            ?: throw UsernameNotFoundException("User '$username' not found")
}

