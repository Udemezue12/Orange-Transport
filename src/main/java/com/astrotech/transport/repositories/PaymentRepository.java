package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {


    Optional<Payment> findByPayerId(UUID userId);
    @Query("SELECT p FROM Payment p JOIN FETCH p.payer WHERE p.generatedReference = :generatedReference")
    Optional<Payment> findByGeneratedReferenceWithPayer(@Param("generatedReference") String generatedReference);



    Slice<Payment> findAllByPayerId(UUID passengerId, Pageable pageable);

    Optional<Payment> findByIdAndPayerId(UUID transactionId, UUID passengerId);

    Slice<Payment> findAllBy(Pageable pageable);
}
