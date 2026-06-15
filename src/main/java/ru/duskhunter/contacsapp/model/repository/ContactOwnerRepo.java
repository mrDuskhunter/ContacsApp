package ru.duskhunter.contacsapp.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;

@Repository
public interface ContactOwnerRepo extends JpaRepository<ContactOwner,Long> {
}
