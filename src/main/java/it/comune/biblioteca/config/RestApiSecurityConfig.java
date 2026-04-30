package it.comune.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import static it.comune.biblioteca.enums.AppRoleEnum.ROLE_SUPER_ADMIN_STRING;

@Configuration
public class RestApiSecurityConfig {
    @Bean
    @Order(1)
    public SecurityFilterChain restApiSecurityFilterChain(HttpSecurity http) throws Exception {
	return http.securityMatcher("/book-api/**").csrf(
			AbstractHttpConfigurer::disable)
		.authorizeHttpRequests(auth -> auth.anyRequest().hasAuthority(ROLE_SUPER_ADMIN_STRING))
		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.httpBasic(cfg -> cfg.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))).build();
    }
}