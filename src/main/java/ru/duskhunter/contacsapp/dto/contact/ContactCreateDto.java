package ru.duskhunter.contacsapp.dto.contact;

import jakarta.validation.constraints.*;
import lombok.*;
import ru.duskhunter.contacsapp.common.util.EmailNormalizer;
import ru.duskhunter.contacsapp.common.util.PhoneNormalizer;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContactCreateDto {
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

    @NotNull(message = "Owner cannot be null")
    private ContactOwner owner;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactCreateDto that = (ContactCreateDto) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(PhoneNormalizer.normalize(telephone), PhoneNormalizer.normalize(that.telephone)) && Objects.equals(EmailNormalizer.normalize(email), EmailNormalizer.normalize(that.email));
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, telephone, email);
    }
}