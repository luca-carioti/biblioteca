package it.comune.biblioteca.ui.access;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "login", autoLayout = false)
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
	setSizeFull();
	setJustifyContentMode(JustifyContentMode.CENTER);
	setAlignItems(Alignment.CENTER);

	login.setAction("login");
	login.setForgotPasswordButtonVisible(false);

	add(new H1("Biblioteca comunale"), login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
	if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
	    login.setError(true);
	}
    }
}
