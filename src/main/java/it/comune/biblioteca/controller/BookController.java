package it.comune.biblioteca.controller;

import it.comune.biblioteca.service.BookFileService;
import it.comune.biblioteca.service.BookFileService.ImportReport;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/book-api")
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
	    "application/vnd.ms-excel",                                           // .xls
	    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"  // .xlsx
    );

    private final BookFileService bookFileService;
    private final Tika tika = new Tika();

    public BookController(BookFileService bookFileService) {
	this.bookFileService = bookFileService;
    }

    @PostMapping(value = "/loadBook", consumes = "multipart/form-data")
    public ResponseEntity<ImportReportResponse> loadBook(
	    @RequestParam("file") MultipartFile file,
	    @AuthenticationPrincipal UserDetails userDetails) {

	if (file.isEmpty()) {
	    return ResponseEntity.badRequest().build();
	}

	if (!isExcelFile(file)) {
	    log.warn("User '{}' uploaded a non-Excel file: '{}'",
		    userDetails.getUsername(), file.getOriginalFilename());
	    return ResponseEntity.status(415).build(); // 415 Unsupported Media Type
	}

	String filename = file.getOriginalFilename();
	String user = userDetails.getUsername();
	log.info("User '{}' is importing file '{}'", user, filename);

	ImportReport report = bookFileService.importBooks(file);

	return ResponseEntity.ok(ImportReportResponse.from(report, filename, user));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Validates the file type using two complementary checks:
     * <ol>
     *   <li>Extension — fast, first line of defence.</li>
     *   <li>Magic bytes via Apache Tika — prevents spoofing a PDF/ZIP/etc.
     *       by simply renaming it to .xlsx.</li>
     * </ol>
     */
    private boolean isExcelFile(MultipartFile file) {
	String filename = file.getOriginalFilename();
	if (filename == null) return false;

	String lower = filename.toLowerCase();
	if (!lower.endsWith(".xls") && !lower.endsWith(".xlsx")) return false;

	try {
	    String detectedMime = tika.detect(file.getInputStream(), filename);
	    return ALLOWED_MIME_TYPES.contains(detectedMime);
	} catch (IOException e) {
	    log.error("Could not read file input stream for MIME detection", e);
	    return false;
	}
    }

    // -------------------------------------------------------------------------
    // Response DTO
    // -------------------------------------------------------------------------

    /**
     * JSON response body for the import endpoint.
     * Kept as a static nested class to stay self-contained in the controller layer;
     * move to a dedicated dto package if the project grows.
     */
    public record ImportReportResponse(
	    String file,
	    String importedBy,
	    int categoriesInserted,
	    int categoriesAlreadyExisting,
	    int booksInserted,
	    int booksSkipped
    ) {
	static ImportReportResponse from(ImportReport report, String filename, String user) {
	    return new ImportReportResponse(
		    filename,
		    user,
		    report.getCategoriesInserted(),
		    report.getCategoriesSkipped(),
		    report.getBooksInserted(),
		    report.getBooksSkipped()
	    );
	}
    }
}