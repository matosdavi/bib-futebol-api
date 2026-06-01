package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;

    @Transactional
    public Club createClub(Club club) {

        clubRepository.findByNameIgnoreCase(club.getName())
                .ifPresent(existingClub -> {
                    throw new IllegalArgumentException("Club with name '" + club.getName() + "' already exists.");
                });

        club.setActive(true);
        return clubRepository.save(club);
    }

    @Transactional(readOnly = true)
    public List<Club> findAllActive() {
        return clubRepository.findByActive(true);
    }

    @Transactional(readOnly = true)
    public Club findById(UUID id) {
        return clubRepository.findById(id)
                .filter(Club::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Club not found or inactive with ID: " + id));
    }

    @Transactional
    public void deleteClub(UUID id) {
        Club club = findById(id);
        club.setActive(false);
        clubRepository.save(club);
    }
}