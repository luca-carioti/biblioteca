package it.comune.biblioteca.ui.management.category;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import it.comune.biblioteca.entity.Category;
import it.comune.biblioteca.service.CategoryService;
import org.apache.commons.lang3.StringUtils;

public class CategoryDetailView extends Dialog {

    private final CategoryService categoryService;

    private final Binder<Category> binder = new BeanValidationBinder<>(Category.class);
    private final Runnable onSave;

    private final Category category;
    private final TextField name = new TextField(getTranslation("category.detail.name"));

    public CategoryDetailView(CategoryService categoryService, Runnable onSave, Category category) {
        this.categoryService = categoryService;
	this.onSave = onSave;
	this.category = category;
	setWidth("600px");

	configureBinder();
	if(category != null) binder.readBean(category);

	FormLayout form = new FormLayout(name);
	form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

	add(form);
	getFooter().add(buildFooter());
    }

    private HorizontalLayout buildFooter() {
	Button cancel = new Button(getTranslation("category.detail.cancelButton"), e -> close());
	cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

	Button save = new Button(getTranslation("category.detail.saveButton"), e -> save());
	save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

	return new HorizontalLayout(cancel, save);
    }

    private void save() {
	var target = this.category != null ? this.category : new Category();
	if (binder.writeBeanIfValid(target)) {
	    categoryService.save(target);
	    Notification success = Notification.show(getTranslation("notification.category.saved"));
	    success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	    onSave.run();
	    close();
	}
    }

    private void configureBinder() {
	binder.forField(name)
		.asRequired(getTranslation("category.required-field.name"))
		.withValidator(value -> {
		    if(category != null && StringUtils.equalsIgnoreCase(categoryService.normalizeName(value), category.getName())) return true;
		    return !categoryService.existsByCodeIgnoreCase(value);
		}, getTranslation("category.existing.name"))
		.bind(Category::getName, Category::setName);
    }
}
