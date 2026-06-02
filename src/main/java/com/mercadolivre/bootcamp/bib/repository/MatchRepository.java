package com.mercadolivre.bootcamp.bib.repository;

import com.mercadolivre.bootcamp.bib.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID>, JpaSpecificationExecutor<Match> {

    List<Match> findByHomeClubIdOrAwayClubId(UUID homeClubId, UUID awayClubId);

    @Query("SELECT m FROM Match m WHERE ABS(m.homeClubGoals - m.awayClubGoals) >= 3")
    Page<Match> findBlowouts();

    @Query("SELECT m FROM Match m WHERE " +
            "(m.homeClubId.id = :clubId AND m.awayClubId.id = :adversaryId) OR " +
            "(m.awayClubId.id = :clubId AND m.homeClubId.id = :adversaryId)")
    List<Match> findByBothClubs(@Param("clubId") UUID clubId, @Param("adversaryId") UUID adversaryId);
}
