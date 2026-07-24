package it.comune.biblioteca.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_books_serial", columnNames = "serial_code")
	})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "serial_code", nullable = false)
    private String serialCode;

    @Column(nullable = false)
    private String isbn;

    private String author;
    private String publisher;

    @ManyToMany
    @JoinTable(
	    name = "book_categories",
	    joinColumns = @JoinColumn(name = "book_id"),
	    inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSerialCode() { return serialCode; }
    public void setSerialCode(String serialCode) { this.serialCode = serialCode; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Set<Category> getCategories() { return categories; }
    public void setCategories(Set<Category> categories) { this.categories = categories; }
}
