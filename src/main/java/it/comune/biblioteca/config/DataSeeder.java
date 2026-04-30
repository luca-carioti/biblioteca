package it.comune.biblioteca.config;

import it.comune.biblioteca.entity.AppUser;
import it.comune.biblioteca.entity.Role;
import it.comune.biblioteca.repository.AppUserRepository;
import it.comune.biblioteca.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static it.comune.biblioteca.enums.AppRoleEnum.ROLE_ADMIN;
import static it.comune.biblioteca.enums.AppRoleEnum.ROLE_SUPER_ADMIN;
import static it.comune.biblioteca.enums.AppRoleEnum.ROLE_USER;

@Configuration
public class DataSeeder {
    @Value("${security.user.super_admin.username}")
    private String superAdminUsername;
    @Value("${security.user.super_admin.password}")
    private String superAdminPassword;
    @Value("${security.user.super_admin.email}")
    private String superAdminEmail;
    @Value( "${security.user.admin.username}")
    private String adminUsername;
    @Value("${security.user.admin.password}")
    private String adminPassword;
    @Value("${security.user.admin.email}")
    private String adminEmail;
    @Value("${security.user.simple_user.username}")
    private String userUsername;
    @Value("${security.user.simple_user.password}")
    private String userPassword;
    @Value("${security.user.simple_user.email}")
    private String userEmail;

    @Bean
    CommandLineRunner seed(RoleRepository roles, AppUserRepository users, PasswordEncoder encoder) {
	return args -> {
	    Role superAdmin = roles.findByName(ROLE_SUPER_ADMIN.name()).orElseGet(() -> {
		Role r = new Role();
		r.setName(ROLE_SUPER_ADMIN.name());
		return roles.save(r);
	    });
	    Role admin = roles.findByName(ROLE_ADMIN.name()).orElseGet(() -> {
		Role r = new Role();
		r.setName(ROLE_ADMIN.name());
		return roles.save(r);
	    });
	    Role user = roles.findByName(ROLE_USER.name()).orElseGet(() -> {
		Role r = new Role();
		r.setName(ROLE_USER.name());
		return roles.save(r);
	    });

	    users.findByUsername(superAdminUsername).orElseGet(() -> {
		AppUser u = new AppUser();
		u.setUsername(superAdminUsername);
		u.setPassword(encoder.encode(superAdminPassword));
		u.setEmail(superAdminEmail);
		u.setEnabled(true);
		u.setRoles(Set.of(superAdmin));
		return users.save(u);
	    });

	    users.findByUsername(adminUsername).orElseGet(() -> {
		AppUser u = new AppUser();
		u.setUsername(adminUsername);
		u.setPassword(encoder.encode(adminPassword));
		u.setEmail(adminEmail);
		u.setEnabled(true);
		u.setRoles(Set.of(admin));
		return users.save(u);
	    });

	    users.findByUsername(userUsername).orElseGet(() -> {
		AppUser u = new AppUser();
		u.setUsername(userUsername);
		u.setPassword(encoder.encode(userPassword));
		u.setEmail(userEmail);
		u.setEnabled(true);
		u.setRoles(Set.of(user));
		return users.save(u);
	    });
	};
    }
}
