package com.astrotech.transport.service;

import com.astrotech.transport.core.*;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.CodeStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.exceptions.*;
import com.astrotech.transport.mappers.GenerateRegisterCodeMapper;
import com.astrotech.transport.repositories.RegistrationCodeRepository;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GenerateRegisterCodeService {

    private final RegistrationCodeRepository registrationCodeRepository;
    private static final Integer NUM_CONSTANT = 3;

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    public GeneratedCodeResponse createRegistrationCode(UserRole role) {
        if (role == null || role == UserRole.PASSENGER) {
            throw new BadRequestException("This role cannot cannot receive registration code");
        }
        var trimmedCode = TrimWhiteSpace.trimWhiteSpace(RegistrationCodeGenerator.generate());
        var newEntity = GenerateRegisterCodeMapper.generateCode(trimmedCode, role);
        var savedEntity = registrationCodeRepository.save(newEntity);
        return GenerateRegisterCodeMapper.getResponse(savedEntity);
    }

    @Transactional
    @RoleRequired(UserRole.ADMIN)
    public List<GeneratedCodeResponse> createRegistrationCodes(List<UserRole> roles, int num) {
        if (roles == null || roles.contains(UserRole.PASSENGER)) {
            throw new BadRequestException("This role cannot receive a registration code");
        }
        var generatedNum = Optional.of(num)
                .filter(n -> n <= NUM_CONSTANT)
                .orElse(NUM_CONSTANT);


        var registrationCodes = roles.stream()
                .flatMap(role -> IntStream.range(0, generatedNum)
                        .mapToObj(i -> {
                            String code = TrimWhiteSpace.trimWhiteSpace(
                                    RegistrationCodeGenerator.generate()
                            );

                            return GenerateRegisterCodeMapper.generateCode(code, role);
                        }))
                .toList();

        var savedCodes =
                registrationCodeRepository.saveAll(registrationCodes);

        return savedCodes.stream()
                .map(GenerateRegisterCodeMapper::getResponse)
                .toList();
    }
    @Transactional
    public void checkAndUpdateCodeStatus(String inviteCode, UserRole role, CodeStatus checkCodeStatus, CodeStatus updateCodeStatus) {
        getCheckAndUpdateStatus(inviteCode, role, checkCodeStatus, updateCodeStatus);
    }




    @Transactional(readOnly = true)
    @RoleRequired(UserRole.ADMIN)
    public SliceResponse<GeneratedCodeResponse> getCodesGeneratedInPast24Hours(int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "generatedAt", true, RegistrationCode.class,
                true);
        var twentyFourHoursAgo = getInstant();

        var result = registrationCodeRepository.findByGeneratedAtAfter(twentyFourHoursAgo, pageable);
        return getGeneratedCodeResponseSliceResponse(result);
    }

    @Transactional(readOnly = true)
    @RoleRequired(UserRole.ADMIN)
    public SliceResponse<GeneratedCodeResponse> getCodes(int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "generatedAt", true, RegistrationCode.class,
                true);


        var result = registrationCodeRepository.findAllBy(pageable);
        return getGeneratedCodeResponseSliceResponse(result);
    }


    @Transactional(readOnly = true)
    public GeneratedCodeResponse getCode(String code) {
        var trimmedCode = TrimWhiteSpace.trimWhiteSpace(code);
        return registrationCodeRepository.findById(trimmedCode)
                .map(GenerateRegisterCodeMapper::getResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired register code"));
    }

    @Transactional(readOnly = true)
    public void verifyCodeAndRoleOrThrow(String code, UserRole requestedRole) {
        var trimmedCode = TrimWhiteSpace.trimWhiteSpace(code);
        boolean isValid = registrationCodeRepository.existsByRegisterCodeAndRole(trimmedCode, requestedRole);
        if (!isValid) {
            throw new BadRequestException("Invalid registration code or role mismatch.");
        }
    }

    @Transactional
    public void deleteInvalidCodes() {

        registrationCodeRepository.deleteByStatus(CodeStatus.INVALID);
    }

    private static Instant getInstant() {
        return Instant.now().minus(
                24,
                ChronoUnit.HOURS);
    }

    private void getCheckAndUpdateStatus(String inviteCode, UserRole role, CodeStatus checkCodeStatus, CodeStatus updateCodeStatus) {
        var trimmedCode = TrimWhiteSpace.trimWhiteSpace(inviteCode);
        RegistrationCode registrationCode = registrationCodeRepository.findByIdAndRoleAndStatus(trimmedCode, checkCodeStatus, role)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or Expired Register code"));
        registrationCode.setStatus(updateCodeStatus);
        var savedRegistrationCode = registrationCodeRepository.save(registrationCode);
        GenerateRegisterCodeMapper.getResponse(savedRegistrationCode);
    }

    private static @NonNull SliceResponse<GeneratedCodeResponse> getGeneratedCodeResponseSliceResponse(Slice<RegistrationCode> result) {
        var content = result.getContent()
                .stream()
                .map(GenerateRegisterCodeMapper::getResponse)
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()
        );
    }
}
