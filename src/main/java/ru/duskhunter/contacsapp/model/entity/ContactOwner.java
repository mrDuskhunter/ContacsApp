package ru.duskhunter.contacsapp.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.duskhunter.contacsapp.model.Role;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contact_owners")
public class ContactOwner {
    private Long id;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @Setter
    private String description;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "incorrect email")
    @Size(min = 2, max = 50, message = "email max size: 50")
    private String email;

    @JsonIgnore
    @Setter
    private String password;

    @Enumerated(EnumType.STRING)
    @Setter
    @NotNull(message = "Role cannot be empty")
    private Role role;

    private List<Contact> contacts = new ArrayList<>();
}
