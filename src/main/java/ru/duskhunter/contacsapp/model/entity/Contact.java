package ru.duskhunter.contacsapp.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    private Long id;
    private String firstName, lastName, telephone, email;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(telephone, contact.telephone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(telephone);
    }
}
