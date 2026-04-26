package ru.duskhunter.contacsapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.duskhunter.contacsapp.model.entity.Contact;
import ru.duskhunter.contacsapp.model.entity.ServerResponse;
import ru.duskhunter.contacsapp.service.ContactService;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping("/get")
    public ServerResponse<List<Contact>> getContacts() {
        return contactService.getContacts();
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ServerResponse<Contact>> getContactById(@PathVariable int id) {
        return contactService.getContactById(id);
    }

    @PostMapping("/create")
    public ServerResponse<Contact> createContact(@RequestBody Contact contact){
        return contactService.createContact(contact);
    }
}
