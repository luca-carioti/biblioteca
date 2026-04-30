package it.comune.biblioteca.repository;

import it.comune.biblioteca.entity.Category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByIdAndBooksIsNotEmpty(Long id);
    List<Category> findByBooksId(Long id);
}
