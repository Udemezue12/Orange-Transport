package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.projection.ExistingUserFields;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    @Query("""
            SELECT
                u.email AS email,
                u.fullName AS fullName,
                u.phoneNumber AS phoneNumber
            FROM User u
            WHERE
                  u.email = :email
               OR u.fullName = :fullName
               OR u.phoneNumber = :phone
            """)
    List<ExistingUserFields> findExistingFields(
            String email,
            String fullName,
            String phone
    );

    @Query("""
                SELECT u
                FROM User u
                WHERE u.role = :role
                  AND u.status = :status
                AND u.assignments IS EMPTY
            """)
    Slice<User> findUnassignedUsersByRole(@Param("status") UserStatus status, @Param("role") UserRole role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.status = :userStatus")
    Optional<User> findByIdAndStatus(@Param("id") UUID userId, @Param("userStatus") UserStatus userStatus);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.status = :userStatus AND u.role = :role")
    Optional<User> findByIdWithUserDetails(@Param("id") UUID userId, @Param("userStatus") UserStatus userStatus, @Param("role") UserRole role);

    List<User> findAllByIdInAndStatus(Iterable<UUID> ids, UserStatus status);


    @Query("SELECT u FROM User u WHERE u.status = :userStatus AND u.role = :role")
    Slice<User> findAllUsersDetails(
            @Param("userStatus") UserStatus userStatus,
            @Param("role") UserRole role,
            Pageable pageable
    );


    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status IN (:statuses)")
    Optional<User> findByEmailAndStatuses(
            @Param("email") String email,
            @Param("statuses") List<UserStatus> statuses
    );


    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Slice<User> findAllBy(Pageable pageable);

    @Query("""
                SELECT u
                FROM User u
                WHERE (:searchWord IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchWord, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchWord, '%')))
                ORDER BY u.fullName ASC
            """)
    Slice<User> searchUsers(
            @Param("searchWord") String searchWord,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE User u SET u.lastSeen = :now WHERE u.id = :userId")
    void updateLastSeen(@Param("userId") UUID userId, @Param("now") Instant now);

    @Modifying
    @Query("UPDATE User u SET u.onlineStatus = :status WHERE u.id = :userId")
    void updateOnlineStatus(@Param("userId") UUID userId, @Param("status") OnlineStatus status);


}
