package fr.pokecloud.auth.controller

import fr.pokecloud.auth.dto.*
import fr.pokecloud.auth.service.PasswordService
import fr.pokecloud.auth.service.TokenService
import fr.pokecloud.auth.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
class AuthController(
    private val userService: UserService,
    private val passwordService: PasswordService,
    private val tokenService: TokenService
) {
    @PostMapping("/login")
    @Operation(description = "Create a new session and get an authentification token.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Successful authentification", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = LoginResponse::class)
            )]
        ), ApiResponse(
            responseCode = "400", description = "Username or password is missing", content = [Content()]
        ), ApiResponse(
            responseCode = "401", description = "Invalid username or password", content = [Content()]
        )]
    )
    fun login(@RequestBody loginInformations: UsernameAndPassword): ResponseEntity<LoginResponse> {
        val user = userService.getUserByUsername(loginInformations.username)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (!passwordService.checkPassword(loginInformations.password, user.encodedPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val token = tokenService.createToken(user)
        return ResponseEntity.status(HttpStatus.OK).header("Authorization", "Bearer $token")
            .body(LoginResponse(token))
    }


    @PostMapping("/signup")
    @Operation(description = "Create a new account and get an authentification token.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Account created successfully.", content = [Content(
                mediaType = "application/json", schema = Schema(implementation = LoginResponse::class)
            )]
        ), ApiResponse(
            responseCode = "400", description = "Bad request: username or password is missing.", content = [Content()]
        ), ApiResponse(
            responseCode = "409", description = "Username already exist.", content = [Content()]
        )]
    )
    fun signup(@RequestBody signupInformations: UsernameAndPassword): ResponseEntity<LoginResponse> {
        val user = userService.createUser(signupInformations.username, signupInformations.password)

        val token = tokenService.createToken(user)
        return ResponseEntity.status(HttpStatus.OK).header("Authorization", "Bearer $token")
            .body(LoginResponse(token))
    }

    @GetMapping("/info/{userId}")
    @Operation(description = "Get user informations.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "User informations.", content = [Content(
                    mediaType = "application/json", schema = Schema(implementation = LoginResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "Resource not found.", content = [Content()]
            ),
        ]
    )
    fun info(@PathVariable("userId") userId: Long): ResponseEntity<AccountInformations> {
        val user = userService.getUserById(userId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        return ResponseEntity(AccountInformations(user.username), HttpStatus.OK)
    }

    @PutMapping("/info/")
    @Operation(description = "Update user informations.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Edited successfully.", content = [Content()]
        ), ApiResponse(
            responseCode = "400",
            description = "Invalid old password or password are not the same.",
            content = [Content()]
        ), ApiResponse(
            responseCode = "401", description = "Access token is missing or invalid.", content = [Content()]
        )]
    )
    fun edit(@RequestBody editInformations: NewAccountInformations, auth: Authentication): ResponseEntity<Void> = TODO()
}