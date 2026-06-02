package com.mercadolivre.bootcamp.bib.repository;

import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import org.springframework.data.jpa.domain.Specification;

public class StadiumSpecification {

    public static Specification<Stadium> hasName(String name) {
        return (root, query, cb) -> name == null ? null
                : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Stadium> hasCity(String city) {
        return (root, query, cb) -> city == null ? null
                : cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
    }

    public static Specification<Stadium> hasState(StateEnum state) {
        return (root, query, cb) -> state == null ? null
                : cb.equal(root.get("state"), state);
    }

    public static Specification<Stadium> isActive(Boolean active) {
        return (root, query, cb) -> active == null ? null
                : cb.equal(root.get("active"), active);
    }
}
