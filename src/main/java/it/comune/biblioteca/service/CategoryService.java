package it.comune.biblioteca.service;

import it.comune.biblioteca.entity.Category;
import it.comune.biblioteca.enums.ExceptionCodeEnum;
import it.comune.biblioteca.exception.PersistenceException;
import it.comune.biblioteca.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
	this.categoryRepository = categoryRepository;
    }

    public Long count() {
	try {
	    return categoryRepository.count();
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_000, e.getMessage(), e);
	}
    }

    public List<Category> findAll() {
	List<Category> categories = new ArrayList<>();
	try {
	    categories = categoryRepository.findAll();
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_000, e.getMessage(), e);
	}
	return categories;
    }

    public Optional<Category> findByName(String name) {
	try {
	    return categoryRepository.findByNameIgnoreCase(name);
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_001, e.getMessage(), e);
	}
    }

    public Category save(Category c) {
	try {
	    if(c.getId() == null && existsByCodeIgnoreCase(c.getName())) {
		throw new PersistenceException(ExceptionCodeEnum.C_001, String.format("Category '%s' already exists", c.getName()));
	    }
	    c.setName(normalizeName(c.getName()));
	    c.setCode(buildCode(c.getName()));
	    return categoryRepository.save(c);
	} catch (PersistenceException pe) {
	    throw pe;
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_000, e.getMessage(), e);
	}
    }

    public void delete(Category c) {
	try {
	    if(categoryRepository.existsByIdAndBooksIsNotEmpty(c.getId())) {
		throw new PersistenceException(ExceptionCodeEnum.C_002, String.format("Category '%s' is associated to a book", c.getName()));
	    }
	    categoryRepository.delete(c);
	} catch (PersistenceException pe) {
	    throw pe;
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_000, e.getMessage(), e);
	}
    }

    public boolean existsByCodeIgnoreCase(String code) {
	try {
	    return categoryRepository.existsByCodeIgnoreCase(buildCode(code));
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_000, e.getMessage(), e);
	}
    }

    public String normalizeName(String name) {
	try {
	    Objects.requireNonNull(name, "Name cannot be null");

	    return name.trim()
		    .replaceAll("\\s+", " ")
		    .toUpperCase();
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_000, e.getMessage(), e);
	}
    }

    public List<Category> findCategoriesNameByBookId(Long bookId) {
	try {
	    return categoryRepository.findByBooksId(bookId).stream()
		    .sorted(Comparator.comparing(Category::getName))
		    .toList();
	} catch (Exception e) {
	    throw new PersistenceException(ExceptionCodeEnum.G_001, e.getMessage(), e);
	}
    }

    private String buildCode(String name) {
	Objects.requireNonNull(name, "Name cannot be null");

	return name.trim()
		.replaceAll("\\s+", "_")
		.toUpperCase();
    }
}
