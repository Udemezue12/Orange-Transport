package com.astrotech.transport.entities;


import com.astrotech.transport.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "registration_number", nullable = false, unique = true)
    private String registrationNumber;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "brand", nullable = false)
    private VehicleBrand brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuelType", nullable = false)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false)
    private TransmissionType transmission;
    @Enumerated(EnumType.STRING)
    @Column(name = "image_upload_status", nullable = false)
    private ImageUploadStatus uploadStatus;
    @Column(name = "manufacture_year", nullable = false)
    private Integer manufactureYear;

    @Column(name = "color", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleColor color;
    @Column(name = "chassis_number", nullable = false)
    private String chassisNumber;
    @Column(name = "engine_number", nullable = false)
    private String engineNumber;

    @Column(name = "vin", unique = true)
    private String vin;

    @Column(name = "model", nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_class", nullable = false)
    private VehicleClass vehicleClass;



    @Column(name = "thumb_nail_url")
    @Builder.Default
    private String thumbNailUrl = null;


    @OneToMany(
            mappedBy = "vehicle",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @OrderBy("rowNumber ASC, seatNumber ASC")
    @Builder.Default
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(
            mappedBy = "vehicle",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<VehicleImages> images = new ArrayList<>();
    @OneToMany(
            mappedBy = "vehicle",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Trip> trips = new ArrayList<>();

}
