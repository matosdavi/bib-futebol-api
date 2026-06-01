package com.mercadolivre.bootcamp.bib.repository;

import com.mercadolivre.bootcamp.bib.entity.Match;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class MatchSpecification {

    public static Specification<Match> hasClub(UUID clubId) {
        return (root, query, cb) -> clubId == null ? null
                : cb.or(
                    cb.equal(root.get("homeClub").get("id"), clubId),
                    cb.equal(root.get("awayClub").get("id"), clubId)
                );
    }

    public static Specification<Match> hasStadium(UUID stadiumId) {
        return (root, query, cb) -> stadiumId == null ? null
                : cb.equal(root.get("stadium").get("id"), stadiumId);
    }
}
