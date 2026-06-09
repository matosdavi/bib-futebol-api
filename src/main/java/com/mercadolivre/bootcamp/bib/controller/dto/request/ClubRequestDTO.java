package com.mercadolivre.bootcamp.bib.controller.dto.request;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ClubRequestDTO(
        @NotBlank(message = "Club name is required.")
        @Size(min = 2, max = 50, message = "Club name must be between 2 and 50 characters.")
        String name,

        @NotNull(message = "State is required.")
        StateEnum state,

        @NotNull(message = "Foundation date is required.")
        @PastOrPresent(message = "Foundation date cannot be in the future.")
        LocalDate foundationDate,

        Boolean active
) {
    public Club toEntity() {
        Club club = new Club();
        club.setName(this.name());
        club.setState(this.state());
        club.setFoundationDate(this.foundationDate());
        if (this.active() != null) club.setActive(this.active());
        return club;
    }
}