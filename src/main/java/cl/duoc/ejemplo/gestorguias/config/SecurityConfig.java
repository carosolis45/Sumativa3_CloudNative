package cl.duoc.ejemplo.gestorguias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Agregar esto
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Solo descargar requiere rol guias.read
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/guias/descargar")
                    .hasAuthority("SCOPE_guias.read")
                // Todos los demás endpoints requieren rol guias.write
                .anyRequest().hasAuthority("SCOPE_guias.write")
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(Customizer.withDefaults())
            );
        return http.build();
    }
}