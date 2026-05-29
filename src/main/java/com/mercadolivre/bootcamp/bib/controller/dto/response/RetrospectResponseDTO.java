package com.mercadolivre.bootcamp.bib.controller.dto.response;

import java.util.UUID;

public record RetrospectResponseDTO(
        UUID clubId,
        String clubName,
        long victories,
        long draws,
        long losses,
        long goalsScored,
        long goalsConceded,
        long goalDifference
) {
}
