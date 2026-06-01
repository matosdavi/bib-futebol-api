package com.mercadolivre.bootcamp.bib.repository;

import com.mercadolivre.bootcamp.bib.entity.Club;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import org.springframework.data.jpa.domain.Specification;

public class ClubSpecification {

    public static Specification<Club> hasName(String name) {
        return (root, query, cb) -> name == null ? null
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Club> hasState(StateEnum state) {
        return (root, query, cb) -> state == null ? null
                : cb.equal(root.get("state"), state);
    }

    public static Specification<Club> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null
                : cb.equal(root.get("active"), active);
    }
}
