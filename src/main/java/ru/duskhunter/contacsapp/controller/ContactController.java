package ru.duskhunter.contacsapp.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contact.ContactCreateDto;
import ru.duskhunter.contacsapp.dto.contact.ContactDto;
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
    public ServerResponse<List<ContactDto>> getContacts() {
        return contactService.getContacts();
    }

    @GetMapping("/get/{id}")
    public ServerResponse<ContactDto> getContactById(@PathVariable long id) {
        return contactService.getContactById(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public ServerResponse<ContactDto> createContact(@Valid @RequestBody ContactCreateDto contact) {
        return contactService.createContact(contact);
    }

    @DeleteMapping("/delete/{id}")
    public ServerResponse<ContactDto> deleteContact(@PathVariable long id) {
        return contactService.deleteContactById(id);
    }

    @PutMapping("/update")
    public ServerResponse<ContactDto> updateContact(@RequestBody ContactDto contact) {
        return contactService.updateContact(contact);
    }
}