package it.comune.biblioteca.service;

import it.comune.biblioteca.entity.Book;
import it.comune.biblioteca.entity.Category;
import it.comune.biblioteca.enums.ExceptionCodeEnum;
import it.comune.biblioteca.exception.PersistenceException;
import it.comune.biblioteca.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository books) {
	this.bookRepository = books;
    }

    public Long count() {
	try {
	    return bookRepository.count();
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_001, e.getMessage(), e);
	}
    }

    public int countBooksByCategory(Category category) {
	return bookRepository.countBooksByCategories(new HashSet<>(List.of(category)));
    }

    public List<Book> findAll() {
	return bookRepository.findAll();
    }

    public void save(Book book, boolean isFromFile) {
	try {
	    validateBook(book, isFromFile);
	    book.setSerialCode(normalizeSerialCode(book.getSerialCode()));
	    book.setName(normalize(book.getName()));
	    book.setAuthor(normalize(book.getAuthor()));
	    book.setPublisher(normalize(book.getPublisher()));
	    book.setDescription(normalize(book.getDescription()));
	    bookRepository.save(book);
	} catch (PersistenceException pe) {
	    throw pe;
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_001, e.getMessage(), e);
	}
    }

    public void delete(Book book) {
	try {
	    bookRepository.delete(book);
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_001, e.getMessage(), e);
	}
    }

    public boolean existsBySerialCodeIgnoreCase(String serialCode) {
	try {
	    return bookRepository.existsBySerialCodeIgnoreCase(serialCode);
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_001, e.getMessage(), e);
	}
    }

    public String normalizeSerialCode(String value) {
	try {
	    Objects.requireNonNull(value, "Value cannot be null");
	    return value.trim().toUpperCase().replaceAll("\\s+", "");
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_001, e.getMessage(), e);
	}
    }

    public Page<Book> advancedSearch(String query, Set<Category> categories, Pageable pageable) {
	try {
	    String cleanQuery = (query == null || query.trim().isEmpty()) ? null : query.trim().replaceAll("\\s+", " ");
	    List<Long> categoryIds = null;
	    if (categories != null && !categories.isEmpty()) {
		categoryIds = categories.stream()
			.map(Category::getId)
			.toList();
	    }

	    return bookRepository.advancedSearch(cleanQuery, categoryIds, pageable);
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_001, e.getMessage(), e);
	}
    }

    public Book findByIdWithCategories(Long id) {
	return bookRepository.findByIdWithCategories(id)
		.orElseThrow(() -> new PersistenceException(ExceptionCodeEnum.G_001, "Book not found"));
    }

    private void validateBook(Book book, boolean isFromFile) {
	if(StringUtils.isBlank(book.getName())) {
	    throw new PersistenceException(ExceptionCodeEnum.B_005);
	}
	if(StringUtils.isBlank(book.getSerialCode())) {
	    throw new PersistenceException(ExceptionCodeEnum.B_003);
	}
	if(book.getId() == null && bookRepository.existsBySerialCodeIgnoreCase(book.getSerialCode())) {
	    throw new PersistenceException(ExceptionCodeEnum.B_001);
	}
	if(!isFromFile && book.getId() == null && bookRepository.existsByIsbnIgnoreCase(book.getIsbn())) {
	    throw new PersistenceException(ExceptionCodeEnum.B_002);
	}
	if(!book.getSerialCode().matches("^[a-zA-Z]+[0-9]+$")) {
	    throw new PersistenceException(ExceptionCodeEnum.B_004);
	}
    }

    private String normalize(String value) {
	if(StringUtils.isBlank(value)) return Strings.EMPTY;
	return value.trim().replaceAll("\\s+", " ");
    }
}
