package ru.duskhunter.contacsapp.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.duskhunter.contacsapp.common.util.EmailNormalizer;
import ru.duskhunter.contacsapp.common.util.PhoneNormalizer;
import ru.duskhunter.contacsapp.common.util.Utils;
import ru.duskhunter.contacsapp.model.Role;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Setter
@Entity
@Table(name = "contact_owners")
@Getter
@NoArgsConstructor
@SuperBuilder
public class ContactOwner extends BaseEntity implements UserDetails {
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
    @Pattern(regexp = Utils.regexEmail , message = "Incorrect email")
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
    @JsonIgnore
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(Role.ROLE_USER.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}