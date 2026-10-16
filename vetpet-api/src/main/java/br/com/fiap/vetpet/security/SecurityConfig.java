package br.com.fiap.vetpet.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String VETERINARIO = "VETERINARIO";
    private static final String TUTOR = "TUTOR";

    private static final String[] ROTAS_PUBLICAS = {
            "/", "/login", "/error", "/css/**", "/js/**", "/img/**", "/favicon/**",
            "/actuator/health/**", "/actuator/info", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    };

    private static final String[] ROTAS_GESTAO_VETERINARIOS = {
            "/veterinarios/novo", "/veterinarios/*/editar", "/veterinarios/*/excluir"
    };

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/tutores").permitAll()
                        .requestMatchers("/api/v1/me/**").hasRole(TUTOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/veterinarios").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/veterinarios/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/veterinarios/**").hasRole(ADMIN)
                        .anyRequest().hasAnyRole(ADMIN, VETERINARIO))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        .requestMatchers(ROTAS_GESTAO_VETERINARIOS).hasRole(ADMIN)
                        .anyRequest().hasAnyRole(ADMIN, VETERINARIO))
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?erro")
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?saiu")
                        .permitAll());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${vetpet.cors.origens}") String origens) {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOriginPatterns(Arrays.stream(origens.split(",")).map(String::trim).toList());
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("*"));
        configuracao.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuracao);
        return source;
    }
}
