package com.mercadolivre.bootcamp.bib.controller.dto.response;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;

import java.time.LocalDate;
import java.util.UUID;

public record ClubResponseDTO(UUID id, String name, StateEnum state, LocalDate foundationDate, boolean active) {
    public static ClubResponseDTO from(Club club) {
        return new ClubResponseDTO(
                club.getId(),
                club.getName(),
                club.getState(),
                club.getFoundationDate(),
                club.isActive()
        );
    }
}
