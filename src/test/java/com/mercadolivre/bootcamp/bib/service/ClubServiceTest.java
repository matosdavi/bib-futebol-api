package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.exception.ConflictException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import com.mercadolivre.bootcamp.bib.repository.ClubRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubService clubService;

    private UUID clubId;
    private Club activeClub;

    @BeforeEach
    void setUp() {
        clubId = UUID.randomUUID();
        activeClub = Club.builder()
                .id(clubId)
                .name("Flamengo")
                .state(StateEnum.RJ)
                .foundationDate(LocalDate.of(1895, 11, 17))
                .active(true)
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_returnsPageOfClubs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Club> expected = new PageImpl<>(List.of(activeClub));
        when(clubRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expected);

        Page<Club> result = clubService.findAll(null, null, null, pageable);

        assertEquals(expected, result);
        verify(clubRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void createClub_whenNameIsUnique_savesWithActiveTrue() {
        Club input = Club.builder().name("Flamengo").state(StateEnum.RJ).build();
        when(clubRepository.findByNameIgnoreCase("Flamengo")).thenReturn(Optional.empty());
        when(clubRepository.save(any(Club.class))).thenAnswer(inv -> inv.getArgument(0));

        Club result = clubService.createClub(input);

        assertTrue(result.isActive());
        verify(clubRepository).save(input);
    }

    @Test
    void createClub_whenNameAlreadyExists_throwsConflictException() {
        Club input = Club.builder().name("Flamengo").state(StateEnum.RJ).build();
        when(clubRepository.findByNameIgnoreCase("Flamengo")).thenReturn(Optional.of(activeClub));

        assertThrows(ConflictException.class, () -> clubService.createClub(input));
        verify(clubRepository, never()).save(any());
    }

    @Test
    void findById_whenClubExistsAndIsActive_returnsClub() {
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(activeClub));

        Club result = clubService.findById(clubId);

        assertEquals(activeClub, result);
    }

    @Test
    void findById_whenClubDoesNotExist_throwsResourceNotFoundException() {
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clubService.findById(clubId));
    }

    @Test
    void findById_whenClubIsInactive_throwsResourceNotFoundException() {
        Club inactiveClub = Club.builder()
                .id(clubId).name("Flamengo").state(StateEnum.RJ).active(false).build();
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(inactiveClub));

        assertThrows(ResourceNotFoundException.class, () -> clubService.findById(clubId));
    }

    @Test
    void deleteClub_whenClubExists_setsInactiveAndSaves() {
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(activeClub));

        clubService.deleteClub(clubId);

        assertFalse(activeClub.isActive());
        verify(clubRepository).save(activeClub);
    }

    @Test
    void deleteClub_whenClubDoesNotExist_throwsResourceNotFoundException() {
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clubService.deleteClub(clubId));
    }
}
