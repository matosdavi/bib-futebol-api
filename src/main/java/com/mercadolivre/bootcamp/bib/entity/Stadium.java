package com.mercadolivre.bootcamp.bib.entity;

import com.mercadolivre.bootcamp.bib.enums.StateEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Table(name = "stadium", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "city"})
})
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stadium {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private StateEnum state;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = false;
}
