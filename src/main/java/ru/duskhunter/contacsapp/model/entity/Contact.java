package ru.duskhunter.contacsapp.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "first_name", nullable = false, length = 100)
    @NotEmpty(message = "First name cannot be empty")
    @Size(min = 2, max = 50, message = "First name min size: 2, max size: 50")
    private String firstName;

    @Setter
    @Column(name = "last_name", nullable = false, length = 100)
    @NotEmpty(message = "Last name cannot be empty")
    @Size(min = 2, max = 50, message = "Last name min size: 2, max size: 50")
    private String lastName;

    @Setter
    @Column(name = "telephone", nullable = false, length = 12)
    @NotEmpty(message = "telephone cannot be empty")
    @Pattern(regexp = "\\+7[( ]?\\d{3}[) -]?\\d{3}[- ]?\\d{2}[- ]?\\d{2}", message = "incorrect tel.number")
    private String telephone;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "incorrect email")
    @Size(min = 2, max = 50, message = "email max size: 50")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull(message = "Owner cannot be null")
    private ContactOwner owner;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
    }

    public Contact(String firstName, String lastName, String telephone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.telephone = telephone;
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(telephone, contact.telephone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(telephone);
    }
}