package com.astrotech.transport.mappers;

import com.astrotech.transport.core.TrimWhiteSpace;

import com.astrotech.transport.dto.response.UserResponse;
import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;

import java.time.Instant;


public class UserMapper {
    public static User create(String phoneNumber,String emailRequest, String fullName, String password, UserRole role){
        var email = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(emailRequest, false);


        return User.builder()
                .email(email)
                .fullName(fullName)
                .password(password)
                .role(role)
                .verified(false)
                .phoneNumber(phoneNumber)
                .status(UserStatus.ACTIVE)
                .deleted(false)
                .suspended(false)
                .createdAt(Instant.now())
                .build();
    }
    public static UserResponse response(User user){
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getStatus(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isVerified()
        );
    }

}
