package com.mercadolivre.bootcamp.bib.repository;

import com.mercadolivre.bootcamp.bib.entity.Stadium;
import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StadiumRepository extends JpaRepository<Stadium, UUID>, JpaSpecificationExecutor<Stadium> {

    List<Stadium> findByActive(boolean active);

    List<Stadium> findByNameIgnoreCase(String name);

    List<Stadium> findByCityIgnoreCase(String city);

    List<Stadium> findByState(StateEnum state);

    Optional<Stadium> findByNameIgnoreCaseAndCityIgnoreCaseAndState(String name, String city, StateEnum state);
}
