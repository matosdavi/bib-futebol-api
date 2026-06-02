package com.mercadolivre.bootcamp.bib.controller;

import com.mercadolivre.bootcamp.bib.controller.dto.request.StadiumRequestDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.StadiumResponseDTO;
import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.service.StadiumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stadiums")
@RequiredArgsConstructor
public class StadiumController {

    private final StadiumService stadiumService;

    @PostMapping
    public ResponseEntity<StadiumResponseDTO> create(@RequestBody @Valid StadiumRequestDTO dto) {

        Stadium savedStadium = stadiumService.createStadium(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(StadiumResponseDTO.from(savedStadium));
    }

    @GetMapping
    public ResponseEntity<List<StadiumResponseDTO>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) StateEnum state,
            @RequestParam(defaultValue = "true") Boolean active,
            Pageable pageable) {

        return ResponseEntity.ok(stadiumService.findAll(name, city, state, active, pageable).map(StadiumResponseDTO::from).getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StadiumResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(StadiumResponseDTO.from(stadiumService.findById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        stadiumService.deleteStadium(id);
        return ResponseEntity.noContent().build();
    }
}
