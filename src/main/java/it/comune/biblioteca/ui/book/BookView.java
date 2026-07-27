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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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

    private int currentPage = 0;
    private int totalPages = 0;

    private final ComboBox<Integer> itemsPerPageSelect = new ComboBox<>();
    private final Button firstButton = new Button();
    private final Button prevButton = new Button();
    private final Button nextButton = new Button();
    private final Button lastButton = new Button();
    private final Span pageLabel = new Span();
    private final HorizontalLayout paginationRow = new HorizontalLayout();

    public BookView(BookService bookService, CategoryService categoryService) {
	this.bookService = bookService;
	this.categoryService = categoryService;

	configureGrid();
	configureFilters();
	configurePagination();

	Button add = new Button(getTranslation("book-view.add-button"), e -> openBookDetailDialog(null, false));

	boolean canManage = SecurityUtils.hasAnyRole(ROLE_ADMIN.name(), ROLE_SUPER_ADMIN.name());
	add.setVisible(canManage);

	Button searchButton = new Button(getTranslation("book-view.search-button"), e -> advancedSearch());
	searchButton.addClickShortcut(ENTER);

	HorizontalLayout toolbar = new HorizontalLayout(search, categoryFilter, searchButton, add);
	toolbar.setAlignItems(Alignment.END);

	add(toolbar, paginationRow, grid);
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
	currentPage = 0;
	search.clear();
	categoryFilter.clear();
	updateGrid();
    }

    private void advancedSearch() {
	currentPage = 0;
	updateGrid();
    }

    private void configurePagination() {
	itemsPerPageSelect.setLabel(getTranslation("book-view.pagination.items-per-page"));
	itemsPerPageSelect.setItems(5, 10, 20, 50);
	itemsPerPageSelect.setValue(10);
	itemsPerPageSelect.setAllowCustomValue(false);
	itemsPerPageSelect.setWidth("150px");
	itemsPerPageSelect.addValueChangeListener(e -> {
	    currentPage = 0;
	    updateGrid();
	});

	firstButton.setIcon(new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT));
	firstButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
	firstButton.addClickListener(e -> {
	    currentPage = 0;
	    updateGrid();
	});

	prevButton.setIcon(new Icon(VaadinIcon.ANGLE_LEFT));
	prevButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
	prevButton.addClickListener(e -> {
	    if (currentPage > 0) {
		currentPage--;
		updateGrid();
	    }
	});

	nextButton.setIcon(new Icon(VaadinIcon.ANGLE_RIGHT));
	nextButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
	nextButton.addClickListener(e -> {
	    if (currentPage < totalPages - 1) {
		currentPage++;
		updateGrid();
	    }
	});

	lastButton.setIcon(new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));
	lastButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
	lastButton.addClickListener(e -> {
	    currentPage = totalPages - 1;
	    updateGrid();
	});

	HorizontalLayout navigationLayout = new HorizontalLayout(firstButton, prevButton, pageLabel, nextButton, lastButton);
	navigationLayout.setAlignItems(Alignment.CENTER);

	paginationRow.add(itemsPerPageSelect, navigationLayout);
	paginationRow.setWidthFull();
	paginationRow.setJustifyContentMode(JustifyContentMode.BETWEEN);
	paginationRow.setAlignItems(Alignment.CENTER);
    }

    private void updateGrid() {
	String query = search.getValue();
	Category category = categoryFilter.getValue();
	int pageSize = itemsPerPageSelect.getValue() != null ? itemsPerPageSelect.getValue() : 10;

	Page<Book> bookPage = bookService.advancedSearch(
		query,
		category != null ? Set.of(category) : new HashSet<>(),
		PageRequest.of(currentPage, pageSize)
	);

	grid.setItems(bookPage.getContent());
	totalPages = bookPage.getTotalPages();

	updatePaginationUI();
    }

    private void updatePaginationUI() {
	firstButton.setEnabled(currentPage > 0);
	prevButton.setEnabled(currentPage > 0);
	nextButton.setEnabled(currentPage < totalPages - 1);
	lastButton.setEnabled(currentPage < totalPages - 1);

	int displayPage = totalPages == 0 ? 0 : currentPage + 1;
	pageLabel.setText(getTranslation("book-view.pagination.page-info", displayPage, totalPages));
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
