package com.astrotech.transport.service;


import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.request.CreateProfileRequest;

import com.astrotech.transport.dto.request.UserUpdate;
import com.astrotech.transport.dto.response.ProfileResponse;
import com.astrotech.transport.dto.response.SimpleProfileResponse;
import com.astrotech.transport.dto.response.SliceResponse;
import com.astrotech.transport.dto.response.UnAssignedWorkerResponse;
import com.astrotech.transport.entities.DriverProfile;
import com.astrotech.transport.entities.Profile;
import com.astrotech.transport.enums.AssignStatus;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;
import com.astrotech.transport.events.DocumentUploadEvent;
import com.astrotech.transport.events.ProfilePicUpdateRequest;
import com.astrotech.transport.events.ProfilePicUploadEvent;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ConflictException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.ProfileMapper;
import com.astrotech.transport.repositories.ProfileRepository;
import com.astrotech.transport.utilities.hash.RequestHashUtil;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserService userService;
    private final RequestHashUtil requestHashUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @RoleRequired({UserRole.ADMIN,
            UserRole.PASSENGER,
            UserRole.TERMINAL_SUPERVISOR,
            UserRole.CUSTOMER_SERVICE_AGENT,
            UserRole.CASHIER,
            UserRole.VEHICLE_LOADER
    })
    @CacheEvict(value = "all-users", allEntries = true)
    public ProfileResponse createProfile(UUID userId, CreateProfileRequest request) {
        var currentUser = userService.getAuthorizedUser(userId);
        if (currentUser.getRole() != UserRole.PASSENGER
                && request == null

        ) {
            throw new BadRequestException("Profile details are required for users with the role: " + currentUser.getRole());

        }

        var profileMapper = ProfileMapper.create(currentUser);
        var savedProfile = profileRepository.save(profileMapper);
        eventPublisher.publishEvent(new ProfilePicUploadEvent(
                savedProfile.getUser().getId(),
                request.profilePicAssetId(),
                request.profilePicPublicId()
        ));
        eventPublisher.publishEvent(new DocumentUploadEvent(
                savedProfile.getUser().getId(),
                request.documentAssetId(),
                request.documentPublicId(),
                request.documentType(),
                request.documentNumber()
        ));
        return ProfileMapper.response(savedProfile);


    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "user-profiles", key = "#userId"),
            @CacheEvict(value = "all-users", allEntries = true)
    })
    @RoleRequired({
            UserRole.ADMIN,
            UserRole.PASSENGER,
            UserRole.TERMINAL_SUPERVISOR,
            UserRole.CUSTOMER_SERVICE_AGENT,
            UserRole.CASHIER,
            UserRole.VEHICLE_LOADER
    })
    public ProfileResponse updateProfile(UserUpdate request, UUID userId) {
        var profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        userService.updateUser(profile.getUser(), request.email(), request.phoneNumber(), request.firstName(), request.lastName(), request.newPassword(), request.oldPassword());
        if (request.assetId() != null && request.publicId() != null) {
            if (!request.assetId().equals(profile.getProfilePicAssetId())) {
                if (profileRepository.existsByProfilePicAssetId(request.assetId())) {
                    throw new ConflictException("Image already exists.");
                }
                var profileUserId = profile.getUser().getId();

                var oldResourceType = profile.getProfilePicResourceType();
                var oldPublicId = profile.getProfilePicPublicId();

                if (oldPublicId != null) {
                    eventPublisher.publishEvent(new ProfilePicUpdateRequest(
                            String.valueOf(profileUserId),
                            oldPublicId,
                            oldResourceType,
                            request.assetId(),
                            request.publicId()));
                }


            }
        }


        return ProfileMapper.response(profile);


    }


    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "user-profiles",
            key = "#userId",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public ProfileResponse getProfile(UUID userId) {


        return profileRepository.findByUserId(userId)
                .map(ProfileMapper::response)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));


    }

    @Transactional(readOnly = true)
    @CustomCacheable(
            value = "all-users",
            key = "'p-' + #page + '-s-' + #size",
            ttl = 120,
            timeUnit = TimeUnit.SECONDS
    )
    @RoleRequired(UserRole.ADMIN)
    public SliceResponse<SimpleProfileResponse> getProfiles(int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "id", true, Profile.class, true);

        var result = profileRepository
                .findAllBy(pageable);
        var content = result.getContent()
                .stream()
                .map(ProfileMapper::simpleProfileResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );


    }

    public SliceResponse<SimpleProfileResponse> searchActiveProfiles(String searchName, int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "user.fullName", true, Profile.class, true);
        var result = profileRepository
                .searchProfiles(searchName, UserStatus.ACTIVE, pageable);
        var content = result.getContent()
                .stream()
                .map(ProfileMapper::simpleProfileResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );

    }

    @CustomCacheable(
            value = "unassigned-lists",
            key = "'unassigned' + #userRole + ':p' + #page + ':s' + #size",
            ttl = 300,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<UnAssignedWorkerResponse> getUnassignedWorkers(
            int page,
            int size,
            String userRole
    ) {
        var trimmedRole = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(userRole, true);
        var role = UserRole.valueOf(trimmedRole);
        if (role == UserRole.ADMIN || role == UserRole.PASSENGER) {
            throw new BadRequestException("Not Allowed");
        }
        var pageable = GetPageRequest.getPageableWithSorting(
                page,
                size,
                "user.fullName",
                true,
                DriverProfile.class,
                true
        );

        var result = profileRepository.findUnassignedWorkers(
                UserStatus.ACTIVE,
                role,
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
    public void updateProfilePicImage(UUID userId, String profilePicPublicId, String profilePicUrl, String profilePicAssetId, String profilePicResourceType, ImageUploadStatus uploadStatus) {
        var profile = getUserProfile(userId);

        var imageHash = requestHashUtil.hash(profilePicAssetId);

        profile.setProfilePicPublicId(profilePicPublicId);
        profile.setProfilePicUploadStatus(uploadStatus);
        profile.setProfilePicUrl(profilePicUrl);
        profile.setProfilePicAssetId(profilePicAssetId);
        profile.setProfilePicHash(imageHash);
        profile.setProfilePicResourceType(profilePicResourceType);
        profileRepository.save(profile);
    }

    public Profile getUserProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found for ID: " + userId + ". Please create a profile to proceed."
                ));
    }

    public Profile findCustomerAgents(UUID userId, UserStatus status, UserRole role) {
        return profileRepository.findByUserIdAndUserStatusAndRole(userId, status, role)
                .orElseThrow(() -> new ResourceNotFoundException("No Customer Service Agent Found"));
    }


}
