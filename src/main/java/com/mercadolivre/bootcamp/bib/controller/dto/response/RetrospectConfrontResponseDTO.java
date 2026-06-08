package com.mercadolivre.bootcamp.bib.controller.dto.response;

import java.util.List;
import java.util.UUID;

public record RetrospectConfrontResponseDTO(
        UUID clubId,
        String clubName,
        UUID adversaryClubId,
        String adversaryClubName,
        long victories,
        long draws,
        long losses,
        long goalsScored,
        long goalsConceded,
        long goalDifference,
        long totalMatches,
        List<MatchResponseDTO> matches
) {
}