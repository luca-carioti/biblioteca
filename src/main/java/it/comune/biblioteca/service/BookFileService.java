package it.comune.biblioteca.service;

import it.comune.biblioteca.entity.Book;
import it.comune.biblioteca.entity.Category;
import it.comune.biblioteca.enums.ExceptionCodeEnum;
import it.comune.biblioteca.exception.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

@Service
public class BookFileService {

    private static final Logger log = LoggerFactory.getLogger(BookFileService.class);

    // Column indices
    private static final int COL_TITLE     = 0;
    private static final int COL_AUTHOR    = 1;
    private static final int COL_PUBLISHER = 2;
    private static final int COL_SERIAL    = 3;

    private final BookService bookService;
    private final CategoryService categoryService;

    public BookFileService(BookService bookService, CategoryService categoryService) {
	this.bookService = bookService;
	this.categoryService = categoryService;
    }

    /**
     * Imports books from an Excel file.
     * Each sheet represents a category; columns are: title, author, publisher, serial code.
     *
     * @param file the uploaded .xlsx file
     * @return an {@link ImportReport} summarising what was inserted and what was skipped
     */
    public ImportReport importBooks(MultipartFile file) {
	ImportReport report = new ImportReport();

	try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
	    int sheetCount = workbook.getNumberOfSheets();

	    for (int i = 0; i < sheetCount; i++) {
		Sheet currentSheet = workbook.getSheetAt(i);
		String sheetName = currentSheet.getSheetName();

		CategoryResult categoryResult = manageCategoryBySheetName(sheetName, report);
		manageBooksInSheet(currentSheet, categoryResult.category(), report);
	    }

	} catch (IOException e) {
	    log.error("Failed to read uploaded file", e);
	    throw new RuntimeException("Failed to read uploaded file: " + e.getMessage(), e);
	}

	log.info("Import complete. {}", report.summary());
	return report;
    }

    // -------------------------------------------------------------------------
    // Category handling
    // -------------------------------------------------------------------------

    private CategoryResult manageCategoryBySheetName(String sheetName, ImportReport report) {
	String normalizedName = categoryService.normalizeName(sheetName);
	Optional<Category> existing = categoryService.findByName(normalizedName);

	if (existing.isPresent()) {
	    report.categorySkipped();
	    log.debug("Category already exists, skipping creation: {}", normalizedName);
	    return new CategoryResult(existing.get(), false);
	}

	Category category = new Category();
	category.setName(normalizedName);
	try {
	    category = categoryService.save(category);
	    report.categoryInserted();
	    log.debug("Category created: {}", normalizedName);
	} catch (PersistenceException pe) {
	    // Race condition: another thread/request already inserted it
	    log.warn("Category '{}' could not be saved ({}), attempting to reload", normalizedName, pe.getMessage());
	    category = categoryService.findByName(normalizedName)
		    .orElseThrow(() -> new PersistenceException(
			    ExceptionCodeEnum.G_001,
			    "Category not found after failed insert: " + normalizedName));
	    report.categorySkipped();
	}
	return new CategoryResult(category, true);
    }

    // -------------------------------------------------------------------------
    // Book handling
    // -------------------------------------------------------------------------

    private void manageBooksInSheet(Sheet sheet, Category category, ImportReport report) {
	for (Row row : sheet) {
	    if (isRowEmpty(row)) continue;
	    try {
		manageBookRow(row, category, report);
	    } catch (Exception e) {
		log.warn("Skipping row {} in sheet '{}': {}",
			row.getRowNum(), sheet.getSheetName(), e.getMessage());
	    }
	}
    }

    private void manageBookRow(Row row, Category category, ImportReport report) {
	String title      = getCellValue(row, COL_TITLE);
	String author     = getCellValue(row, COL_AUTHOR);
	String publisher  = getCellValue(row, COL_PUBLISHER);
	String serialCode = getCellValue(row, COL_SERIAL);

	// Title is mandatory
	if (StringUtils.isBlank(title)) {
	    log.debug("Row {} skipped: empty title", row.getRowNum());
	    return;
	}

	// Serial code is mandatory for the uniqueness check defined in BookService
	if (StringUtils.isBlank(serialCode)) {
	    log.debug("Row {} skipped: no serial code for title '{}'", row.getRowNum(), title);
	    report.bookSkipped();
	    return;
	}

	String normalizedSerial = bookService.normalizeSerialCode(serialCode);

	// Validate serial code format before checking existence to avoid a pointless DB call
	if (!normalizedSerial.matches("^[a-zA-Z]+[0-9]+$")) {
	    log.debug("Row {} skipped: serial code '{}' has invalid format", row.getRowNum(), normalizedSerial);
	    report.bookSkipped();
	    return;
	}

	// Skip if a book with the same serial code already exists (covers duplicate titles too,
	// since serial codes are meant to be unique library identifiers)
	if (bookService.existsBySerialCodeIgnoreCase(normalizedSerial)) {
	    log.debug("Book with serial '{}' already exists, skipping", normalizedSerial);
	    report.bookSkipped();
	    return;
	}

	Book book = new Book();
	book.setName(title);
	book.setAuthor(author);
	book.setPublisher(publisher);
	book.setSerialCode(normalizedSerial);
	book.setIsbn("");  // ISBN not present in the file; set empty to satisfy @Column(nullable=false)
	book.setCategories(new HashSet<>());
	book.getCategories().add(category);

	bookService.save(book, true);
	report.bookInserted();
	log.debug("Book inserted: '{}' [{}]", title, normalizedSerial);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String getCellValue(Row row, int colIndex) {
	if (row == null) return "";
	Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	if (cell == null) return "";
	return switch (cell.getCellType()) {
	    case STRING  -> cell.getStringCellValue().trim();
	    case NUMERIC -> {
		// Avoid ".0" suffix for whole numbers
		double d = cell.getNumericCellValue();
		yield (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
	    }
	    case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
	    case FORMULA -> cell.getCachedFormulaResultType() == CellType.STRING
		    ? cell.getStringCellValue().trim()
		    : String.valueOf(cell.getNumericCellValue());
	    default -> "";
	};
    }

    private boolean isRowEmpty(Row row) {
	if (row == null) return true;
	for (int c = COL_TITLE; c <= COL_SERIAL; c++) {
	    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
	    if (cell != null && cell.getCellType() != CellType.BLANK) return false;
	}
	return true;
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    /** Lightweight carrier for the category and whether it was just created. */
    private record CategoryResult(Category category, boolean created) {}

    /**
     * Accumulates counters for the import process and exposes a human-readable summary.
     */
    public static class ImportReport {

	private int categoriesInserted = 0;
	private int categoriesSkipped  = 0;
	private int booksInserted      = 0;
	private int booksSkipped       = 0;

	void categoryInserted() { categoriesInserted++; }
	void categorySkipped()  { categoriesSkipped++;  }
	void bookInserted()     { booksInserted++;      }
	void bookSkipped()      { booksSkipped++;       }

	public int getCategoriesInserted() { return categoriesInserted; }
	public int getCategoriesSkipped()  { return categoriesSkipped;  }
	public int getBooksInserted()      { return booksInserted;      }
	public int getBooksSkipped()       { return booksSkipped;       }

	public String summary() {
	    return String.format(
		    "Import summary -> Categories: %d inserted, %d already existing | " +
			    "Books: %d inserted, %d skipped (already existing or invalid)",
		    categoriesInserted, categoriesSkipped,
		    booksInserted, booksSkipped
	    );
	}

	@Override
	public String toString() {
	    return summary();
	}
    }
}