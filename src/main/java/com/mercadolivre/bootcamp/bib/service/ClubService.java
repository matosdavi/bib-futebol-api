package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import com.mercadolivre.bootcamp.bib.exception.ConflictException;
import com.mercadolivre.bootcamp.bib.exception.ResourceNotFoundException;
import com.mercadolivre.bootcamp.bib.repository.ClubRepository;
import com.mercadolivre.bootcamp.bib.repository.ClubSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    @Transactional
    public Club createClub(Club club) {

        clubRepository.findByNameIgnoreCase(club.getName())
                .ifPresent(existingClub -> {
                    throw new ConflictException("Club with name '" + club.getName() + "' already exists.");
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
    public void deleteClub(UUID id) {
        Club club = findById(id);
        club.setActive(false);
        clubRepository.save(club);
    }
}