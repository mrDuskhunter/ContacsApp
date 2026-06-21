package ru.duskhunter.contacsapp.dto.contactowner;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.duskhunter.contacsapp.model.Role;

import java.time.LocalDate;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ContactCreateOwnerDto {
    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 2, max = 50, message = "Username min size: 2, max size: 50")
    private String username;

    @NotNull(message = "Birthday cannot be empty")
    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;

    @NotNull(message = "Email cannot be empty")
    @Pattern(regexp = "^(?=.{1,50}$)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,50}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,50}[a-zA-Z0-9])?)$", message = "Incorrect email")
    private String email;

    @NotNull(message = "Telephone cannot be empty")
    @Pattern(regexp = "\\+7[( ]?\\d{3}[) -]?\\d{3}[- ]?\\d{2}[- ]?\\d{2}", message = "Incorrect tel.number")
    private String telephone;

    @NotNull(message = "Password cannot be empty")
    @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\"!@#$%^&*_-]).{50}", message = "Incorrect password")
    private String password;

    @NotNull(message = "Role cannot be empty")
    private Role role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactCreateOwnerDto that = (ContactCreateOwnerDto) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}