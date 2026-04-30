package it.comune.biblioteca.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
	name = "categories",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_categories_name", columnNames = "name"),
		@UniqueConstraint(name = "uk_categories_code", columnNames = "code")
	}
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @ManyToMany(mappedBy = "categories")
    private Set<Book> books = new HashSet<>();

    public Long getId() {
	return id;
    }

    public void setId(Long id) {
	this.id = id;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Set<Book> getBooks() {
	return books;
    }

    public void setBooks(Set<Book> books) {
	this.books = books;
    }

    public String getCode() {
	return code;
    }

    public void setCode(String code) {
	this.code = code;
    }
}
