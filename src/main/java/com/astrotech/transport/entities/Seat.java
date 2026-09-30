package com.astrotech.transport.entities;

import com.astrotech.transport.enums.SeatPosition;
import com.astrotech.transport.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_seat_vehicle_number",
                columnNames = {"vehicle_id", "seat_number"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING)
    @Column(name= "position",nullable = false)
    private SeatPosition position;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status;



    @Column(name = "seat_number", nullable = false)
    private String seatNumber;
    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
//    @OneToMany(mappedBy = "seat")
//    @Builder.Default
//    private List<TripSeatReservation> reservations = new ArrayList<>();
}
