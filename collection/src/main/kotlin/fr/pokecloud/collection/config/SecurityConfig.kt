package fr.pokecloud.collection.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.OctetSequenceKey
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler
import org.springframework.security.web.SecurityFilterChain
import java.security.Key
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }.authorizeHttpRequests { requests ->
            requests.requestMatchers(HttpMethod.GET, "/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll().requestMatchers(HttpMethod.POST, "/**")
                .authenticated().requestMatchers(HttpMethod.DELETE, "/**").authenticated()
                .requestMatchers("/swagger-ui/**").permitAll().requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/api-docs/").permitAll().requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs*/**").permitAll()
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .anyRequest().denyAll()
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
        }.cors(Customizer.withDefaults())
        return http.build()
    }

    @Bean
    fun jwtDecoder(jwk: JWK): JwtDecoder {
        return NimbusJwtDecoder.withSecretKey(jwk.toOctetSequenceKey().toSecretKey()).build()
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
}