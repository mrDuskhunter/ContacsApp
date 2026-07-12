package ru.duskhunter.contacsapp.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.duskhunter.contacsapp.common.util.EmailNormalizer;
import ru.duskhunter.contacsapp.common.util.PhoneNormalizer;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
@Table(name = "contacts")
public class Contact extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name cannot be empty")
    @Size(max = 50, message = "First name max size: 50")
    private String firstName;

    @Setter
    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 50, message = "Last name max size: 50")
    private String lastName;

    @Setter
    @Column(name = "telephone", nullable = false, unique = true, length = 12)
    @NotEmpty(message = "Telephone cannot be empty")
//    @Pattern(regexp = "\\+7[( ]?\\d{3}[) -]?\\d{3}[- ]?\\d{2}[- ]?\\d{2}", message = "Incorrect tel.number")
    @Pattern(regexp = "\\+7\\d{10}", message = "Incorrect tel.number")
    private String telephone;

    @Setter
    @Column(name = "email", nullable = false, unique = true, length = 50)
    @NotNull(message = "Email cannot be empty")
    @Pattern(regexp = "^(?=.{1,50}$)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,50}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,50}[a-zA-Z0-9])?)$", message = "Incorrect email")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @NotNull(message = "Owner cannot be null")
    private ContactOwner owner;

    @PrePersist
    @PreUpdate
    private void normalizeFields() {
        telephone = PhoneNormalizer.normalize(telephone);
        email = EmailNormalizer.normalize(email);
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