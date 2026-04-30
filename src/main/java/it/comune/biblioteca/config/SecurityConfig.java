package it.comune.biblioteca.config;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import it.comune.biblioteca.ui.access.LoginView;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static it.comune.biblioteca.enums.AppRoleEnum.ROLE_SUPER_ADMIN_STRING;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http.csrf(csrf -> csrf
		.ignoringRequestMatchers("/book-api/**")
	);

	http.authorizeHttpRequests(man -> man
		.requestMatchers("/images/**").permitAll()
		.requestMatchers("/styles/**").permitAll()
		.requestMatchers("/.well-known/**").permitAll()
		.requestMatchers("/book-api/**").hasAuthority(ROLE_SUPER_ADMIN_STRING)
	);
	http.logout(logout -> logout
		.logoutUrl("/logout")
		.logoutSuccessUrl("/login")
	);

	super.configure(http);
	setLoginView(http, LoginView.class);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
	return new BCryptPasswordEncoder();
    }
}