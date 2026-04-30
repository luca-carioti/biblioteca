package it.comune.biblioteca.service;

import it.comune.biblioteca.entity.AppUser;
import it.comune.biblioteca.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final AppUserRepository users;

    public UserService(AppUserRepository users) {
	this.users = users;
    }

    public List<AppUser> findAll() { return users.findAll(); }
    public AppUser save(AppUser u) { return users.save(u); }
}
