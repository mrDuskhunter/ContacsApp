package ru.duskhunter.contacsapp.dto.contact;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContactDto extends ContactCreateDto {
    @NotEmpty(message = "ID cannot be empty")
    private Long id;
}