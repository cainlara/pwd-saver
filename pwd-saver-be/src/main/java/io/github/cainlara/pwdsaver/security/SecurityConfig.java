package io.github.cainlara.pwdsaver.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security configuration: server-side sessions (persisted via Spring
 * Session JDBC, see application.yml), BCrypt for login-password hashing,
 * uniform JSON error responses for unauthenticated/rejected requests, and
 * CORS (see {@link #corsConfigurationSource(String)}).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Origins are compared with exact, case-sensitive string equality — no
   * wildcard subdomain patterns and no bare {@code *} (Spring would otherwise
   * accept {@code *} as a literal allowed origin, but combined with
   * {@code allowCredentials(true)} that throws at request time instead of
   * failing safe, so it's filtered out here and treated as absent).
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      @Value("${cors.allowed-origins:}") String allowedOrigins) {
    List<String> origins = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim)
        .filter(origin -> !origin.isEmpty() && !"*".equals(origin))
        .toList();

    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(origins);
    configuration.setAllowCredentials(true);
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AccountUserDetailsService accountUserDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(accountUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(provider);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
      JsonAuthenticationEntryPoint entryPoint, CorsConfigurationSource corsConfigurationSource)
      throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .exceptionHandling(handling -> handling.authenticationEntryPoint(entryPoint))
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login").permitAll()
            .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/prometheus").permitAll()
            .requestMatchers("/scalar", "/scalar/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
            .anyRequest().authenticated());
    return http.build();
  }
}
