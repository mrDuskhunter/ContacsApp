package ru.duskhunter.contacsapp.dto.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContactCreateDtoRequest {
    @NotEmpty(message = "First name cannot be empty")
    @Size(min = 2, max = 50, message = "First name min size: 2, max size: 50")
    private String firstName;

    @NotEmpty(message = "Last name cannot be empty")
    @Size(min = 2, max = 50, message = "Last name min size: 2, max size: 50")
    private String lastName;

    @NotEmpty(message = "telephone cannot be empty")
    @Pattern(regexp = "\\+7[( ]?\\d{3}[) -]?\\d{3}[- ]?\\d{2}[- ]?\\d{2}", message = "incorrect tel.number")
    private String telephone;

    @Email(message = "incorrect email")
    @Size(min = 2, max = 50, message = "email max size: 50")
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactCreateDtoRequest that = (ContactCreateDtoRequest) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(telephone, that.telephone) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, telephone, email);
    }
}