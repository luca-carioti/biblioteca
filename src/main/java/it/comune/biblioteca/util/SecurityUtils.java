package it.comune.biblioteca.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static boolean hasAnyRole(String... roles) {
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	if (auth == null || auth.getAuthorities() == null) return false;
	for (String r : roles) {
	    if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(r))) return true;
	}
	return false;
    }

    public static boolean isLoggedIn() {
	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	return auth != null
		&& auth.isAuthenticated()
		&& !(auth instanceof AnonymousAuthenticationToken);
    }
}
