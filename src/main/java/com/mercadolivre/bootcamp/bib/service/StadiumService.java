package com.mercadolivre.bootcamp.bib.service;

import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.repository.StadiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StadiumService {

    private final StadiumRepository stadiumRepository;

    @Transactional
    public Stadium createStadium(Stadium stadium) {

        stadiumRepository.findByNameIgnoreCaseAndCityIgnoreCaseAndState(
                stadium.getName(),
                stadium.getCity(),
                stadium.getState()
        ).ifPresent(existingStadium -> {
            throw new IllegalArgumentException("Stadium with name '" + stadium.getName() + "' already exists in " +
                    stadium.getCity() + ", " + stadium.getState());
        });

        stadium.setActive(true);
        return stadiumRepository.save(stadium);
    }

    @Transactional(readOnly = true)
    public Page<Stadium> findAll(Pageable pageable) {
        return stadiumRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Stadium findById(UUID id) {

        return stadiumRepository.findById(id)
                .filter(Stadium::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Stadium not found with ID: " + id));
    }

    @Transactional
    public void deleteStadium(UUID id) {

        Stadium stadium = findById(id);
        stadium.setActive(false);
        stadiumRepository.save(stadium);
    }
}