package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.exception.ConflictException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import com.mercadolivre.bootcamp.bib.repository.StadiumRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StadiumServiceTest {

    @Mock
    private StadiumRepository stadiumRepository;

    @InjectMocks
    private StadiumService stadiumService;

    private UUID stadiumId;
    private Stadium activeStadium;

    @BeforeEach
    void setUp() {
        stadiumId = UUID.randomUUID();
        activeStadium = Stadium.builder()
                .id(stadiumId)
                .name("Maracanã")
                .city("Rio de Janeiro")
                .state(StateEnum.RJ)
                .active(true)
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAll_returnsPageOfStadiums() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Stadium> expected = new PageImpl<>(List.of(activeStadium));
        when(stadiumRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expected);

        Page<Stadium> result = stadiumService.findAll(null, null, null, null, pageable);

        assertEquals(expected, result);
        verify(stadiumRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void createStadium_whenNameCityStateIsUnique_savesWithActiveTrue() {
        Stadium input = Stadium.builder()
                .name("Maracanã").city("Rio de Janeiro").state(StateEnum.RJ).build();
        when(stadiumRepository.findByNameIgnoreCase("Maracanã")).thenReturn(Optional.empty());
        when(stadiumRepository.save(any(Stadium.class))).thenAnswer(inv -> inv.getArgument(0));

        Stadium result = stadiumService.createStadium(input);

        assertTrue(result.isActive());
        verify(stadiumRepository).save(input);
    }

    @Test
    void createStadium_whenAlreadyExists_throwsConflictException() {
        Stadium input = Stadium.builder()
                .name("Maracanã").city("Rio de Janeiro").state(StateEnum.RJ).build();
        when(stadiumRepository.findByNameIgnoreCase("Maracanã")).thenReturn(Optional.of(activeStadium));

        assertThrows(ConflictException.class, () -> stadiumService.createStadium(input));
        verify(stadiumRepository, never()).save(any());
    }

    @Test
    void findById_whenStadiumExistsAndIsActive_returnsStadium() {
        when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(activeStadium));

        Stadium result = stadiumService.findById(stadiumId);

        assertEquals(activeStadium, result);
    }

    @Test
    void findById_whenStadiumDoesNotExist_throwsResourceNotFoundException() {
        when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stadiumService.findById(stadiumId));
    }

    @Test
    void findById_whenStadiumIsInactive_throwsResourceNotFoundException() {
        Stadium inactiveStadium = Stadium.builder()
                .id(stadiumId).name("Maracanã").city("Rio de Janeiro").state(StateEnum.RJ).active(false).build();
        when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(inactiveStadium));

        assertThrows(ResourceNotFoundException.class, () -> stadiumService.findById(stadiumId));
    }

    @Test
    void deleteStadium_whenStadiumExists_setsInactiveAndSaves() {
        when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.of(activeStadium));

        stadiumService.deleteStadium(stadiumId);

        assertFalse(activeStadium.isActive());
        verify(stadiumRepository).save(activeStadium);
    }

    @Test
    void deleteStadium_whenStadiumDoesNotExist_throwsResourceNotFoundException() {
        when(stadiumRepository.findById(stadiumId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stadiumService.deleteStadium(stadiumId));
    }
}
