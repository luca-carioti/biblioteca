package it.comune.biblioteca.ui.book;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.comune.biblioteca.entity.Book;
import it.comune.biblioteca.entity.Category;
import it.comune.biblioteca.service.BookService;
import it.comune.biblioteca.service.CategoryService;
import it.comune.biblioteca.ui.common.MainLayout;
import it.comune.biblioteca.util.SecurityUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.vaadin.flow.component.Key.ENTER;
import static it.comune.biblioteca.enums.AppRoleEnum.ROLE_ADMIN;
import static it.comune.biblioteca.enums.AppRoleEnum.ROLE_SUPER_ADMIN;

@Route(value = "books", layout = MainLayout.class)
@PageTitle("Libri")
@AnonymousAllowed
public class BookView extends VerticalLayout {

    private final BookService bookService;
    private final CategoryService categoryService;

    private final Grid<Book> grid = new Grid<>(Book.class, false);
    private final TextField search = new TextField(getTranslation("book-view.search"));
    private final ComboBox<Category> categoryFilter = new ComboBox<>(getTranslation("book-view.category"));

    public BookView(BookService bookService, CategoryService categoryService) {
	this.bookService = bookService;
	this.categoryService = categoryService;

	configureGrid();
	configureFilters();

	Button add = new Button(getTranslation("book-view.add-button"), e -> openBookDetailDialog(null, false));

	boolean canManage = SecurityUtils.hasAnyRole(ROLE_ADMIN.name(), ROLE_SUPER_ADMIN.name());
	add.setVisible(canManage);

	Button searchButton = new Button(getTranslation("book-view.search-button"), e -> advancedSearch());
	searchButton.addClickShortcut(ENTER);

	HorizontalLayout toolbar = new HorizontalLayout(search, categoryFilter, searchButton, add);
	toolbar.setAlignItems(Alignment.END);

	add(toolbar, grid);
	setSizeFull();

	refresh();
    }

    private void configureGrid() {
	grid.addColumn(Book::getName).setHeader(getTranslation("book-view.table.col.name")).setAutoWidth(true);
	grid.addColumn(Book::getAuthor).setHeader(getTranslation("book-view.table.col.author")).setAutoWidth(true);
	grid.addColumn(Book::getPublisher).setHeader(getTranslation("book-view.table.col.publisher")).setAutoWidth(true);
	grid.addColumn(Book::getSerialCode).setHeader(getTranslation("book-view.table.col.serial-code")).setAutoWidth(true);
	grid.addColumn(Book::getIsbn).setHeader(getTranslation("book-view.table.col.isbn")).setAutoWidth(true);
	grid.addComponentColumn(this::buildCategoryBadges)
		.setHeader(getTranslation("book-view.table.col.category"))
		.setAutoWidth(true);
	grid.addComponentColumn(this::buildBookActions).setAutoWidth(true);
	grid.setSelectionMode(Grid.SelectionMode.SINGLE);
	grid.setHeight("70vh");
    }

    private HorizontalLayout buildCategoryBadges(Book book) {
	HorizontalLayout layout = new HorizontalLayout();
	layout.setSpacing(true);
	layout.setAlignItems(FlexComponent.Alignment.CENTER);

	List<Category> categories = categoryService.findCategoriesNameByBookId(book.getId());
	if (categories == null || categories.isEmpty()) {
	    return layout;
	}

	// Show up to maxVisible badges
	categories.stream().limit(3).forEach(cat -> {
	    Span badge = new Span(cat.getName());
	    badge.getElement().getThemeList().add("badge");
	    badge.getStyle()
		    .set("font-size", "var(--lumo-font-size-xs)")
		    .set("white-space", "nowrap");
	    layout.add(badge);
	});

	// If there are more, show a "+N" badge
	if (categories.size() > 3) {
	    int remaining = categories.size() - 3;
	    Span more = new Span("+" + remaining);
	    more.getElement().getThemeList().add("badge contrast");
	    more.getStyle().set("font-size", "var(--lumo-font-size-xs)");
	    layout.add(more);
	}

	return layout;
    }

    private void configureFilters() {
	search.setPlaceholder(getTranslation("book-view.search-placeholder"));
	search.setClearButtonVisible(true);

	categoryFilter.setItems(categoryService.findAll());
	categoryFilter.setItemLabelGenerator(Category::getName);
	categoryFilter.setClearButtonVisible(true);
    }

    private void refresh() {
	List<Book> all = bookService.findAll();
	search.clear();
	categoryFilter.clear();
	grid.setItems(all);
    }

    private void advancedSearch() {
	grid.setItems(bookService.advancedSearch(search.getValue(), new HashSet<>(categoryFilter.getValue() != null ? List.of(categoryFilter.getValue()) : new ArrayList<>())));
    }

    private void openBookDetailDialog(Book book, boolean readOnly) {
	Book fullBook = book != null ? bookService.findByIdWithCategories(book.getId()) : null;
	BookDetailView dialog = new BookDetailView(bookService, categoryService, this::refresh, fullBook, readOnly);
	dialog.open();
    }

    private HorizontalLayout buildBookActions(Book book) {
	Button edit = new Button(getTranslation("book-view.table.col.action.edit"), new Icon(VaadinIcon.EDIT), e -> openBookDetailDialog(book, !SecurityUtils.hasAnyRole(ROLE_ADMIN.name(), ROLE_SUPER_ADMIN.name())));
	edit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

	if(!SecurityUtils.hasAnyRole(ROLE_ADMIN.name(), ROLE_SUPER_ADMIN.name())) {
	    return new HorizontalLayout(edit);
	}

	Button delete = new Button(getTranslation("book-view.table.col.action.delete"), new Icon(VaadinIcon.TRASH), e -> performCategoryDelete(book));
	delete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

	return new HorizontalLayout(edit, delete);
    }

    private void performCategoryDelete(Book book) {
	ConfirmDialog confirm = new ConfirmDialog();
	confirm.setHeader(getTranslation("book-view.delete-modal.title"));
	confirm.setText(getTranslation("book-view.delete-modal.message"));
	confirm.setCancelable(true);
	confirm.setConfirmButtonTheme("error primary");
	confirm.setConfirmText(getTranslation("book-view.delete-modal.confirm"));
	confirm.setCancelText(getTranslation("book-view.delete-modal.cancel"));
	confirm.addConfirmListener(e -> {
	    bookService.delete(book);
	    refresh();
	    Notification.show(getTranslation("book-view.delete-modal.success")).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	});
	confirm.open();
    }
}
