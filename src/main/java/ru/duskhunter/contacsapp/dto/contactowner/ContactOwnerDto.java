package ru.duskhunter.contacsapp.dto.contactowner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.duskhunter.contacsapp.model.Role;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ContactOwnerDto {
    @NotEmpty(message = "ID cannot be empty")
    private long id;

    @NotEmpty(message = "Username cannot be empty")
    @Size(min = 2, max = 50, message = "Username min size: 2, max size: 50")
    private String username;

    @NotNull(message = "Description cannot be null")
    @Size(max = 50, message = "Description max size: 150")
    private String description;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Incorrect email")
    @Size(min = 2, max = 50, message = "Email max size: 50")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    @Size(min = 5, max = 50, message = "Username min size: 5, max size: 50")
    private String password;

    @NotEmpty(message = "Role cannot be empty")
    private Role role;
}