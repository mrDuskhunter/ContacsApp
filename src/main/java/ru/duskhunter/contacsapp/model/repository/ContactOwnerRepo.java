package ru.duskhunter.contacsapp.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;

import java.util.Optional;

@Repository
public interface ContactOwnerRepo extends JpaRepository<ContactOwner,Long> {
    @Query("SELECT co.email FROM ContactOwner co WHERE co.id = :ownerId")
    Optional<String> findEmailById(Long ownerId);

    @Query("SELECT co.telephone FROM ContactOwner co WHERE co.id = :ownerId")
    Optional<String> findTelephoneById(Long ownerId);
    Optional<ContactOwner> findByEmail(String email);
    Optional<ContactOwner> findByTelephone(String telephone);
}