package sms.pollub.gamerental.client

import jakarta.persistence.Access
import jakarta.persistence.AccessType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import sms.pollub.gamerental.shared.BaseEntity

@Entity
@Table(name = "clients")
@Access(AccessType.FIELD)
class Client(
    @Column(nullable = false)
    var firstName: String,

    @Column(nullable = false)
    var lastName: String,

    var phone: String? = null,

    var email: String? = null
) : BaseEntity()

