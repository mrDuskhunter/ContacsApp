package ru.duskhunter.contacsapp.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.duskhunter.contacsapp.model.entity.Contact;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepo extends JpaRepository<Contact, Long> {
    Optional<Contact> findByOwnerIdAndEmail(Long ownerId, String email);
    Optional<Contact> findByOwnerIdAndTelephone(Long ownerId, String email);

    @Query("SELECT co.telephone FROM Contact co WHERE co.id = :contactId")
    Optional<String> findTelephoneById(@Param("contactId") long id);

    @Query("SELECT co.email FROM Contact co WHERE co.id = :contactId")
    Optional<String> findEmailById(@Param("contactId") long id);

    @Query("SELECT co FROM Contact co WHERE co.owner.id = :ownerId")
    List<Contact> findAllByOwnerId(@Param("ownerId") long id);
}