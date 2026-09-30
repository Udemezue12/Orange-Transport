package com.astrotech.transport.service;


import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.DriverProfileRequest;
import com.astrotech.transport.dto.request.DriverProfileUpdateRequest;
import com.astrotech.transport.dto.response.DriverProfileResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.UnAssignedWorkerResponse;
import com.astrotech.transport.entities.DriverProfile;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.events.ImageUpdateRequest;
import com.astrotech.transport.events.ImageUploadRequest;
import com.astrotech.transport.events.LicenseVerificationEvent;
import com.astrotech.transport.exceptions.ConflictException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.DriverProfileMapper;
import com.astrotech.transport.repositories.DriverProfileRepository;
import com.astrotech.transport.utilities.hash.RequestHashUtil;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DriverProfileService {
    private final DriverProfileRepository driverProfileRepository;

    private final RequestHashUtil requestHashUtil;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;


    @Transactional
    @RoleRequired(UserRole.DRIVER)
    @CacheEvict(value = "all-drivers", allEntries = true)
    public DriverProfileResponse createProfile(DriverProfileRequest request, UUID userId) {
        var currentUser = userService.getAuthorizedUser(userId);
        var currentUserId = currentUser.getId();


        var trimmedLicenseNumber = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.licenseNumber(), true);
        if (driverProfileRepository.existsByLicenseNumber(trimmedLicenseNumber)) {
            throw new ConflictException("License number already exists.");
        }


        var profile = DriverProfileMapper.create(request, currentUser);


        var savedProfile = driverProfileRepository.save(profile);
        eventPublisher.publishEvent(new LicenseVerificationEvent(currentUserId.toString(), savedProfile.getLicenseNumber()));
        if (request.assetId() != null && request.publicId() != null) {
            eventPublisher.publishEvent(new ImageUploadRequest(currentUserId.toString(), request.assetId(), request.publicId()));
        }

        return DriverProfileMapper.response(savedProfile);

    }

    @Transactional
    @RoleRequired(UserRole.DRIVER)
    @Caching(evict = {
            @CacheEvict(value = "driver-profiles", key = "#userId"),
            @CacheEvict(value = "all-drivers", allEntries = true)
    })
    public DriverProfileResponse updateProfile(DriverProfileUpdateRequest request, UUID userId) {

        var profile = getDriverProfile(userId);
        userService.updateUser(profile.getUser(),
                request.email(), request.phoneNumber(), request.firstName(),
                request.lastName(), request.newPassword(), request.oldPassword()
        );




        var trimmedLicenseNumber = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.licenseNumber(), true);
        if (trimmedLicenseNumber != null && !trimmedLicenseNumber.isBlank()) {
            if (!trimmedLicenseNumber.equals(profile.getLicenseNumber())) {
                if (driverProfileRepository.existsByLicenseNumber(trimmedLicenseNumber)) {
                    throw new ConflictException("License number already exists.");
                }
                profile.setLicenseNumber(trimmedLicenseNumber);
                profile.setLicenseVerified(false);


                eventPublisher.publishEvent(new LicenseVerificationEvent(userId.toString(), trimmedLicenseNumber));

            }
        }


        if (request.assetId() != null && request.publicId() != null) {
            if (!request.assetId().equals(profile.getAssetId())) {
                if (driverProfileRepository.existsByAssetId(request.assetId())) {
                    throw new ConflictException("Image already exists.");
                }
                var profileUserId = profile.getUser().getId();

                var oldResourceType = profile.getResourceType();
                var oldPublicId = profile.getPublicId();

                if (oldPublicId != null) {
                    eventPublisher.publishEvent(new ImageUpdateRequest(
                            String.valueOf(profileUserId),
                            oldPublicId,
                            oldResourceType,
                            request.assetId(),
                            request.publicId()));
                }


            }
        }

        driverProfileRepository.save(profile);
        return DriverProfileMapper.response(profile);

    }

    @Transactional(readOnly = true)
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR, UserRole.DRIVER})
    @CustomCacheable(
            value = "driver-profiles",
            key = "#userId",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public DriverProfileResponse getSingleProfile(UUID userId) {
        var profile = getDriverProfile(userId);

        return DriverProfileMapper.response(profile);


    }

    @Transactional
    @RoleRequired({UserRole.ADMIN, UserRole.TERMINAL_SUPERVISOR})
    @CustomCacheable(
            value = "all-drivers",
            key = "'p-' + #page + '-s-' + #size + '-sort-' + #sortBy",
            ttl = 120,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<DriverProfileResponse> getAllDrivers(int page, int size, String sortBy) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, sortBy, true, DriverProfile.class, true);


        var sliceResult = driverProfileRepository.findAllBy(pageable);

        var content = sliceResult.getContent()
                .stream()
                .map(DriverProfileMapper::response)
                .toList();

        return new SliceResponse<>(
                content,
                sliceResult.getNumber(),
                sliceResult.getSize(),
                sliceResult.hasNext(),
                sliceResult.hasPrevious()
        );
    }

    public DriverProfile getDriverProfile(UUID userId) {


        return driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "driver-profiles", key = "#userId"),
            @CacheEvict(value = "all-drivers", allEntries = true)
    })
    public void updateProfileImage(UUID userId, String publicId, String assetId, String secureUrl, String resourceType, ImageUploadStatus uploadStatus) {
        var profile = getDriverProfile(userId);

        var imageHash = requestHashUtil.hash(assetId);

        profile.setImageUrl(secureUrl);
        profile.setUploadStatus(uploadStatus);
        profile.setPublicId(publicId);
        profile.setAssetId(assetId);
        profile.setImageHash(imageHash);
        profile.setResourceType(resourceType);
        driverProfileRepository.save(profile);
    }
    @CustomCacheable(
            value = "unassigned-lists",
            key = "'unassignedDrivers-p' + #page + '-s' + #size",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<UnAssignedWorkerResponse> getUnassignedDrivers(
            int page,
            int size
    ) {
        var pageable = GetPageRequest.getPageableWithSorting(
                page,
                size,
                "user.fullName",
                true,
                DriverProfile.class,
                true
        );

        var result = driverProfileRepository.findUnassignedDrivers(
                UserStatus.ACTIVE,
                UserRole.DRIVER,
                AssignStatus.ACTIVE,
                pageable
        );

        return new SliceResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "driver-profiles", key = "#profile.userId"),
            @CacheEvict(value = "all-drivers", allEntries = true)
    })
    public void updateLicenseVerificationStatus(DriverProfile profile, boolean verified, LicenseVerificationStatus status) {


        profile.setLicenseVerified(verified);
        profile.setLicenseVerificationStatus(status);
        profile.setLicenseVerifiedAt(Instant.now());
        driverProfileRepository.save(profile);
    }


}
