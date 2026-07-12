package ru.duskhunter.contacsapp.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.duskhunter.contacsapp.common.util.EmailNormalizer;
import ru.duskhunter.contacsapp.common.util.PhoneNormalizer;
import ru.duskhunter.contacsapp.model.Role;

import java.time.LocalDate;

@Setter
@Entity
@Table(name = "contact_owners")
@Getter
@NoArgsConstructor
@SuperBuilder
public class ContactOwner extends BaseEntity {
    @Column(name = "username", nullable = false, length = 50)
    @NotBlank(message = "Username cannot be empty")
    @Size(max = 50, message = "Username max size: 50")
    private String username;

    @NotNull(message = "Birthday cannot be empty")
    @Past(message = "Birthday must be in the past")
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    @NotNull(message = "Email cannot be empty")
    @Pattern(regexp = "^(?=.{1,50}$)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,50}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,50}[a-zA-Z0-9])?)$", message = "Incorrect email")
    private String email;

    @Column(name = "telephone", nullable = false, unique = true, length = 12)
    @NotNull(message = "Telephone cannot be empty")
//    @Pattern(regexp = "\\+7[( ]?\\d{3}[) -]?\\d{3}[- ]?\\d{2}[- ]?\\d{2}", message = "Incorrect tel.number")
    @Pattern(regexp = "\\+7\\d{10}", message = "Incorrect tel.number")
    private String telephone;

    /*
        (?=.*[a-z]) — гарантирует наличие хотя бы одной строчной буквы;
        (?=.*[A-Z]) — гарантирует наличие хотя бы одной заглавной буквы;
        (?=.*\d) — гарантирует наличие хотя бы одной цифры;
        (?=.*[!@#$%^&*]) — гарантирует наличие хотя бы одного специального символа;
        .{8,} — минимальная длина пароля (8 символов);
     */
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\"!@#$%^&*_-]).{8,50}", message = "Incorrect password")
    @Column(name = "password", nullable = false, length = 60)
    @NotNull(message = "Password cannot be empty")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @NotNull(message = "Role cannot be empty")
    private Role role;

    @PrePersist
    @PreUpdate
    private void normalizeFields() {
        telephone = PhoneNormalizer.normalize(telephone);
        email = EmailNormalizer.normalize(email);
    }
}