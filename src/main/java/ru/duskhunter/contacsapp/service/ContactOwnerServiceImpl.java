package ru.duskhunter.contacsapp.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.duskhunter.contacsapp.common.util.ServerResponseHelper;
import ru.duskhunter.contacsapp.common.util.Validator;
import ru.duskhunter.contacsapp.dto.ServerResponse;
import ru.duskhunter.contacsapp.dto.contactowner.ContactCreateOwnerDto;
import ru.duskhunter.contacsapp.dto.contactowner.ContactOwnerDto;
import ru.duskhunter.contacsapp.model.entity.ContactOwner;
import ru.duskhunter.contacsapp.model.repository.ContactOwnerRepo;

import java.sql.SQLDataException;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ContactOwnerServiceImpl implements ContactOwnerService {
    private final ContactOwnerRepo ownerRepo;
    private final ModelMapper mapper;

    @Override
    public ServerResponse<List<ContactOwnerDto>> getOwners() {
        List<ContactOwnerDto> contactOwnerDtos = ownerRepo.findAll()
                .stream()
                .map(contact -> mapper.map(contact, ContactOwnerDto.class))
                .toList();
        return ServerResponseHelper.response(true, contactOwnerDtos, HttpStatus.OK, List.of());
    }

    @Override
    public ResponseEntity<ServerResponse<ContactOwnerDto>> getOwnerById(long ownerId) {
        Optional<ContactOwner> contactOwnerOptional = ownerRepo.findById(ownerId);

        return contactOwnerOptional.map(contact -> ServerResponseHelper.responseEntity(true, mapper.map(contact, ContactOwnerDto.class), HttpStatus.OK, List.of()))
                .orElseGet(() -> ServerResponseHelper.responseEntity(false, null,
                        HttpStatus.NO_CONTENT, List.of(String.format("The owner with id %s does not exist", ownerId))));
    }

    @Override
    public ServerResponse<ContactOwnerDto> createOwner(ContactCreateOwnerDto contactCreateOwnerDto) {
        ContactOwner owner = mapper.map(contactCreateOwnerDto, ContactOwner.class);
        List<String> entityErrors = Validator.validate(owner);

        if (!entityErrors.isEmpty()) {
            return ServerResponseHelper.response(false, mapper.map(owner, ContactOwnerDto.class), HttpStatus.BAD_REQUEST,
                    entityErrors);
        }

        try {
            ContactOwner savedOwner = ownerRepo.saveAndFlush(owner);
            return ServerResponseHelper.response(true, mapper.map(savedOwner, ContactOwnerDto.class), HttpStatus.CREATED, List.of());
        } catch (DataIntegrityViolationException e) {
            return ServerResponseHelper.response(false, mapper.map(owner, ContactOwnerDto.class), HttpStatus.CONFLICT,
                    List.of("Error creating owner"));
        }
    }

    @Override
    public ServerResponse<ContactOwnerDto> deleteOwnerById(long ownerId) {
        Optional<ContactOwner> contactOptional = ownerRepo.findById(ownerId);
        if (contactOptional.isEmpty()) {
            return ServerResponseHelper.response(false, null, HttpStatus.NO_CONTENT,
                    List.of(String.format("The owner with id %s does not exist", ownerId)));
        }

        ContactOwnerDto contactOwnerDto = mapper.map(contactOptional.get(), ContactOwnerDto.class);

        try {
            ownerRepo.deleteById(ownerId);
            Optional<ContactOwner> contactOptionalTest = ownerRepo.findById(ownerId);
            if (contactOptionalTest.isPresent()) {
                throw new SQLDataException();
            }
        } catch (Exception e) {
            return ServerResponseHelper.response(false, contactOwnerDto, HttpStatus.INTERNAL_SERVER_ERROR,
                    List.of(String.format("Unexpected server error during deletion operation the owner with id %s", ownerId)));
        }
        return ServerResponseHelper.response(true, contactOwnerDto, HttpStatus.OK, List.of());
    }

    @Override
    public ServerResponse<ContactOwnerDto> updateOwner(ContactOwnerDto contactOwnerDto) {
        Long id = contactOwnerDto.getId();

        if (!matchId(id)) {
            return ServerResponseHelper.response(false, contactOwnerDto, HttpStatus.NO_CONTENT,
                    List.of(String.format("The owner with id %s does not exist", id)));
        }

        ContactOwner contactOwner = mapper.map(contactOwnerDto, ContactOwner.class);

        List<String> entityErrors = Validator.validate(contactOwner);

        if (!entityErrors.isEmpty()) {
            return ServerResponseHelper.response(false, mapper.map(contactOwner, ContactOwnerDto.class), HttpStatus.BAD_REQUEST,
                    entityErrors);
        }

        try {
            ContactOwner savedContactOwner = ownerRepo.saveAndFlush(contactOwner);
            return ServerResponseHelper.response(true, mapper.map(savedContactOwner, ContactOwnerDto.class), HttpStatus.OK, List.of());
        } catch (DataIntegrityViolationException e) {
            return ServerResponseHelper.response(true, mapper.map(contactOwner, ContactOwnerDto.class), HttpStatus.CONFLICT,
                    List.of("Error update owner"));
        }
    }

    private boolean matchId(long contactId) {
        return ownerRepo.findAll().stream().mapToLong(ContactOwner::getId).anyMatch(id -> id == contactId);
    }
}