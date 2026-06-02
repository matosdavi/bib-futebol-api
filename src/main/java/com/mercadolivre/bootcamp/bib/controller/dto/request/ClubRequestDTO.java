package com.mercadolivre.bootcamp.bib.controller.dto.request;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClubRequestDTO(
        @NotBlank(message = "Club name is required.")
        @Size(min = 2, max = 50, message = "Club name must be between 2 and 50 characters.")
        String name,

        @NotNull(message = "State is required.")
        StateEnum state
) {
    public Club toEntity() {
        Club club = new Club();
        club.setName(this.name());
        club.setState(this.state());
        return club;
    }
}