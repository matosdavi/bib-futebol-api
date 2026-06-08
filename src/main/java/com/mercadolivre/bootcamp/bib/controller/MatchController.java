package com.mercadolivre.bootcamp.bib.controller;

import com.mercadolivre.bootcamp.bib.controller.dto.request.MatchRequestDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.MatchResponseDTO;
import com.mercadolivre.bootcamp.bib.entity.Match;
import com.mercadolivre.bootcamp.bib.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<MatchResponseDTO> create(@RequestBody @Valid MatchRequestDTO dto) {

        Match savedMatch = matchService.createMatch(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(MatchResponseDTO.from(savedMatch));
    }

    @GetMapping
    public ResponseEntity<List<MatchResponseDTO>> findAll(
            @RequestParam(required = false) UUID clubId,
            @RequestParam(required = false) UUID stadiumId,
            Pageable pageable) {

        return ResponseEntity.ok(matchService.findAll(clubId, stadiumId, pageable).map(MatchResponseDTO::from).getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(MatchResponseDTO.from(matchService.findById(id)));
    }

    @GetMapping("/blowouts")
    public ResponseEntity<List<MatchResponseDTO>> findBlowouts(Pageable pageable) {
        return ResponseEntity.ok(matchService.findBlowouts(pageable).map(MatchResponseDTO::from).getContent());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid MatchRequestDTO dto) {
        Match updatedMatch = matchService.updateMatch(id, dto.toEntity());
        return ResponseEntity.ok(MatchResponseDTO.from(updatedMatch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        matchService.deleteMatch(id);
        return ResponseEntity.noContent().build();
    }
}
