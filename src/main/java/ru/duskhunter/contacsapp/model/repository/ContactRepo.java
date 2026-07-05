package ru.duskhunter.contacsapp.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.duskhunter.contacsapp.model.entity.Contact;

import java.util.Optional;

@Repository
public interface ContactRepo extends JpaRepository<Contact, Long> {
    Optional<Contact> findByEmail(String email);
    Optional<Contact> findByTelephone(String email);
    Optional<String> findEmailById(long id);
    Optional<String> findTelephoneById(long id);
}