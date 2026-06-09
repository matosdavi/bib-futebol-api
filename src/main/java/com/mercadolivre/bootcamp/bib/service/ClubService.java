package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.controller.dto.response.ClubRankingResponseDTO;
import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.entity.Match;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.exception.ConflictException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import com.mercadolivre.bootcamp.bib.repository.ClubRepository;
import com.mercadolivre.bootcamp.bib.repository.ClubSpecification;
import com.mercadolivre.bootcamp.bib.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final MatchRepository matchRepository;

    @Transactional
    public Club createClub(Club club) {

        clubRepository.findByNameIgnoreCaseAndState(
                        club.getName(),
                        club.getState()
                )
                .ifPresent(existingClub -> {
                    throw new ConflictException("Club with name '" + club.getName() + "' already exists in " + club.getState());
                });

        club.setActive(true);
        return clubRepository.save(club);
    }

    @Transactional(readOnly = true)
    public Page<Club> findAll(String name, StateEnum state, Boolean active, Pageable pageable) {
        Specification<Club> spec = Specification
                .where(ClubSpecification.hasName(name))
                .and(ClubSpecification.hasState(state))
                .and(ClubSpecification.isActive(active));
        return clubRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Club findById(UUID id) {
        return clubRepository.findById(id)
                .filter(Club::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found or inactive with ID: " + id));
    }

    @Transactional
    public Club updateClub(UUID id, Club updatedClub) {

        Club existingClub = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with ID: " + id));

        if (!existingClub.getName().equalsIgnoreCase(updatedClub.getName())) {
            clubRepository.findByNameIgnoreCaseAndState(
                    updatedClub.getName(),
                    updatedClub.getState()
            ).ifPresent(clubWithSameName -> {
                throw new ConflictException("Another club with name '" + updatedClub.getName() + "' already exists in " + updatedClub.getState());
            });
        }

        existingClub.setName(updatedClub.getName());
        existingClub.setFoundationDate(updatedClub.getFoundationDate());
        existingClub.setState(updatedClub.getState());
        existingClub.setActive(updatedClub.isActive());

        return clubRepository.save(existingClub);
    }

    @Transactional
    public void deleteClub(UUID id) {
        Club club = findById(id);
        club.setActive(false);
        clubRepository.save(club);
    }

    @Transactional(readOnly = true)
    public List<ClubRankingResponseDTO> rankingByPoints(Pageable pageable) {
        return clubRepository.findRankingByPoints(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .map(this::computeClubRanking)
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<ClubRankingResponseDTO> rankingByGoals(Pageable pageable) {
        return clubRepository.findRankingByGoals(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .map(this::computeClubRanking)
                .getContent();
    }

    private ClubRankingResponseDTO computeClubRanking(Club club) {
        List<Match> matches = matchRepository.findByHomeClubIdOrAwayClubId(club.getId());

        long wins = 0, draws = 0, losses = 0, goalsFor = 0, goalsAgainst = 0;

        for (Match match : matches) {
            boolean isHome = match.getHomeClubId().getId().equals(club.getId());
            int myGoals = isHome ? match.getHomeClubGoals() : match.getAwayClubGoals();
            int opponentGoals = isHome ? match.getAwayClubGoals() : match.getHomeClubGoals();
            goalsFor += myGoals;
            goalsAgainst += opponentGoals;
            if (myGoals > opponentGoals) wins++;
            else if (myGoals < opponentGoals) losses++;
            else draws++;
        }

        return new ClubRankingResponseDTO(
                club.getId(), club.getName(),
                matches.size(), wins, draws, losses,
                wins * 3 + draws,
                goalsFor, goalsAgainst, goalsFor - goalsAgainst
        );
    }
}