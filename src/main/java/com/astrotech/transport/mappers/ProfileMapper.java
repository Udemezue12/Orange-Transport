package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.ProfileResponse;
import com.astrotech.transport.dto.response.SimpleProfileResponse;
import com.astrotech.transport.entities.Profile;
import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.ImageUploadStatus;

public class ProfileMapper {
    public static Profile create(User user) {
        return Profile.builder()
                .profilePicUploadStatus(ImageUploadStatus.PENDING)
                .user(user)
                .build();
    }
    public static SimpleProfileResponse simpleProfileResponse(Profile profile) {
        return new SimpleProfileResponse(
                profile.getId(),
                profile.getProfilePicUploadStatus(),
                profile.getProfilePicUrl(),
                profile.getProfilePicPublicId(),
                profile.getProfilePicResourceType()

        );
    }
    public static ProfileResponse response(Profile profile) {
        var userResponse = UserMapper.response(profile.getUser());
        var simpleProfileResponse = simpleProfileResponse(profile);
        var identityDocumentResponse = IdentityDocumentMapper.simpleResponse(profile.getIdentityDocument());
        return new ProfileResponse(
                simpleProfileResponse,
                identityDocumentResponse,
                userResponse

        );



    }
}
