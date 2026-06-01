package com.mercadolivre.bootcamp.bib.repository;

import com.mercadolivre.bootcamp.bib.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClubRepository extends JpaRepository<Club, UUID>, JpaSpecificationExecutor<Club> {

    Optional<Club> findByNameIgnoreCase(String name);
}