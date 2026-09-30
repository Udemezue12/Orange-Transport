package com.astrotech.transport.controllers;


import com.astrotech.transport.cloudinary.*;
import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cloudinary-uploads")
@RequiredArgsConstructor
@Tag(name = "Cloudinary Uploads", description = "For uploading images, pdf, files to Cloudinary using signed uploads")
public class CloudinaryController {
    private final CloudinaryService cloudinaryService;
    private final ApiCacheControl apiCacheControl;


    @PostMapping("/generate-signed-uploads")
    public ResponseEntity<ApiResponse<List<CloudinarySignedUploadResponse>>> generate(
            @Valid @RequestBody CloudinaryBatchSignatureRequest request
    ) {
        var response = cloudinaryService.generate(request);

        return ApiResponseBuilder.success(
                "Generated signatures successfully",
                response,
                apiCacheControl.noStore()
        );
    }

    @PostMapping(value = "/generate-signed-uploads-multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<CloudinarySignedUploadResponse>>> generateUploadSignatures(
            @RequestPart("files") List<MultipartFile> files
    ) {
        var response = cloudinaryService.generateUploadSignatures(files);
        return ApiResponseBuilder.success(
                "Generated signatures successfully",
                response,
                apiCacheControl.noStore()
        );
    }


}
