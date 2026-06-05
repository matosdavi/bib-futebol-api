package com.mercadolivre.bootcamp.bib.entity;

import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Table(name = "club",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name"})
        })
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private StateEnum state;

    @Column(name = "foundation_date", nullable = false)
    private LocalDate foundationDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = false;
}