package com.astrotech.transport.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "passenger_manifests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerManifest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    private Ticket ticket;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "boarding_point", nullable = false)
    private String boardingPoint;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "next_of_kin_name", nullable = false)
    private String nextOfKinName;

    @Column(name = "next_of_kin_phone", nullable = false)
    private String nextOfKinPhone;
}
