package com.mercadolivre.bootcamp.bib.controller.dto.response;

import java.util.UUID;

public record ClubRankingResponseDTO(
        UUID clubId,
        String clubName,
        long totalMatches,
        long wins,
        long draws,
        long losses,
        long points,
        long goalsFor,
        long goalsAgainst,
        long goalBalance
) {}
