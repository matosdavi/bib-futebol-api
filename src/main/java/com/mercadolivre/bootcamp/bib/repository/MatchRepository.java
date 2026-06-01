package com.mercadolivre.bootcamp.bib.repository;

import com.mercadolivre.bootcamp.bib.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {

    List<Match> findByHomeClubId(UUID homeClubId);

    List<Match> findByAwayClubId(UUID awayClubId);

    List<Match> findByHomeClubIdOrAwayClubId(UUID homeClubId, UUID awayClubId);

    @Query("SELECT m FROM Match m WHERE ABS(m.homeClubGoals - m.awayClubGoals) >= 3")
    List<Match> findBlowouts();
}
