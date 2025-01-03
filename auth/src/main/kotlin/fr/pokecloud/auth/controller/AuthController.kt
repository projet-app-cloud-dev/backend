package fr.pokecloud.auth.controller

import fr.pokecloud.auth.dto.AccountInformations
import fr.pokecloud.auth.dto.LoginResponse
import fr.pokecloud.auth.dto.NewAccountInformations
import fr.pokecloud.auth.dto.UsernameAndPassword
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
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
class AuthController(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
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

        if (!passwordEncoder.matches(loginInformations.password, user.encodedPassword)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val token = tokenService.createToken(user)
        return ResponseEntity.status(HttpStatus.OK).header("Authorization", "Bearer $token").body(LoginResponse(token))
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
        return ResponseEntity.status(HttpStatus.OK).header("Authorization", "Bearer $token").body(LoginResponse(token))
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
        val user = userService.getUserById(userId) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        return ResponseEntity(AccountInformations(user.username), HttpStatus.OK)
    }

    @PutMapping("/info")
    @Operation(description = "Update user informations.")
    @ApiResponses(
        value = [ApiResponse(
            responseCode = "200", description = "Edited successfully.", content = [Content()]
        ), ApiResponse(
            responseCode = "400",
            description = "Missing username or password, or old password is not valid.",
            content = [Content()]
        ), ApiResponse(
            responseCode = "401", description = "Access token is missing or invalid.", content = [Content()]
        ), ApiResponse(
            responseCode = "409", description = "Username already exist.", content = [Content()]
        )]
    )
    fun edit(@RequestBody editInformations: NewAccountInformations, auth: Authentication): ResponseEntity<Void> {
        if (editInformations.username == null && editInformations.password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }

        val user =
            userService.getUserById(auth.name.toLong()) ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (editInformations.password != null) {
            if (!passwordEncoder.matches(editInformations.password.oldPassword, user.encodedPassword)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
            }
        }

        userService.editUser(auth.name.toLong(), editInformations.username, editInformations.password?.newPassword)
        return ResponseEntity.ok().build()
    }
}