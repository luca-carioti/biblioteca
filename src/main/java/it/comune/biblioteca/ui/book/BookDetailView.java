package it.comune.biblioteca.ui.book;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import it.comune.biblioteca.entity.Book;
import it.comune.biblioteca.entity.Category;
import it.comune.biblioteca.service.BookService;
import it.comune.biblioteca.service.CategoryService;

import java.util.Set;
import java.util.stream.Collectors;

public class BookDetailView extends Dialog {

    private final BookService bookService;

    private final TextField name = new TextField();
    private final TextField serialCode = new TextField();
    private final TextField isbn = new TextField();
    private final TextField author = new TextField();
    private final TextField publisher = new TextField();
    private final TextArea description = new TextArea();
    private final MultiSelectComboBox<Category> categories = new MultiSelectComboBox<>();

    private final Binder<Book> binder = new BeanValidationBinder<>(Book.class);

    private final Runnable onSave;
    private final Book book;

    public BookDetailView(BookService bookService, CategoryService categoryService, Runnable onSave, Book book, boolean readOnly) {
	this.bookService = bookService;
	this.onSave = onSave;
	this.book = book;

	setWidth("600px");

	// Field labels via i18n
	name.setLabel(getTranslation("book-detail.field.title"));
	serialCode.setLabel(getTranslation("book-detail.field.serial-code"));
	isbn.setLabel(getTranslation("book-detail.field.isbn"));
	author.setLabel(getTranslation("book-detail.field.author"));
	publisher.setLabel(getTranslation("book-detail.field.publisher"));
	description.setLabel(getTranslation("book-detail.field.description"));
	categories.setLabel(getTranslation("book-detail.field.categories"));

	categories.setItems(categoryService.findAll());
	categories.setItemLabelGenerator(Category::getName);

	configureBinder();

	if (book != null) {
	    Set<Category> remapped = categories.getGenericDataView()
		    .getItems()
		    .filter(item -> book.getCategories().stream()
			    .anyMatch(bc -> bc.getId().equals(item.getId())))
		    .collect(Collectors.toSet());
	    book.setCategories(remapped);
	    binder.readBean(book);
	}

	if (readOnly)
	    setReadOnly();

	FormLayout form = new FormLayout(name, serialCode, isbn, author, publisher, description, categories);
	form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
	form.setColspan(description, 2);
	form.setColspan(categories, 2);

	add(form);
	getFooter().add(buildFooter(readOnly));
    }

    private void setReadOnly() {
	name.setReadOnly(true);
	serialCode.setReadOnly(true);
	isbn.setReadOnly(true);
	author.setReadOnly(true);
	publisher.setReadOnly(true);
	description.setReadOnly(true);
	categories.setReadOnly(true);
    }

    private void configureBinder() {
	binder.forField(name)
		.asRequired(getTranslation("book-detail.validation.title-required"))
		.bind(Book::getName, Book::setName);

	binder.forField(serialCode)
		.asRequired(getTranslation("book-detail.validation.serial-required"))
		.withValidator(value -> {
		    if (book != null && book.getSerialCode().equalsIgnoreCase(bookService.normalizeSerialCode(value))) return true;
		    return !bookService.existsBySerialCodeIgnoreCase(value);
		}, getTranslation("book-detail.validation.serial-duplicate"))
		.withValidator(
			value -> value.matches("^[a-zA-Z]+[0-9]+$"),
			getTranslation("book-detail.validation.serial-format")
		)
		.bind(Book::getSerialCode, Book::setSerialCode);

	binder.forField(isbn)
		.asRequired(getTranslation("book-detail.validation.isbn-required"))
		.bind(Book::getIsbn, Book::setIsbn);

	binder.forField(author)
		.bind(Book::getAuthor, Book::setAuthor);

	binder.forField(publisher)
		.bind(Book::getPublisher, Book::setPublisher);

	binder.forField(description)
		.bind(Book::getDescription, Book::setDescription);

	binder.forField(categories)
		.asRequired(getTranslation("book-detail.validation.categories-required"))
		.bind(Book::getCategories, Book::setCategories);

	serialCode.addBlurListener(e -> binder.validate());
    }

    private HorizontalLayout buildFooter(boolean readOnly) {
	Button cancel = new Button(getTranslation("book-detail.button.cancel"), e -> close());
	cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

	if (readOnly) {
	    return new HorizontalLayout(cancel);
	}

	Button save = new Button(getTranslation("book-detail.button.save"), e -> save());
	save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

	return new HorizontalLayout(cancel, save);
    }

    private void save() {
	Book book = this.book != null ? this.book : new Book();
	if (binder.writeBeanIfValid(book)) {
	    bookService.save(book, false);
	    Notification success = Notification.show(getTranslation("book-detail.notification.saved"));
	    success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	    onSave.run();
	    close();
	}
    }
}