package ru.duskhunter.contacsapp.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import ru.duskhunter.contacsapp.model.Role;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "contact_owners")
public class ContactOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotNull(message = "Birthday cannot be empty")
    @Past(message = "Birthday must be in the past")
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Setter
    @Column(name = "email", nullable = false, unique = true, length = 50)
    @NotNull(message = "Email cannot be empty")
    @Pattern(regexp = "^(?=.{1,50}$)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,50}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,50}[a-zA-Z0-9])?)$", message = "Incorrect email")
    private String email;

    @Column(name = "telephone", nullable = false, length = 12)
    @NotNull(message = "Telephone cannot be empty")
    @Pattern(regexp = "\\+7[( ]?\\d{3}[) -]?\\d{3}[- ]?\\d{2}[- ]?\\d{2}", message = "Incorrect tel.number")
    private String telephone;

    /*
        (?=.*[a-z]) — гарантирует наличие хотя бы одной строчной буквы;
        (?=.*[A-Z]) — гарантирует наличие хотя бы одной заглавной буквы;
        (?=.*\d) — гарантирует наличие хотя бы одной цифры;
        (?=.*[!@#$%^&*]) — гарантирует наличие хотя бы одного специального символа;
        .{8,} — минимальная длина пароля (8 символов);
     */
    @Setter
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\"!@#$%^&*_-]).{8,50}", message = "Incorrect password")
    @Column(name = "password", nullable = false, length = 60)
    @NotNull(message = "Password cannot be empty")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @NotNull(message = "Role cannot be empty")
    private Role role;
}