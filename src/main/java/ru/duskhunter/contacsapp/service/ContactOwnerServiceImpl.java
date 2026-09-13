package ru.duskhunter.contacsapp.service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.duskhunter.contacsapp.common.util.EmailNormalizer;
import ru.duskhunter.contacsapp.common.util.PhoneNormalizer;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.common.util.Validator;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contactowner.ContactCreateOwnerDto;
import ru.duskhunter.contacsapp.dto.contactowner.ContactOwnerDto;
import ru.duskhunter.contacsapp.exception.EntityConflictException;
import ru.duskhunter.contacsapp.exception.InternalServerException;
import ru.duskhunter.contacsapp.exception.NotFoundException;
import ru.duskhunter.contacsapp.exception.ValidationException;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactOwnerRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContactOwnerServiceImpl implements ContactOwnerService {
    private final ContactOwnerRepo ownerRepo;
    private final ModelMapper mapper;
    private final Validator validator;

    @Override
    public ServerResponse<List<ContactOwnerDto>> getOwners() {
        List<ContactOwnerDto> contactOwnerDtos = ownerRepo.findAll()
                .stream()
                .map(contact -> mapper.map(contact, ContactOwnerDto.class))
                .toList();
        return ServerResponseHelper.response(true, contactOwnerDtos, HttpStatus.OK, List.of());
    }

    @Override
    public ServerResponse<ContactOwnerDto> getOwnerById(long ownerId) {
        Optional<ContactOwner> contactOwnerOptional = ownerRepo.findById(ownerId);

        return contactOwnerOptional.map(contact -> ServerResponseHelper.response(true, mapper.map(contact, ContactOwnerDto.class), HttpStatus.OK, List.of()))
                .orElseThrow(() -> new NotFoundException(String.format("The owner with id %d does not exist", ownerId), null));
    }

    @Transactional
    @Override
    public ServerResponse<ContactOwnerDto> createOwner(ContactCreateOwnerDto contactCreateOwnerDto) {
        ContactOwner owner = mapper.map(contactCreateOwnerDto, ContactOwner.class);
        owner.setTelephone(PhoneNormalizer.normalize(owner.getTelephone()));
        owner.setEmail(EmailNormalizer.normalize(owner.getEmail()));

        List<String> entityErrors = validator.validate(owner);

        if (!entityErrors.isEmpty()) {
            throw new ValidationException(entityErrors, mapper.map(owner, ContactOwnerDto.class));
        }

        assertNotTaken(
                ownerRepo.findByEmail(owner.getEmail()).isPresent(),
                "User with this email already exist",
                owner
        );

        assertNotTaken(
                ownerRepo.findByTelephone(owner.getTelephone()).isPresent(),
                "User with this telephone already exist",
                owner
        );

        try {
            ContactOwner savedOwner = ownerRepo.saveAndFlush(owner);
            return ServerResponseHelper.response(true, mapper.map(savedOwner, ContactOwnerDto.class), HttpStatus.CREATED, List.of());
        } catch (DataIntegrityViolationException e) {
            throw new EntityConflictException("Error creating owner", mapper.map(owner, ContactOwnerDto.class));
        }
    }

    @Transactional
    @Override
    public ServerResponse<ContactOwnerDto> deleteOwnerById(long ownerId) {
        ContactOwner contact = ownerRepo.findById(ownerId)
                .orElseThrow(() ->
                        new NotFoundException(getMessageForErrorOwnerIdNotExist(ownerId),
                                null));

        ContactOwnerDto contactOwnerDto = mapper.map(contact, ContactOwnerDto.class);

        ownerRepo.deleteById(ownerId);
        if (ownerRepo.findById(ownerId).isPresent()) {
            throw new InternalServerException(
                    String.format("Unexpected server error during deletion operation the owner with id %s", ownerId),
                    contactOwnerDto);
        }

        return ServerResponseHelper.response(true, contactOwnerDto, HttpStatus.OK, List.of());
    }

    @Transactional
    @Override
    public ServerResponse<ContactOwnerDto> updateOwner(ContactOwnerDto contactOwnerDto) {
        Long id = contactOwnerDto.getId();

        if (!ownerRepo.existsById(id)) {
            throw new NotFoundException(getMessageForErrorOwnerIdNotExist(id), contactOwnerDto);
        }

        ContactOwner contactOwner = mapper.map(contactOwnerDto, ContactOwner.class);
        contactOwner.setTelephone(PhoneNormalizer.normalize(contactOwner.getTelephone()));
        contactOwner.setEmail(EmailNormalizer.normalize(contactOwner.getEmail()));

        List<String> entityErrors = validator.validate(contactOwner);

        if (!entityErrors.isEmpty()) {
            throw new ValidationException(entityErrors, contactOwner);
        }

        String existingEmail = ownerRepo.findEmailById(id).orElse(null);

        //equals email
        if (existingEmail != null && !existingEmail.equals(contactOwner.getEmail())) {
            throw new EntityConflictException(
                    "It is forbidden to change the email address. Contact the administrator",
                    contactOwnerDto);
        }

        String existingTelephone = ownerRepo.findTelephoneById(id).orElse(null);

        if (existingTelephone != null && !existingTelephone.equals(contactOwner.getTelephone())) {
            assertNotTaken(
                    ownerRepo.findByTelephone(contactOwner.getTelephone()).isPresent(),
                    "User with this telephone already exist",
                    contactOwner
            );
        }

        try {
            ContactOwner savedContactOwner = ownerRepo.saveAndFlush(contactOwner);
            return ServerResponseHelper.response(true, mapper.map(savedContactOwner, ContactOwnerDto.class), HttpStatus.OK, List.of());
        } catch (DataIntegrityViolationException e) {
            throw new EntityConflictException("Error update owner", mapper.map(contactOwner, ContactOwnerDto.class));
        } catch (ConstraintViolationException cve) {
            List<String> errors = cve.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toList();
            throw new ValidationException(errors, mapper.map(contactOwner, ContactOwnerDto.class));
        }
    }

    @Override
    public ServerResponse<ContactOwnerDto> getOwnerByEmail(String email) {
        Optional<ContactOwner> contactOwnerOptional = ownerRepo.findByEmail(email);

        return contactOwnerOptional.map(contact -> ServerResponseHelper.response(true, mapper.map(contact, ContactOwnerDto.class), HttpStatus.OK, List.of()))
                .orElseThrow(() -> new NotFoundException(String.format("User with email %s does not exist", email), null));
    }

    private void assertNotTaken(boolean alreadyExists, String message, ContactOwner owner) {
        if (alreadyExists) {
            throw new EntityConflictException(message, mapper.map(owner, ContactOwnerDto.class));
        }
    }

    private String getMessageForErrorOwnerIdNotExist(long ownerId) {
        return String.format("The owner with id %d does not exist", ownerId);
    }

    @Override
    public UserDetailsService getUserDetailsService() {
        return new UserDetailsService(){
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                ContactOwner contactOwner = ownerRepo
                        .findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
                return new User(
                                contactOwner.getEmail(),
                                contactOwner.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority(contactOwner.getRole().name()))
                );
            }
        };
    }
}