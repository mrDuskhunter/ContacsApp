package ru.duskhunter.contacsapp.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;

import java.util.Optional;

@Repository
public interface ContactOwnerRepo extends JpaRepository<ContactOwner,Long> {
    Optional<String> findEmailById(Long ownerId);
    Optional<String> findTelephoneById(Long ownerId);
    Optional<ContactOwner> findByEmail(String email);
    Optional<ContactOwner> findByTelephone(String email);
}
