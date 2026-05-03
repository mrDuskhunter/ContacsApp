package ru.duskhunter.contacsapp.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.duskhunter.contacsapp.model.Role;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ContactOwner {
    private UUID id;
    private String username;
    private String description;
    private String email;
    private String password;
    private Role role;

    private List<Contact> contacts = new ArrayList<>();

    public ContactOwner() {
        this.id = UUID.randomUUID();
    }

    public ContactOwner(String username, String description) {
        this();
        this.username = username;
        this.description = description;
    }
}
