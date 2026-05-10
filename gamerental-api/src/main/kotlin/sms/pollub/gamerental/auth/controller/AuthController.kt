package sms.pollub.gamerental.auth.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import sms.pollub.gamerental.auth.commands.LoginCommand
import sms.pollub.gamerental.auth.commands.RegisterCommand
import sms.pollub.gamerental.auth.commands.TokenResponse
import sms.pollub.gamerental.auth.handlers.LoginCommandHandler
import sms.pollub.gamerental.auth.handlers.RegisterCommandHandler

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerCommandHandler: RegisterCommandHandler,
    private val loginCommandHandler: LoginCommandHandler
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody command: RegisterCommand) {
        registerCommandHandler.handle(command)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody command: LoginCommand): TokenResponse =
        loginCommandHandler.handle(command)
}

