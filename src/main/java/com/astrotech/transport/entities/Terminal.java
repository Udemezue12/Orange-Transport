package com.astrotech.transport.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "terminals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "name" ,nullable = false)
    private String name;
    @Column(name = "city" , nullable = false)
    private String city;
    @Column(name = "address" , nullable = false)
    private String address;
    @Column(name = "state" , nullable = false)
    private String state;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_supervisor_id", referencedColumnName = "id", nullable = false, unique = true)
    private User terminalSupervisor;


}
