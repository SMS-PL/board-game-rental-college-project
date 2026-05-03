package sms.pollub.gamerental.auth

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import sms.pollub.gamerental.shared.BaseEntity

@Entity
@Table(name = "app_users")
@Access(AccessType.FIELD)
class AppUser(
    @Column(name = "username", unique = true, nullable = false)
    val login: String,

    @Column(name = "password", nullable = false)
    var passwordHash: String
) : BaseEntity(), UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()
    override fun getPassword(): String = passwordHash
    override fun getUsername(): String = login
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
