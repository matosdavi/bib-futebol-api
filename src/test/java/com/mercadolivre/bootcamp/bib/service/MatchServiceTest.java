package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectConfrontResponseDTO;
import com.mercadolivre.bootcamp.bib.controller.dto.response.RetrospectResponseDTO;
import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.entity.Match;
import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.exception.BusinessRuleException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import com.mercadolivre.bootcamp.bib.repository.ClubRepository;
import com.mercadolivre.bootcamp.bib.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private StadiumService stadiumService;

    @InjectMocks
    private MatchService matchService;

    private UUID homeClubId;
    private UUID awayClubId;
    private UUID stadiumId;
    private UUID matchId;
    private Club activeHomeClub;
    private Club activeAwayClub;
    private Stadium activeStadium;
    private Match persistedMatch;

    @BeforeEach
    void setUp() {
        homeClubId = UUID.randomUUID();
        awayClubId = UUID.randomUUID();
        stadiumId = UUID.randomUUID();
        matchId = UUID.randomUUID();

        activeHomeClub = Club.builder().id(homeClubId).name("Flamengo").state(StateEnum.RJ).active(true).build();
        activeAwayClub = Club.builder().id(awayClubId).name("Vasco").state(StateEnum.RJ).active(true).build();
        activeStadium = Stadium.builder().id(stadiumId).name("Maracanã").city("Rio de Janeiro").state(StateEnum.RJ).active(true).build();

        persistedMatch = Match.builder()
                .id(matchId)
                .homeClubId(activeHomeClub)
                .awayClubId(activeAwayClub)
                .stadiumId(activeStadium)
                .homeClubGoals(2)
                .awayClubGoals(1)
                .matchDateTime(LocalDateTime.now())
                .build();
    }

    private Match buildMatchInput(UUID homeId, UUID awayId, UUID stadId, int homeGoals, int awayGoals) {
        return Match.builder()
                .homeClubId(Club.builder().id(homeId).build())
                .awayClubId(Club.builder().id(awayId).build())
                .stadiumId(Stadium.builder().id(stadId).build())
                .homeClubGoals(homeGoals)
                .awayClubGoals(awayGoals)
                .matchDateTime(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // createMatch
    // -------------------------------------------------------------------------

    @Test
    void createMatch_whenValid_savesAndReturnsMatch() {
        Match input = buildMatchInput(homeClubId, awayClubId, stadiumId, 2, 1);
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(activeHomeClub));
        when(clubRepository.findById(awayClubId)).thenReturn(Optional.of(activeAwayClub));
        when(stadiumService.findById(stadiumId)).thenReturn(activeStadium);
        when(matchRepository.save(input)).thenReturn(persistedMatch);

        Match result = matchService.createMatch(input);

        assertEquals(persistedMatch, result);
        verify(matchRepository).save(input);
    }

    @Test
    void createMatch_whenHomeAndAwayClubAreTheSame_throwsBusinessRuleException() {
        Match input = buildMatchInput(homeClubId, homeClubId, stadiumId, 2, 1);

        assertThrows(BusinessRuleException.class, () -> matchService.createMatch(input));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createMatch_whenHomeClubNotFound_throwsResourceNotFoundException() {
        Match input = buildMatchInput(homeClubId, awayClubId, stadiumId, 2, 1);
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matchService.createMatch(input));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createMatch_whenHomeClubIsInactive_throwsResourceNotFoundException() {
        Club inactiveClub = Club.builder().id(homeClubId).name("Flamengo").active(false).build();
        Match input = buildMatchInput(homeClubId, awayClubId, stadiumId, 2, 1);
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(inactiveClub));

        assertThrows(ResourceNotFoundException.class, () -> matchService.createMatch(input));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createMatch_whenAwayClubNotFound_throwsResourceNotFoundException() {
        Match input = buildMatchInput(homeClubId, awayClubId, stadiumId, 2, 1);
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(activeHomeClub));
        when(clubRepository.findById(awayClubId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matchService.createMatch(input));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createMatch_whenStadiumNotFound_throwsResourceNotFoundException() {
        Match input = buildMatchInput(homeClubId, awayClubId, stadiumId, 2, 1);
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(activeHomeClub));
        when(clubRepository.findById(awayClubId)).thenReturn(Optional.of(activeAwayClub));
        when(stadiumService.findById(stadiumId))
                .thenThrow(new ResourceNotFoundException("Stadium not found with ID: " + stadiumId));

        assertThrows(ResourceNotFoundException.class, () -> matchService.createMatch(input));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createMatch_whenHomeClubGoalsAreNegative_throwsBusinessRuleException() {
        Match input = buildMatchInput(homeClubId, awayClubId, stadiumId, -1, 1);
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(activeHomeClub));
        when(clubRepository.findById(awayClubId)).thenReturn(Optional.of(activeAwayClub));
        when(stadiumService.findById(stadiumId)).thenReturn(activeStadium);

        assertThrows(BusinessRuleException.class, () -> matchService.createMatch(input));
        verify(matchRepository, never()).save(any());
    }

    @Test
    void createMatch_whenAwayClubGoalsAreNegative_throwsBusinessRuleException() {
        Match input = buildMatchInput(homeClubId, awayClubId, stadiumId, 1, -1);
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(activeHomeClub));
        when(clubRepository.findById(awayClubId)).thenReturn(Optional.of(activeAwayClub));
        when(stadiumService.findById(stadiumId)).thenReturn(activeStadium);

        assertThrows(BusinessRuleException.class, () -> matchService.createMatch(input));
        verify(matchRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    @SuppressWarnings("unchecked")
    void findAll_returnsPageOfMatches() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> expected = new PageImpl<>(List.of(persistedMatch));
        when(matchRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expected);

        Page<Match> result = matchService.findAll(null, null, pageable);

        assertEquals(expected, result);
        verify(matchRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // -------------------------------------------------------------------------
    // findBlowouts
    // -------------------------------------------------------------------------

    @Test
    void findBlowouts_returnsPageOfMatches() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Match> expected = new PageImpl<>(List.of(persistedMatch));
        when(matchRepository.findBlowouts(any(Pageable.class))).thenReturn(expected);

        Page<Match> result = matchService.findBlowouts(pageable);

        assertEquals(expected, result);
        verify(matchRepository).findBlowouts(pageable);
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Test
    void findById_whenMatchExists_returnsMatch() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(persistedMatch));

        Match result = matchService.findById(matchId);

        assertEquals(persistedMatch, result);
    }

    @Test
    void findById_whenMatchDoesNotExist_throwsResourceNotFoundException() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matchService.findById(matchId));
    }

    // -------------------------------------------------------------------------
    // deleteMatch
    // -------------------------------------------------------------------------

    @Test
    void deleteMatch_whenMatchExists_deletesMatch() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.of(persistedMatch));

        matchService.deleteMatch(matchId);

        verify(matchRepository).delete(persistedMatch);
    }

    @Test
    void deleteMatch_whenMatchDoesNotExist_throwsResourceNotFoundException() {
        when(matchRepository.findById(matchId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matchService.deleteMatch(matchId));
    }

    // -------------------------------------------------------------------------
    // generalRetrospectCalculate
    // -------------------------------------------------------------------------

    @Test
    void generalRetrospectCalculate_whenClubNotFound_throwsResourceNotFoundException() {
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> matchService.generalRetrospectCalculate(homeClubId));
    }

    @Test
    void generalRetrospectCalculate_whenMatchesExist_returnsCorrectStats() {
        // m1: club is home, wins 2-0 → victory, scored 2, conceded 0
        Match m1 = Match.builder()
                .homeClubId(activeHomeClub).awayClubId(activeAwayClub)
                .homeClubGoals(2).awayClubGoals(0).build();
        // m2: club is away, wins 3-0 → victory, scored 3, conceded 0
        Match m2 = Match.builder()
                .homeClubId(activeAwayClub).awayClubId(activeHomeClub)
                .homeClubGoals(0).awayClubGoals(3).build();
        // m3: club is home, draws 1-1 → draw, scored 1, conceded 1
        Match m3 = Match.builder()
                .homeClubId(activeHomeClub).awayClubId(activeAwayClub)
                .homeClubGoals(1).awayClubGoals(1).build();
        // m4: club is home, loses 0-2 → loss, scored 0, conceded 2
        Match m4 = Match.builder()
                .homeClubId(activeHomeClub).awayClubId(activeAwayClub)
                .homeClubGoals(0).awayClubGoals(2).build();

        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(activeHomeClub));
        when(matchRepository.findByHomeClubIdOrAwayClubId(homeClubId, homeClubId))
                .thenReturn(List.of(m1, m2, m3, m4));

        RetrospectResponseDTO result = matchService.generalRetrospectCalculate(homeClubId);

        assertAll(
                () -> assertEquals(homeClubId, result.clubId()),
                () -> assertEquals("Flamengo", result.clubName()),
                () -> assertEquals(2, result.victories()),
                () -> assertEquals(1, result.draws()),
                () -> assertEquals(1, result.losses()),
                () -> assertEquals(6, result.goalsScored()),
                () -> assertEquals(3, result.goalsConceded()),
                () -> assertEquals(3, result.goalDifference())
        );
    }

    @Test
    void generalRetrospectCalculate_whenNoMatchesPlayed_returnsAllZeros() {
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(activeHomeClub));
        when(matchRepository.findByHomeClubIdOrAwayClubId(homeClubId, homeClubId))
                .thenReturn(List.of());

        RetrospectResponseDTO result = matchService.generalRetrospectCalculate(homeClubId);

        assertAll(
                () -> assertEquals(0, result.victories()),
                () -> assertEquals(0, result.draws()),
                () -> assertEquals(0, result.losses()),
                () -> assertEquals(0, result.goalsScored()),
                () -> assertEquals(0, result.goalsConceded()),
                () -> assertEquals(0, result.goalDifference())
        );
    }

    // -------------------------------------------------------------------------
    // confrontRetrospectCalculate
    // -------------------------------------------------------------------------

    @Test
    void confrontRetrospectCalculate_whenSameClubIds_throwsBusinessRuleException() {
        assertThrows(BusinessRuleException.class,
                () -> matchService.confrontRetrospectCalculate(homeClubId, homeClubId));
    }

    @Test
    void confrontRetrospectCalculate_whenClubNotFound_throwsResourceNotFoundException() {
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> matchService.confrontRetrospectCalculate(homeClubId, awayClubId));
    }

    @Test
    void confrontRetrospectCalculate_whenAdversaryNotFound_throwsResourceNotFoundException() {
        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(activeHomeClub));
        when(clubRepository.findById(awayClubId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> matchService.confrontRetrospectCalculate(homeClubId, awayClubId));
    }

    @Test
    void confrontRetrospectCalculate_whenMatchesExist_returnsCorrectStats() {
        // m1: club is home, wins 2-0 → victory, scored 2, conceded 0
        Match m1 = Match.builder()
                .homeClubId(activeHomeClub).awayClubId(activeAwayClub)
                .homeClubGoals(2).awayClubGoals(0).build();
        // m2: club is away, wins 3-1 → victory, scored 3, conceded 1
        Match m2 = Match.builder()
                .homeClubId(activeAwayClub).awayClubId(activeHomeClub)
                .homeClubGoals(1).awayClubGoals(3).build();
        // m3: club is home, draws 1-1 → draw, scored 1, conceded 1
        Match m3 = Match.builder()
                .homeClubId(activeHomeClub).awayClubId(activeAwayClub)
                .homeClubGoals(1).awayClubGoals(1).build();

        when(clubRepository.findById(homeClubId)).thenReturn(Optional.of(activeHomeClub));
        when(clubRepository.findById(awayClubId)).thenReturn(Optional.of(activeAwayClub));
        when(matchRepository.findByBothClubs(homeClubId, awayClubId)).thenReturn(List.of(m1, m2, m3));

        RetrospectConfrontResponseDTO result = matchService.confrontRetrospectCalculate(homeClubId, awayClubId);

        assertAll(
                () -> assertEquals(homeClubId, result.clubId()),
                () -> assertEquals("Flamengo", result.clubName()),
                () -> assertEquals(awayClubId, result.adversaryClubId()),
                () -> assertEquals("Vasco", result.adversaryClubName()),
                () -> assertEquals(2, result.victories()),
                () -> assertEquals(1, result.draws()),
                () -> assertEquals(0, result.losses()),
                () -> assertEquals(6, result.goalsScored()),
                () -> assertEquals(2, result.goalsConceded()),
                () -> assertEquals(4, result.goalDifference()),
                () -> assertEquals(3, result.totalMatches())
        );
    }
}
