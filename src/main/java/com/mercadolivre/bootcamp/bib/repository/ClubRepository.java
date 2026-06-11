package com.mercadolivre.bootcamp.bib.repository;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClubRepository extends JpaRepository<Club, UUID>, JpaSpecificationExecutor<Club> {

    Optional<Club> findByNameIgnoreCaseAndState(String name, StateEnum state);

    @Query(value = """
            SELECT c.* FROM club c
            WHERE c.active = true
            ORDER BY (
                SELECT COALESCE(SUM(
                    CASE
                        WHEN m.home_club_id = c.id AND m.home_club_goals > m.away_club_goals THEN 3
                        WHEN m.home_club_id = c.id AND m.home_club_goals = m.away_club_goals THEN 1
                        WHEN m.away_club_id = c.id AND m.away_club_goals > m.home_club_goals THEN 3
                        WHEN m.away_club_id = c.id AND m.away_club_goals = m.home_club_goals THEN 1
                        ELSE 0
                    END
                ), 0) FROM `match` m
                WHERE m.home_club_id = c.id OR m.away_club_id = c.id
            ) DESC
            """,
            countQuery = "SELECT COUNT(*) FROM club WHERE active = true",
            nativeQuery = true)
    Page<Club> findRankingByPoints(Pageable pageable);

    @Query(value = """
            SELECT c.* FROM club c
            WHERE c.active = true
            ORDER BY (
                SELECT COALESCE(SUM(
                    CASE
                        WHEN m.home_club_id = c.id THEN m.home_club_goals
                        WHEN m.away_club_id = c.id THEN m.away_club_goals
                        ELSE 0
                    END
                ), 0) FROM `match` m
                WHERE m.home_club_id = c.id OR m.away_club_id = c.id
            ) DESC
            """,
            countQuery = "SELECT COUNT(*) FROM club WHERE active = true",
            nativeQuery = true)
    Page<Club> findRankingByGoals(Pageable pageable);
}