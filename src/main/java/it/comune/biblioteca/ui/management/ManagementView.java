package it.comune.biblioteca.ui.management;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import it.comune.biblioteca.entity.Category;
import it.comune.biblioteca.enums.AppRoleEnum;
import it.comune.biblioteca.service.BookService;
import it.comune.biblioteca.service.CategoryService;
import it.comune.biblioteca.ui.management.category.CategoryDetailView;
import it.comune.biblioteca.ui.common.MainLayout;
import jakarta.annotation.security.RolesAllowed;

/**
 * This page contains all management parameters such as categories and future features
 */
@Route(value = "management", layout = MainLayout.class)
@PageTitle("Gestione")
@RolesAllowed({ AppRoleEnum.ROLE_ADMIN_STRING, AppRoleEnum.ROLE_SUPER_ADMIN_STRING})
public class ManagementView extends VerticalLayout {

    private final CategoryService categoryService;
    private final BookService bookService;

    private final Grid<Category> categoryGrid = new Grid<>(Category.class, false);

    public ManagementView(CategoryService categoryService, BookService bookService) {
	this.categoryService = categoryService;
	this.bookService = bookService;
	setSizeFull();
	setPadding(true);

	Accordion accordion = new Accordion();
	accordion.setWidthFull();
	accordion.add(createCategoryPanel());

	add(accordion);
    }

    private AccordionPanel createCategoryPanel() {
	VerticalLayout categoryPanel = new VerticalLayout();
	Button addCategory = new Button(getTranslation("management.category.add-button"), new Icon(VaadinIcon.PLUS), e -> openCategoryDetail(null));
	categoryGrid.addColumn(Category::getName)
		.setHeader(getTranslation("management.category.table.col.name"))
		.setAutoWidth(true);

	categoryGrid.addColumn(bookService::countBooksByCategory)
		.setHeader(getTranslation("management.category.table.col.n-books"))
		.setAutoWidth(true);

	categoryGrid.addComponentColumn(this::buildCategoryActions)
		.setAutoWidth(true);

	categoryGrid.setItems(categoryService.findAll());
	categoryGrid.setAllRowsVisible(true); // no internal scroll, panel handles it
	categoryPanel.add(addCategory, categoryGrid);
	return new AccordionPanel(getTranslation("management.category.title"), categoryPanel);
    }

    private HorizontalLayout buildCategoryActions(Category category) {
	Button edit = new Button(getTranslation("management.category.table.col.action.edit"), new Icon(VaadinIcon.EDIT), e -> openCategoryDetail(category));
	edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

	Button delete = new Button(getTranslation("management.category.table.col.action.delete"), new Icon(VaadinIcon.TRASH), e -> performCategoryDelete(category));
	delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

	return new HorizontalLayout(edit, delete);
    }


    private void openCategoryDetail(Category category) {
	var categoryDetailDialog = new CategoryDetailView(categoryService, this::refreshCategoryGrid, category);
	categoryDetailDialog.open();
    }

    private void performCategoryDelete(Category category) {
	ConfirmDialog confirm = new ConfirmDialog();
	confirm.setHeader(getTranslation("management.category.delete-model.title"));
	confirm.setText(getTranslation("management.category.delete-model.message", category.getName())); //todo verify if it works
	confirm.setCancelable(true);
	confirm.setConfirmButtonTheme("error primary");
	confirm.setConfirmText(getTranslation("management.category.delete-model.confirm"));
	confirm.setCancelText(getTranslation("management.category.delete-model.cancel"));
	confirm.addConfirmListener(e -> {
	    categoryService.delete(category);
	    refreshCategoryGrid();
	    Notification.show(getTranslation("management.category.delete-model.success")).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	});
	confirm.open();
    }

    private void refreshCategoryGrid() {
	categoryGrid.setItems(categoryService.findAll());
    }


}
