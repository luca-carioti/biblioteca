package it.comune.biblioteca.service;

import it.comune.biblioteca.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public CustomUserDetailsService(AppUserRepository users) {
	this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	var u = users.findByUsername(username)
		.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

	var authorities = u.getRoles().stream()
		.map(r -> new SimpleGrantedAuthority(r.getName()))
		.toList();

	return User.builder()
		.username(u.getUsername())
		.password(u.getPassword())
		.disabled(!u.isEnabled())
		.authorities(authorities)
		.build();
    }
}
