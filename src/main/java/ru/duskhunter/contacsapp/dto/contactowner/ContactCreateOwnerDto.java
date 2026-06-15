package ru.duskhunter.contacsapp.dto.contactowner;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ContactCreateOwnerDto extends ContactOwnerDto {
    @NotEmpty(message = "ID cannot be empty")
    private long id;
}