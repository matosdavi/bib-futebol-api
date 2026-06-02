package com.mercadolivre.bootcamp.bib.controller.dto.response;

import com.mercadolivre.bootcamp.bib.entity.Match;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatchResponseDTO(UUID id, ClubResponseDTO homeClub, ClubResponseDTO awayClub,
                               Integer homeClubGoals, Integer awayClubGoals,
                               StadiumResponseDTO stadium, LocalDateTime matchDateTime) {
    public static MatchResponseDTO from(Match match) {
        return new MatchResponseDTO(
                match.getId(),
                ClubResponseDTO.from(match.getHomeClubId()),
                ClubResponseDTO.from(match.getAwayClubId()),
                match.getHomeClubGoals(),
                match.getAwayClubGoals(),
                StadiumResponseDTO.from(match.getStadiumId()),
                match.getMatchDateTime()
        );
    }
}
