package com.mercadolivre.bootcamp.bib.controller.dto.request;

import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StadiumRequestDTO(
        @NotBlank(message = "Stadium name is required.")
        @Size(min = 3, max = 100, message = "Stadium name must be between 3 and 100 characters.")
        String name,

        @NotBlank(message = "City is required.")
        @Size(min = 2, max = 50, message = "City name must be between 2 and 50 characters.")
        String city,

        @NotNull(message = "State is required.")
        StateEnum state) {

    public Stadium toEntity() {
        Stadium stadium = new Stadium();
        stadium.setName(this.name());
        stadium.setCity(this.city());
        stadium.setState(this.state());
        return stadium;
    }
}
