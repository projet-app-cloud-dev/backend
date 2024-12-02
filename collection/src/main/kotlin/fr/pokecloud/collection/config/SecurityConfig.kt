package fr.pokecloud.collection.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(HttpMethod.GET, "/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/**").authenticated()
                    .requestMatchers("/swagger-ui/**")
                    .permitAll().requestMatchers("/swagger-ui.html").permitAll().requestMatchers("/v3/api-docs*/**")
                    .permitAll()
                    .anyRequest().denyAll()
            }
        return http.build()
    }

}