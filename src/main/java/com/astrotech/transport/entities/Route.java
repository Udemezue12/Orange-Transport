package com.astrotech.transport.entities;



import jakarta.persistence.*;
import lombok.*;


import java.util.*;

@Entity
@Table(
        name = "routes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_route_origin_destination",
                        columnNames = {"origin_terminal_id", "destination_terminal_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_terminal_id", nullable = false)
    private Terminal originTerminal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_terminal_id", nullable = false)
    private Terminal destinationTerminal;

    @Column(name = "distance_km")
    @Builder.Default
    private Double distanceKm = null;

    @Column(name = "estimated_duration_minutes")
    @Builder.Default
    private Integer estimatedDurationMinutes = null;


    @OneToMany(
            mappedBy = "route",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )

    @Builder.Default
    private List<RouteFare> fares = new ArrayList<>();



}
