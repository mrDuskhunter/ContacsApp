package ru.duskhunter.contacsapp.dto;

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
public class ContactDto {
    @NotEmpty(message = "ID cannot be empty")
    Long id;

    @NotEmpty(message = "FirstName cannot be empty")
    @Size(min = 2, max = 50, message = "FirstName min size: 2, max size: 50")
    private String firstName;

    @NotEmpty(message = "LastName cannot be empty")
    @Size(min = 2, max = 50, message = "LastName min size: 2, max size: 50")
    private String lastName;

    @NotEmpty(message = "Telephone cannot be empty")
    @Pattern(regexp = "\\+7[( ]?\\d{3}[) -]?\\d{3}[- ]?\\d{2}[- ]?\\d{2}", message = "Incorrect tel.number")
    private String telephone;

    @Email(message = "Incorrect email")
    @Size(min = 2, max = 50, message = "Email max size: 50")
    private String email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactDto that = (ContactDto) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(telephone, that.telephone) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, telephone, email);
    }
}