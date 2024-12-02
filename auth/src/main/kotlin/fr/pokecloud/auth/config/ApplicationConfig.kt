package fr.pokecloud.auth.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSelector
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler
import org.springframework.security.web.SecurityFilterChain
import java.security.Key
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@Configuration
@EnableWebSecurity
class ApplicationConfig {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeHttpRequests { authorizeRequests ->
            authorizeRequests.requestMatchers(HttpMethod.POST, "/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/signup").permitAll().requestMatchers(HttpMethod.GET, "/info/**")
                .permitAll().requestMatchers(HttpMethod.PUT, "/info").authenticated().requestMatchers("/swagger-ui/**")
                .permitAll().requestMatchers("/swagger-ui.html").permitAll().requestMatchers("/v3/api-docs*/**")
                .permitAll()
                .anyRequest().denyAll()
        }.csrf {
            it.disable()
        }.sessionManagement { session ->
            session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
            )
        }.oauth2ResourceServer { oauth2 ->
            oauth2.jwt(
                Customizer.withDefaults()
            )
        }.exceptionHandling { exceptions: ExceptionHandlingConfigurer<HttpSecurity?> ->
            exceptions.authenticationEntryPoint(BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(BearerTokenAccessDeniedHandler())
        }

        return http.build()
    }


    @Bean
    fun jwtDecoder(jwk: JWK): JwtDecoder {
        return NimbusJwtDecoder.withSecretKey(jwk.toOctetSequenceKey().toSecretKey()).build()
    }

    @Bean
    fun jwtEncoder(jwk: JWK): JwtEncoder {
        val jwkSet = JWKSet(jwk)
        val jwkSource: JWKSource<SecurityContext> =
            JWKSource<SecurityContext> { _: JWKSelector?, _: SecurityContext? -> jwkSet.keys }
        val nimbusJwtEncoder = NimbusJwtEncoder(jwkSource)
        return nimbusJwtEncoder
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Bean
    fun getJwk(environment: Environment): JWK {
        val key: Key = SecretKeySpec(
            Base64.decode(
                requireNotNull(environment.getProperty("jwt.key")) {
                    "Missing JWT_KEY environment variable"
                }), JWSAlgorithm.HS256.name
        )
        val jwk: JWK = OctetSequenceKey.Builder(key.encoded).algorithm(JWSAlgorithm.HS256).build()
        return jwk
    }

    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}