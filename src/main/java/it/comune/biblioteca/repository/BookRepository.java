package it.comune.biblioteca.repository;

import it.comune.biblioteca.entity.Book;
import it.comune.biblioteca.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbnIgnoreCase(String isbn);

    boolean existsBySerialCodeIgnoreCase(String serialCode);

    boolean existsByNameIgnoreCase(String name);

    int countBooksByCategories(Set<Category> categories);

    @Query(value = "SELECT DISTINCT b FROM Book b " +
	    "LEFT JOIN b.categories c " +
	    "WHERE (:query IS NULL OR :query = '' OR (" +
	    "LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
	    "LOWER(b.serialCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
	    "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
	    "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
	    "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))" +
	    ")) AND (:categoryIds IS NULL OR c.id IN :categoryIds)",
	    countQuery = "SELECT COUNT(DISTINCT b) FROM Book b " +
	    "LEFT JOIN b.categories c " +
	    "WHERE (:query IS NULL OR :query = '' OR (" +
	    "LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
	    "LOWER(b.serialCode) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
	    "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
	    "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
	    "LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))" +
	    ")) AND (:categoryIds IS NULL OR c.id IN :categoryIds)")
    Page<Book> advancedSearch(
	    @Param("query") String query,
	    @Param("categoryIds") Collection<Long> categoryIds,
	    Pageable pageable
    );

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.categories WHERE b.id = :id")
    Optional<Book> findByIdWithCategories(@Param("id") Long id);
}
