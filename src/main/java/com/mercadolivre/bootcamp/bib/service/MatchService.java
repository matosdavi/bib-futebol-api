package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectConfrontResponseDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectResponseDTO;
import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.entity.Match;
import com.mercadolivre.bootcamp.bib.exception.BusinessRuleException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import com.mercadolivre.bootcamp.bib.repository.ClubRepository;
import com.mercadolivre.bootcamp.bib.repository.MatchRepository;
import com.mercadolivre.bootcamp.bib.repository.MatchSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ClubRepository clubRepository;
    private final StadiumService stadiumService;

    @Transactional
    public Match createMatch(Match match) {

        if (match.getHomeClubId().getId().equals(match.getAwayClubId().getId())) {
            throw new BusinessRuleException("Home club and away club cannot be the same.");
        }

        clubRepository.findById(match.getHomeClubId().getId())
                .filter(Club::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Home club not found with ID: " + match.getHomeClubId().getId()));
        clubRepository.findById(match.getAwayClubId().getId())
                .filter(Club::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Away club not found with ID: " + match.getAwayClubId().getId()));
        stadiumService.findById(match.getStadiumId().getId());

        if (match.getHomeClubGoals() < 0 || match.getAwayClubGoals() < 0) {
            throw new BusinessRuleException("Goals cannot be negative.");
        }

        return matchRepository.save(match);
    }

    @Transactional(readOnly = true)
    public Page<Match> findAll(UUID clubId, UUID stadiumId, Pageable pageable) {
        Specification<Match> spec = Specification
                .where(MatchSpecification.hasClub(clubId))
                .and(MatchSpecification.hasStadium(stadiumId));
        return matchRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Match findById(UUID id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Match> findBlowouts() {
        return matchRepository.findBlowouts();
    }

    @Transactional(readOnly = true)
    public RetrospectResponseDTO generalRetrospectCalculate(UUID clubId) {

        Club club = clubRepository.findById(clubId)
                .filter(Club::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with ID: " + clubId));

        List<Match> matches = matchRepository.findByHomeClubIdOrAwayClubId(clubId, clubId);

        long victories = 0;
        long losses = 0;
        long draws = 0;
        long goalsScored = 0;
        long goalsConceded = 0;

        for (Match match : matches) {

            boolean isHomeClub = match.getHomeClubId().getId().equals(clubId);

            int homeGoals = isHomeClub ? match.getHomeClubGoals() : match.getAwayClubGoals();
            int awayGoals = isHomeClub ? match.getAwayClubGoals() : match.getHomeClubGoals();

            goalsScored += homeGoals;
            goalsConceded += awayGoals;

            if (homeGoals > awayGoals) {
                victories++;
            } else if (homeGoals < awayGoals) {
                losses++;
            } else {
                draws++;
            }
        }

        long goalDifference = goalsScored - goalsConceded;

        return new RetrospectResponseDTO(
                club.getId(),
                club.getName(),
                victories,
                draws,
                losses,
                goalsScored,
                goalsConceded,
                goalDifference
        );
    }

    @Transactional(readOnly = true)
    public RetrospectConfrontResponseDTO confrontRetrospectCalculate(UUID clubId, UUID adversaryClubId) {

        if (clubId.equals(adversaryClubId)) {
            throw new BusinessRuleException("Club ID and adversary club ID cannot be the same.");
        }

        Club club = clubRepository.findById(clubId)
                .filter(Club::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with ID: " + clubId));

        Club adversaryClub = clubRepository.findById(adversaryClubId)
                .filter(Club::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Adversary club not found with ID: " + adversaryClubId));

        List<Match> matches = matchRepository.findByBothClubs(clubId, adversaryClubId);

        long victories = 0;
        long losses = 0;
        long draws = 0;
        long goalsScored = 0;
        long goalsConceded = 0;

        for (Match match : matches) {

            boolean isHomeClub = match.getHomeClubId().getId().equals(clubId);

            int homeGoals = isHomeClub ? match.getHomeClubGoals() : match.getAwayClubGoals();
            int awayGoals = isHomeClub ? match.getAwayClubGoals() : match.getHomeClubGoals();

            goalsScored += homeGoals;
            goalsConceded += awayGoals;

            if (homeGoals > awayGoals) {
                victories++;
            } else if (homeGoals < awayGoals) {
                losses++;
            } else {
                draws++;
            }
        }

        long goalDifference = goalsScored - goalsConceded;
        long totalMatches = matches.size();

        return new RetrospectConfrontResponseDTO(
                clubId,
                club.getName(),
                adversaryClubId,
                adversaryClub.getName(),
                victories,
                draws,
                losses,
                goalsScored,
                goalsConceded,
                goalDifference,
                totalMatches
        );
    }

    @Transactional
    public void deleteMatch(UUID id) {
        Match match = findById(id);
        matchRepository.delete(match);
    }
}