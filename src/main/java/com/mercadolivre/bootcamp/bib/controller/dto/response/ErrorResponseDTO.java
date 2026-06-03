package com.mercadolivre.bootcamp.bib.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        List<String> messages,
        String path
) {
}
