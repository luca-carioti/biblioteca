package it.comune.biblioteca.ui.common;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.spring.security.AuthenticationContext;
import it.comune.biblioteca.enums.AppRoleEnum;
import it.comune.biblioteca.ui.UsersView;
import it.comune.biblioteca.ui.book.BookView;
import it.comune.biblioteca.ui.management.ManagementView;
import it.comune.biblioteca.util.SecurityUtils;

@CssImport("./styles/styles.css")
public class MainLayout extends AppLayout implements RouterLayout {

    private final AuthenticationContext authenticationContext;

    public MainLayout(AuthenticationContext authenticationContext) {
	this.authenticationContext = authenticationContext;

	setPrimarySection(Section.NAVBAR);

	// --- NAVBAR ---
	DrawerToggle toggle = new DrawerToggle();

	Image logo = new Image("images/logo.png", "Logo");
	logo.setHeight("36px");
	logo.getStyle().set("object-fit", "contain");

	H1 title = new H1(getTranslation("mainLayout.title"));
	title.getStyle()
		.set("font-size", "1.2em")
		.set("margin", "0")
		.set("white-space", "nowrap")
		.set("color", "#ffffff"); // bianco sulla navbar blu

	HorizontalLayout navbar = new HorizontalLayout(toggle, logo, title);
	navbar.setWidthFull();
	navbar.setAlignItems(FlexComponent.Alignment.CENTER);
	navbar.setSpacing(true);
	navbar.setPadding(true);

	addToNavbar(navbar);

	// --- DRAWER ---
	SideNav nav = new SideNav();
	nav.addItem(new SideNavItem(getTranslation("mainLayout.sidenav.home"), HomeView.class));
	nav.addItem(new SideNavItem(getTranslation("mainLayout.sidenav.books"), BookView.class));
	if (SecurityUtils.hasAnyRole(AppRoleEnum.ROLE_ADMIN_STRING, AppRoleEnum.ROLE_SUPER_ADMIN_STRING)) {
	    nav.addItem(new SideNavItem(getTranslation("mainLayout.sidenav.management"), ManagementView.class));
	}
	if (SecurityUtils.hasAnyRole(AppRoleEnum.ROLE_SUPER_ADMIN_STRING)) {
	    nav.addItem(new SideNavItem(getTranslation("mainLayout.sidenav.users"), UsersView.class));
	}

	Scroller navScroller = new Scroller(nav);
	navScroller.setSizeFull();

	VerticalLayout drawerLayout = new VerticalLayout(navScroller);
	drawerLayout.setSizeFull();
	drawerLayout.setPadding(true);
	drawerLayout.setSpacing(false);
	drawerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
	drawerLayout.setFlexGrow(1, navScroller);

	if (SecurityUtils.isLoggedIn()) {
	    Button logout = new Button(getTranslation("mainLayout.sidenav.logout"), new Icon(VaadinIcon.SIGN_OUT), e -> doLogout());
	    logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
	    logout.setWidthFull();
	    drawerLayout.add(logout);
	}

	addToDrawer(drawerLayout);
    }

    private void doLogout() {
	authenticationContext.logout();
    }
}