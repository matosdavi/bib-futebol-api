package com.mercadolivre.bootcamp.bib.controller.dto.request;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.entity.Match;
import com.mercadolivre.bootcamp.bib.entity.Stadium;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatchRequestDTO(
        @NotNull(message = "Home club ID is required.")
        UUID homeClubId,

        @NotNull(message = "Away club ID is required.")
        UUID awayClubId,

        @NotNull(message = "Home club goals must be provided.")
        @PositiveOrZero(message = "Home club goals cannot be negative.")
        Integer homeClubGoals,

        @NotNull(message = "Away club goals must be provided.")
        @PositiveOrZero(message = "Away club goals cannot be negative.")
        Integer awayClubGoals,

        @NotNull(message = "Stadium ID is required.")
        UUID stadiumId,

        @NotNull(message = "Match date and time is required.")
        @PastOrPresent(message = "Match date and time cannot be in the future.")
        LocalDateTime matchDateTime
) {

    public Match toEntity() {
        Club homeClub = new Club();
        homeClub.setId(this.homeClubId());

        Club awayClub = new Club();
        awayClub.setId(this.awayClubId());

        Stadium stadium = new Stadium();
        stadium.setId(this.stadiumId());

        Match match = new Match();
        match.setHomeClubId(homeClub);
        match.setAwayClubId(awayClub);
        match.setStadiumId(stadium);
        match.setHomeClubGoals(this.homeClubGoals());
        match.setAwayClubGoals(this.awayClubGoals());
        match.setMatchDateTime(this.matchDateTime());

        return match;
    }
}
