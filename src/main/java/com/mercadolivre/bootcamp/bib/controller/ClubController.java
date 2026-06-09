package com.mercadolivre.bootcamp.bib.controller;

import com.mercadolivre.bootcamp.bib.controller.dto.request.ClubRequestDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.ClubRankingResponseDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.ClubResponseDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.PageResponse;
import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectConfrontResponseDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectResponseDTO;
import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.service.ClubService;
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
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;
    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<ClubResponseDTO> create(@RequestBody @Valid ClubRequestDTO dto) {

        Club savedClub = clubService.createClub(dto.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClubResponseDTO.from(savedClub));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ClubResponseDTO>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) StateEnum state,
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {

        return ResponseEntity.ok(PageResponse.from(clubService.findAll(name, state, active, pageable).map(ClubResponseDTO::from)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ClubResponseDTO.from(clubService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClubResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid ClubRequestDTO dto) {
        Club updatedClub = clubService.updateClub(id, dto.toEntity());
        return ResponseEntity.ok(ClubResponseDTO.from(updatedClub));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clubService.deleteClub(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/retrospect")
    public ResponseEntity<RetrospectResponseDTO> retrospect(@PathVariable UUID id) {
        return ResponseEntity.ok(matchService.generalRetrospectCalculate(id));
    }

    @GetMapping("/{id}/retrospect/{adversaryId}")
    public ResponseEntity<RetrospectConfrontResponseDTO> confrontRetrospect(
            @PathVariable UUID id,
            @PathVariable UUID adversaryId) {
        return ResponseEntity.ok(matchService.confrontRetrospectCalculate(id, adversaryId));
    }

    @GetMapping("/ranking/points")
    public ResponseEntity<List<ClubRankingResponseDTO>> rankingByPoints(Pageable pageable) {
        return ResponseEntity.ok(clubService.rankingByPoints(pageable));
    }

    @GetMapping("/ranking/goals")
    public ResponseEntity<List<ClubRankingResponseDTO>> rankingByGoals(Pageable pageable) {
        return ResponseEntity.ok(clubService.rankingByGoals(pageable));
    }
}
