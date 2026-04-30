package it.comune.biblioteca.ui.common;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import it.comune.biblioteca.service.BookService;
import it.comune.biblioteca.service.CategoryService;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    public HomeView(BookService bookService, CategoryService categoryService) {
	setPadding(true);
	setSpacing(true);
	setWidthFull();
	getStyle().set("background-color", "var(--library-surface)");

	// --- Text section (standalone card) ---
	H2 descTitle = new H2(getTranslation("home.desc.title"));
	descTitle.getStyle()
		.set("color", "var(--library-primary)")
		.set("margin-top", "0");

	VerticalLayout textSection = new VerticalLayout(
		descTitle,
		new Paragraph(getTranslation("home.desc.par1")),
		new Paragraph(getTranslation("home.desc.par2")),
		new Paragraph(getTranslation("home.desc.par3"))
	);
	textSection.setPadding(false);
	textSection.setSpacing(false);
	textSection.setWidthFull();
	textSection.getStyle()
		.set("color", "var(--library-text)")
		.set("background-color", "#ffffff")
		.set("border-radius", "12px")
		.set("padding", "24px")
		.set("box-shadow", "0 1px 6px rgba(42,100,150,0.10)")
		.set("border", "1px solid var(--library-border)");

	// --- Stats cards row ---
	// Books count card
	Span booksEmoji = new Span("📚");
	booksEmoji.getStyle().set("font-size", "2rem").set("line-height", "1");

	Span booksCount = new Span(bookService.count().toString());
	booksCount.setId("stat-books-count");
	booksCount.getStyle()
		.set("font-size", "2.2rem")
		.set("font-weight", "700")
		.set("color", "#1565C0")
		.set("line-height", "1.1");

	Span booksLabel = new Span(getTranslation("home.stat.totalBooks"));
	booksLabel.getStyle()
		.set("font-size", "0.85rem")
		.set("color", "#5e7fa8")
		.set("text-transform", "uppercase")
		.set("letter-spacing", "0.05em");

	VerticalLayout booksCard = new VerticalLayout(booksEmoji, booksCount, booksLabel);
	booksCard.setPadding(false);
	booksCard.setSpacing(false);
	booksCard.setAlignItems(FlexComponent.Alignment.CENTER);
	booksCard.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
	booksCard.getStyle()
		.set("background", "linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)")
		.set("border-radius", "16px")
		.set("padding", "28px 20px")
		.set("flex", "1")
		.set("min-width", "160px")
		.set("box-shadow", "0 2px 10px rgba(21,101,192,0.13)")
		.set("border", "1px solid #90caf9")
		.set("gap", "6px");

	// Categories count card
	Span catEmoji = new Span("🗂️");
	catEmoji.getStyle().set("font-size", "2rem").set("line-height", "1");

	Span catCount = new Span(categoryService.count().toString());
	catCount.setId("stat-categories-count");
	catCount.getStyle()
		.set("font-size", "2.2rem")
		.set("font-weight", "700")
		.set("color", "#2E7D32")
		.set("line-height", "1.1");

	Span catLabel = new Span(getTranslation("home.stat.totalCategories"));
	catLabel.getStyle()
		.set("font-size", "0.85rem")
		.set("color", "#5a7f5e")
		.set("text-transform", "uppercase")
		.set("letter-spacing", "0.05em");

	VerticalLayout catCard = new VerticalLayout(catEmoji, catCount, catLabel);
	catCard.setPadding(false);
	catCard.setSpacing(false);
	catCard.setAlignItems(FlexComponent.Alignment.CENTER);
	catCard.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
	catCard.getStyle()
		.set("background", "linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%)")
		.set("border-radius", "16px")
		.set("padding", "28px 20px")
		.set("flex", "1")
		.set("min-width", "160px")
		.set("box-shadow", "0 2px 10px rgba(46,125,50,0.13)")
		.set("border", "1px solid #a5d6a7")
		.set("gap", "6px");

	HorizontalLayout statsRow = new HorizontalLayout(booksCard, catCard);
	statsRow.setWidthFull();
	statsRow.setSpacing(true);
	statsRow.getStyle().set("gap", "16px");

	// --- Photos row ---
	Image photoSmall = new Image("images/biblioteca.jpg", "Biblioteca");
	photoSmall.getStyle()
		.set("width", "100%")
		.set("height", "100%")
		.set("object-fit", "cover")
		.set("border-radius", "12px")
		.set("box-shadow", "0 2px 8px rgba(0,0,0,0.12)")
		.set("display", "block");

	Div photoSmallWrapper = new Div(photoSmall);
	photoSmallWrapper.getStyle()
		.set("flex", "1 1 0")
		.set("min-width", "0")
		.set("height", "280px")
		.set("overflow", "hidden")
		.set("border-radius", "12px");

	Image photoBig = new Image("images/scaffali.jpg", "Scaffali biblioteca");
	photoBig.getStyle()
		.set("width", "100%")
		.set("height", "100%")
		.set("object-fit", "cover")
		.set("border-radius", "12px")
		.set("box-shadow", "0 2px 10px rgba(0,0,0,0.12)")
		.set("display", "block");

	Div photoBigWrapper = new Div(photoBig);
	photoBigWrapper.getStyle()
		.set("flex", "1 1 0")
		.set("min-width", "0")
		.set("height", "280px")
		.set("overflow", "hidden")
		.set("border-radius", "12px");

	HorizontalLayout photosRow = new HorizontalLayout(photoSmallWrapper, photoBigWrapper);
	photosRow.setWidthFull();
	photosRow.getStyle()
		.set("gap", "16px")
		.set("align-items", "stretch")
		.set("display", "flex");

	add(textSection, statsRow, photosRow);
    }
}