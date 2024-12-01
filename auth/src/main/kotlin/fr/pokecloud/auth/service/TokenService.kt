package fr.pokecloud.auth.service

import fr.pokecloud.auth.model.User
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TokenService(private val jwtEncoder: JwtEncoder) {
    fun createToken(user: User) : String {
        val now = Instant.now()
        val expiry = 36000L

        val claims: JwtClaimsSet =
            JwtClaimsSet.builder().issuer("self").issuedAt(now).expiresAt(now.plusSeconds(expiry))
                .subject(user.userId.toString()).claim("scope", "USER").claim("username", user.username).build()
        val jwsHeader: JwsHeader = JwsHeader.with(MacAlgorithm.HS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).tokenValue
    }
}