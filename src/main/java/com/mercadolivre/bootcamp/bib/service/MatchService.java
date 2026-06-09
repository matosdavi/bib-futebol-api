package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.controller.dto.response.MatchResponseDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectConfrontResponseDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectResponseDTO;
import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.entity.Match;
import com.mercadolivre.bootcamp.bib.exception.BusinessRuleException;
import com.mercadolivre.bootcamp.bib.exception.ConflictException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
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
                .orElseThrow(() -> new ConflictException("Home club not found with ID: " + match.getHomeClubId().getId()));
        clubRepository.findById(match.getAwayClubId().getId())
                .filter(Club::isActive)
                .orElseThrow(() -> new ConflictException("Away club not found with ID: " + match.getAwayClubId().getId()));
        clubRepository.findById(match.getHomeClubId().getId())
                .filter(club -> club.getFoundationDate().isBefore(ChronoLocalDate.from(match.getMatchDateTime())))
                .orElseThrow(() -> new ConflictException("Home club must be founded before the match date."));
        clubRepository.findById(match.getAwayClubId().getId())
                .filter(club -> club.getFoundationDate().isBefore(ChronoLocalDate.from(match.getMatchDateTime())))
                .orElseThrow(() -> new ConflictException("Away club must be founded before the match date."));
        stadiumService.findById(match.getStadiumId().getId());

        if (match.getHomeClubGoals() < 0 || match.getAwayClubGoals() < 0) {
            throw new BusinessRuleException("Goals cannot be negative.");
        }

        validateClubSchedule(match.getHomeClubId().getId(), match.getMatchDateTime(), null);
        validateClubSchedule(match.getAwayClubId().getId(), match.getMatchDateTime(), null);
        validateStadiumAvailability(match.getStadiumId().getId(), match.getMatchDateTime(), null);

        return matchRepository.save(match);
    }

    @Transactional
    public Match updateMatch(UUID id, Match updatedMatch) {
        Match existingMatch = findById(id);

        if (updatedMatch.getHomeClubId().getId().equals(updatedMatch.getAwayClubId().getId())) {
            throw new BusinessRuleException("Home club and away club cannot be the same.");
        }

        clubRepository.findById(updatedMatch.getHomeClubId().getId())
                .filter(Club::isActive)
                .orElseThrow(() -> new ConflictException("Home club not found with ID: " + updatedMatch.getHomeClubId().getId()));
        clubRepository.findById(updatedMatch.getAwayClubId().getId())
                .filter(Club::isActive)
                .orElseThrow(() -> new ConflictException("Away club not found with ID: " + updatedMatch.getAwayClubId().getId()));
        clubRepository.findById(updatedMatch.getHomeClubId().getId())
                .filter(club -> club.getFoundationDate().isBefore(ChronoLocalDate.from(updatedMatch.getMatchDateTime())))
                .orElseThrow(() -> new ConflictException("Home club must be founded before the match date."));
        clubRepository.findById(updatedMatch.getAwayClubId().getId())
                .filter(club -> club.getFoundationDate().isBefore(ChronoLocalDate.from(updatedMatch.getMatchDateTime())))
                .orElseThrow(() -> new ConflictException("Away club must be founded before the match date."));
        stadiumService.findById(updatedMatch.getStadiumId().getId());

        if (updatedMatch.getHomeClubGoals() < 0 || updatedMatch.getAwayClubGoals() < 0) {
            throw new BusinessRuleException("Goals cannot be negative.");
        }

        validateClubSchedule(updatedMatch.getHomeClubId().getId(), updatedMatch.getMatchDateTime(), id);
        validateClubSchedule(updatedMatch.getAwayClubId().getId(), updatedMatch.getMatchDateTime(), id);
        validateStadiumAvailability(updatedMatch.getStadiumId().getId(), updatedMatch.getMatchDateTime(), id);

        existingMatch.setHomeClubId(updatedMatch.getHomeClubId());
        existingMatch.setAwayClubId(updatedMatch.getAwayClubId());
        existingMatch.setStadiumId(updatedMatch.getStadiumId());
        existingMatch.setMatchDateTime(updatedMatch.getMatchDateTime());
        existingMatch.setHomeClubGoals(updatedMatch.getHomeClubGoals());
        existingMatch.setAwayClubGoals(updatedMatch.getAwayClubGoals());

        return matchRepository.save(existingMatch);
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
    public Page<Match> findBlowouts(Pageable pageable) {
        return matchRepository.findBlowouts(pageable);
    }

    @Transactional(readOnly = true)
    public RetrospectResponseDTO generalRetrospectCalculate(UUID clubId) {

        Club club = clubRepository.findById(clubId)
                .filter(Club::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with ID: " + clubId));

        List<Match> matches = matchRepository.findByHomeClubIdOrAwayClubId(clubId);

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
                totalMatches,
                matches.stream().map(MatchResponseDTO::from).toList()
        );
    }

    @Transactional
    public void deleteMatch(UUID id) {
        Match match = findById(id);
        matchRepository.delete(match);
    }

    private void validateClubSchedule(UUID clubId, LocalDateTime matchDateTime, UUID excludeMatchId) {
        List<Match> clashes = matchRepository.findClubMatchesInWindow(
                clubId,
                matchDateTime.minusHours(48),
                matchDateTime.plusHours(48)
        );
        boolean hasClash = clashes.stream()
                .anyMatch(m -> !m.getId().equals(excludeMatchId));
        if (hasClash) {
            throw new BusinessRuleException("Club already has a match scheduled within 48 hours of this date.");
        }
    }

    private void validateStadiumAvailability(UUID stadiumId, LocalDateTime matchDateTime, UUID excludeMatchId) {
        LocalDate matchDate = matchDateTime.toLocalDate();
        List<Match> occupied = matchRepository.findByStadiumAndDay(
                stadiumId,
                matchDate.atStartOfDay(),
                matchDate.plusDays(1).atStartOfDay()
        );
        boolean hasConflict = occupied.stream()
                .anyMatch(m -> excludeMatchId == null || !m.getId().equals(excludeMatchId));
        if (hasConflict) {
            throw new BusinessRuleException("Stadium already has a match scheduled on this day.");
        }
    }
}