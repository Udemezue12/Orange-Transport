package com.astrotech.transport.cloudinary;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CloudinaryBatchSignatureRequest(
        @NotEmpty
        List<@Valid CloudinarySignatureRequest> files
) {}
