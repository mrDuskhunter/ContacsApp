package ru.duskhunter.contacsapp.model.entity;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ContactsDAO {
    private final List<Contact> contacts;

    public ContactsDAO() {
        contacts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            contacts.add(new Contact((long) i, "Name" + i, "lastName" + i, "+123456789" + i, "email" + i + "@mail.ru"));
        }
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void addContact(Contact contact) {
        contacts.add(contact);
    }
}