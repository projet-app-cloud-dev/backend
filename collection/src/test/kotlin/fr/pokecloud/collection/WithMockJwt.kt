package fr.pokecloud.collection

import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.lang.annotation.Inherited


@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory::class)
annotation class WithMockJwt(val value: Long = 1L, val roles: Array<String> = [])

class WithMockJwtSecurityContextFactory : WithSecurityContextFactory<WithMockJwt> {
    override fun createSecurityContext(annotation: WithMockJwt): SecurityContext {
        val jwt = Jwt.withTokenValue("token").header("alg", "none").subject(annotation.value.toString()).build()

        val authorities = AuthorityUtils.createAuthorityList(*annotation.roles)
        val token = JwtAuthenticationToken(jwt, authorities)

        val context: SecurityContext = SecurityContextHolder.createEmptyContext()
        context.authentication = token
        return context
    }
}