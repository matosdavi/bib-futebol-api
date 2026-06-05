package com.mercadolivre.bootcamp.bib.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "match")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Match {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "home_club_id", nullable = false)
    private Club homeClubId;

    @ManyToOne
    @JoinColumn(name = "away_club_id", nullable = false)
    private Club awayClubId;

    @ManyToOne
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadiumId;

    @Column(name = "match_date_time", nullable = false)
    private LocalDateTime matchDateTime;

    @Min(0)
    @Column(name = "home_club_goals", nullable = false)
    private Integer homeClubGoals;

    @Min(0)
    @Column(name = "away_club_goals", nullable = false)
    private Integer awayClubGoals;
}