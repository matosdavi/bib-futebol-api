package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectResponseDTO;
import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.entity.Match;
import com.mercadolivre.bootcamp.bib.repository.ClubRepository;
import com.mercadolivre.bootcamp.bib.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ClubRepository clubRepository;

    public RetrospectResponseDTO generalRetrospectCalculate(UUID clubId) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found with ID: " + clubId));

        List<Match> matches = matchRepository.findByHomeClubIdOrAwayClubId(clubId, clubId);

        long victories = 0;
        long loses = 0;
        long draws = 0;
        long goalsScored = 0;
        long goalsConceded = 0;

        for (Match match : matches) {

            boolean isHomeClub = match.getHomeClub().getId().equals(clubId);

            int homeGoals = isHomeClub ? match.getHomeClubGoals() : match.getAwayClubGoals();
            int awayGoals = isHomeClub ? match.getAwayClubGoals() : match.getHomeClubGoals();

            goalsScored += homeGoals;
            goalsConceded += awayGoals;

            if (homeGoals > awayGoals) {
                victories++;
            } else if (homeGoals < awayGoals) {
                loses++;
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
                loses,
                goalsScored,
                goalsConceded,
                goalDifference
        );
    }
}