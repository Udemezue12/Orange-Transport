package com.astrotech.transport.entities;

import com.astrotech.transport.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "assigned_workers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_assigned_workers_worker",
                        columnNames = "worker_user_id"
                ),
                @UniqueConstraint(
                        name = "uk_assigned_workers_entity",
                        columnNames = {"assigned_service_type", "assigned_service_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_assigned_workers_assigned_by",
                        columnList = "assigned_by_user_id"
                ),
                @Index(
                        name = "idx_assigned_workers_worker",
                        columnList = "worker_user_id"
                ),
                @Index(name = "idx_assigned_workers_entity",
                        columnList = "assigned_service_type, assigned_service_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "assigned_by_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_assigned_worker_assigned_by")
    )
    private User assignedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "worker_user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_assigned_worker_worker")
    )
    private User worker;

    @Enumerated(EnumType.STRING)
    @Column(name = "assigned_service_type", nullable = false, length = 30)
    private AssignedServiceType assignedServiceType;
    @Enumerated(EnumType.STRING)
    @Column(name = "assign_status", nullable = false, length = 30)
    private AssignStatus assignStatus;

    @Column(name = "assigned_service_id", nullable = false)
    private UUID assignedServiceId;


    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @PrePersist
    protected void onCreate() {
        this.assignedAt = Instant.now();
    }
}
