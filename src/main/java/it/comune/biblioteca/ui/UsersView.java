package it.comune.biblioteca.ui;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import it.comune.biblioteca.entity.AppUser;
import it.comune.biblioteca.enums.AppRoleEnum;
import it.comune.biblioteca.service.UserService;

import it.comune.biblioteca.ui.common.MainLayout;
import jakarta.annotation.security.RolesAllowed;

//todo to be moved in management
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Utenti")
@RolesAllowed(AppRoleEnum.ROLE_SUPER_ADMIN_STRING)
public class UsersView extends VerticalLayout {

    public UsersView(UserService userService) {
	Grid<AppUser> grid = new Grid<>(AppUser.class, false);
	grid.addColumn(AppUser::getUsername).setHeader("Username");
	grid.addColumn(AppUser::getFirstName).setHeader("Nome");
	grid.addColumn(AppUser::getLastName).setHeader("Cognome");
	grid.addColumn(AppUser::getEmail).setHeader("Email");
	grid.addColumn(u -> u.getRoles().stream().map(r -> r.getName()).sorted().toList().toString())
		.setHeader("Ruoli");
	grid.setItems(userService.findAll());

	add(grid);
    }
}
