package com.mercadolivre.bootcamp.bib.controller.dto.response;

import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;

import java.util.UUID;

public record StadiumResponseDTO(UUID id, String name, String city, StateEnum state) {
    public static StadiumResponseDTO from(Stadium stadium) {
        return new StadiumResponseDTO(stadium.getId(), stadium.getName(), stadium.getCity(), stadium.getState());
    }
}
