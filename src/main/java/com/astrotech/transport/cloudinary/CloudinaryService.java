package com.astrotech.transport.cloudinary;

import com.astrotech.transport.config.CloudinaryConfig;
import com.astrotech.transport.enums.MediaType;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.InternalServerException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private static final Map<String, String> MIME_TO_RESOURCE = Map.ofEntries(
            Map.entry("image/jpeg", "image"),
            Map.entry("image/png", "image"),
            Map.entry("image/webp", "image"),
            Map.entry("video/mp4", "video"),
            Map.entry("video/webm", "video"),
            Map.entry("audio/mpeg", "video"),
            Map.entry("audio/ogg", "video"),
            Map.entry("application/pdf", "raw")
    );
    private static final Map<String, Long> MAX_SIZES = Map.of(
            "image", 16L * 1024 * 1024,   // 16 MB
            "video", 64L * 1024 * 1024,   // 64 MB
            "raw", 10L * 1024 * 1024      // 10 MB
    );

    private final Cloudinary cloudinary;
    private final CloudinaryConfig config;


    public List<CloudinarySignedUploadResponse> generate(
            CloudinaryBatchSignatureRequest request
    ) {
        return request.files()
                .stream()
                .map(this::generateUploadSignature)
                .toList();


    }
    public List<CloudinarySignedUploadResponse> generateUploadSignatures(
            List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one file must be provided.");
        }

        return files.stream()
                .map(file -> {
                    long fileSize = file.getSize();
                    String mimeType = file.getContentType();
                    String fileName = file.getOriginalFilename();

                    var request = new CloudinarySignatureRequest(fileSize, mimeType, fileName);
                    return generateUploadSignature(request);
                })
                .toList();
    }

    private static void validateRequests(Long fileSize, String mimeType, String fileName) {

        if (fileSize == null) {
            throw new BadRequestException("File size is required.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new BadRequestException("File name cannot be empty.");
        }
        if (mimeType == null || mimeType.isBlank()) {
            throw new BadRequestException("File MIME type is required.");

        }
    }

    public VerifiedCloudinaryAsset getVerifiedCloudinaryAsset(String publicId, String assetId) {
        var asset = verifyUpload(
                publicId,
                assetId
        );

        if (asset == null
                || asset.assetId() == null
                || asset.publicId() == null
                || asset.secureUrl() == null) {

            throw new IllegalStateException(
                    "Cloudinary verification returned incomplete data."
            );
        }
        return asset;
    }


    public CloudinaryBackendUploadResponse uploadMedia(
            MultipartFile file, UUID Id) {

        validateFile(file);

        var mimeType = file.getContentType();
        var resourceType = MIME_TO_RESOURCE.getOrDefault(mimeType, "raw");

        validateFileSize(file.getSize(), resourceType);

        var mediaType = resolveMediaType(mimeType);

        var publicId = "transport/" + UUID.randomUUID();

        Map<String, Object> options = buildUploadOptions(
                publicId,
                resourceType,
                mimeType,
                String.valueOf(Id)
        );

        boolean uploaded = false;
        File tempFile = null;

        try {
            var digest = MessageDigest.getInstance("SHA-256");
            Map<?, ?> uploadResult;
            String checksum;


            tempFile = File.createTempFile("securechat-", ".tmp");


            try (InputStream inputStream = file.getInputStream();
                 OutputStream outputStream = new FileOutputStream(tempFile);
                 DigestOutputStream digestOutputStream = new DigestOutputStream(outputStream, digest)) {

                inputStream.transferTo(digestOutputStream);
            }


            checksum = HexFormat.of().formatHex(digest.digest());


            if ("video".equals(resourceType) && file.getSize() >= 100L * 1024 * 1024) {
                uploadResult = cloudinary.uploader().uploadLarge(tempFile, options);
            } else {
                uploadResult = cloudinary.uploader().upload(tempFile, options);
            }

            uploaded = true;

            log.info(
                    "Media uploaded successfully. publicId={}, ticketId={}, size={} KB",
                    publicId,
                    Id,
                    file.getSize() / 1024
            );

            return assembleMediaUploadResponse(
                    uploadResult,
                    publicId,
                    resourceType,
                    mimeType,
                    mediaType,
                    file.getSize(),
                    file.getOriginalFilename(),
                    checksum
            );

        } catch (IOException e) {
            log.error("I/O error uploading media for {}", Id, e);
            throw new BadRequestException("Failed to upload media.");

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm unavailable", e);
            throw new IllegalStateException("SHA-256 algorithm is unavailable.", e);

        } catch (Exception e) {
            log.error("Unexpected Cloudinary upload error for {}", Id, e);
            if (uploaded) {
                deleteResource(publicId, resourceType);
            }
            throw new BadRequestException("Failed to upload media.");

        } finally {

            if (tempFile != null && tempFile.exists()) {
                try {
                    java.nio.file.Files.delete(tempFile.toPath());
                } catch (IOException e) {
                    log.warn("Failed to clean up local temporary file at {}", tempFile.getAbsolutePath(), e);
                }
            }
        }
    }

    public List<CloudinaryBackendUploadResponse> processBatchUpload(MultipartFile[] files, String Id) {
        List<CloudinaryBackendUploadResponse> uploadedResponses = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            try {

                var response = uploadMedia(file, UUID.fromString(Id));


                uploadedResponses.add(response);

            } catch (Exception e) {
                log.error("Failed to process and upload file: {}",
                        file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown_file", e);


                throw new InternalServerException("Batch processing aborted");
            }
        }

        return uploadedResponses;
    }


    public boolean deleteResources(List<String> publicIds) {

        try {
            var result = cloudinary.api().deleteResources(
                    publicIds,
                    ObjectUtils.asMap(
                            "invalidate", true
                    )
            );

            log.info(
                    "Cloudinary bulk deletion completed. result={}",
                    result
            );

            return true;

        } catch (Exception e) {
            log.error(
                    "Failed to delete multi-resources: {}",
                    e.getMessage(),
                    e
            );

            throw new InternalServerException("Error deleting resources");
        }
    }

    public boolean deleteResource(String publicId, String resourceType) {

        var options = ObjectUtils.asMap(
                "resource_type", resourceType,
                "invalidate", true
        );

        try {
            var result = cloudinary.uploader().destroy(publicId, options);

            var status = (String) result.get("result");

            if ("ok".equals(status)) {
                log.info(
                        "Cloudinary resource deleted successfully. publicId={}",
                        publicId
                );
                return true;
            }

            if ("not found".equals(status)) {
                log.warn(
                        "Cloudinary resource was not found. publicId={}",
                        publicId
                );
                return true;
            }

            log.error(
                    "Cloudinary resource deletion failed. publicId={}, result={}",
                    publicId,
                    status
            );

            return false;

        } catch (Exception e) {
            log.error(
                    "Failed to delete resource {}: {}",
                    publicId,
                    e.getMessage(),
                    e
            );

            throw new InternalServerException("Error deleting resource");
        }
    }


    public CloudinaryUploadResponse uploadPdf(
            byte[] pdfBytes,
            String folder,
            String publicId
    ) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            log.error("PDF bytes are empty");
            throw new IllegalArgumentException("PDF bytes cannot be empty");
        }
        if (resourceExists(publicId, "raw")){
            throw new BadRequestException("PDF already exists");
        }


        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    pdfBytes,
                    ObjectUtils.asMap(
                            "resource_type", "raw",
                            "folder", folder,
                            "public_id", publicId,
                            "overwrite", true
                    )
            );

            log.info(
                    "Successfully uploaded PDF to Cloudinary. publicId={}, secureUrl={}",
                    uploadResult.get("public_id"),
                    uploadResult.get("secure_url")
            );

            return new CloudinaryUploadResponse(
                    (String) uploadResult.get("asset_id"),
                    (String) uploadResult.get("resource_type"),
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id")
            );

        } catch (IOException e) {
            log.error("PDF upload to Cloudinary failed", e);
            throw new RuntimeException("PDF upload to Cloudinary failed", e);
        }
    }




    public boolean resourceExists(String publicId, String resourceType) {
        try {
            cloudinary.api().resource(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private VerifiedCloudinaryAsset verifyUpload(
            String expectedPublicId,
            String expectedAssetId
    ) {

        try {

            Map<?, ?> resource = cloudinary.api().resource(
                    expectedPublicId,
                    ObjectUtils.emptyMap()
            );

            var actualAssetId = (String) resource.get("asset_id");

            var actualPublicId = (String) resource.get("public_id");
            var secureUrl = (String) resource.get("secure_url");
            var resourceType = (String) resource.get("resource_type");
            var format = (String) resource.get("format");
            var thumbNailUrl = buildThumbnailUrl(resource, resourceType, "image");


            Integer width = (Integer) resource.get("width");
            Integer height = (Integer) resource.get("height");

            Number bytesNumber = (Number) resource.get("bytes");
            Long bytes = bytesNumber == null ? null : bytesNumber.longValue();

            if (!actualPublicId.equals(expectedPublicId)) {
                throw new BadRequestException("Invalid Cloudinary public ID.");
            }

            if (expectedAssetId != null &&
                    !expectedAssetId.equals(actualAssetId)) {

                throw new BadRequestException("Cloudinary asset verification failed.");
            }

            return VerifiedCloudinaryAsset.builder()
                    .assetId(actualAssetId)
                    .publicId(actualPublicId)
                    .secureUrl(secureUrl)
                    .thumbNailUrl(thumbNailUrl)
                    .resourceType(resourceType)
                    .format(format)
                    .width(width)
                    .height(height)
                    .bytes(bytes)
                    .build();

        } catch (ResourceNotFoundException ex) {

            throw new BadRequestException("Uploaded image does not exist.");

        } catch (Exception ex) {

            log.error("Cloudinary verification failed", ex);
            throw new InternalServerException(
                    "Unable to verify uploaded image."
            );
        }
    }

    private void validateTimestamp(long timestamp) {
        long now = Instant.now().getEpochSecond();
        if ((now - timestamp) > 30) {
            throw new BadRequestException("Timestamp expired");
        }
        if (timestamp > now + 5) {
            throw new BadRequestException("Timestamp is in the future");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        String mimeType = file.getContentType();
        if (mimeType == null || !MIME_TO_RESOURCE.containsKey(mimeType)) {
            throw new BadRequestException("Unsupported file type: " + mimeType);
        }
    }

    private void validateFileSize(long fileSize, String resourceType) {
        Long maxSize = MAX_SIZES.get(resourceType);
        if (maxSize == null) {
            throw new BadRequestException("Unsupported resource type");
        }
        if (fileSize > maxSize) {
            throw new BadRequestException(
                    String.format("File exceeds the maximum allowed size of %d MB", maxSize / (1024 * 1024))
            );
        }
    }

    private String buildThumbnailUrl(Map<?, ?> result, String resourceType, String mimeType) {
        var url = (String) result.get("secure_url");
        if (url == null) return null;

        if ("image".equals(resourceType)) {
            return url.replace("/upload/", "/upload/w_400,h_400,c_thumb,g_face/");
        }
        if ("video".equals(resourceType) && mimeType != null && mimeType.startsWith("video")) {
            return url.replace("/upload/", "/upload/w_320,h_240,c_fit,so_0/").replaceAll("\\.[^.]+$", ".jpg");
        }
        return null;
    }

    private MediaType resolveMediaType(String mimeType) {
        if (mimeType == null) return MediaType.DOCUMENT;
        if (mimeType.startsWith("image/")) return MediaType.IMAGE;
        if (mimeType.startsWith("video/")) return MediaType.VIDEO;
        if (mimeType.startsWith("audio/")) return MediaType.AUDIO;
        return MediaType.DOCUMENT;
    }

    private CloudinaryBackendUploadResponse assembleMediaUploadResponse(
            Map<?, ?> result, String publicId, String resourceType, String mimeType,
            MediaType mediaType, long fileSize, String originalName, String checksum) {

        var secureUrl = (String) result.get("secure_url");
        String thumbnailUrl = buildThumbnailUrl(result, resourceType, mimeType);
        var assetId = (String) result.get("asset_id");
        Integer width = result.get("width") != null ? (Integer) result.get("width") : null;
        Integer height = result.get("height") != null ? (Integer) result.get("height") : null;
        Integer duration = result.get("duration") != null ? ((Number) result.get("duration")).intValue() : null;

        return CloudinaryBackendUploadResponse
                .builder()
                .publicId(publicId)
                .secureUrl(secureUrl)
                .thumbnailUrl(thumbnailUrl)
                .assetId(assetId)
                .originalName(originalName)
                .mimeType(mimeType)
                .mediaType(mediaType)
                .fileSize(fileSize)
                .width(width)
                .height(height)
                .duration(duration)
                .checksum(checksum)
                .build();
    }

    private CloudinarySignedUploadResponse generateUploadSignature(
            CloudinarySignatureRequest request
    ) {
        var fileSize = request.fileSize();
        var mimeType = request.mimeType();
        String fileName = request.fileName();
        validateRequests(fileSize, mimeType, fileName);

        var resourceType = MIME_TO_RESOURCE.get(mimeType);
        if (resourceType == null) {
            throw new BadRequestException("Unsupported file type");
        }

        var maxSize = MAX_SIZES.get(resourceType);
        if (fileSize > maxSize) {
            throw new BadRequestException(
                    "Maximum allowed size is " + (maxSize / (1024 * 1024)) + " MB"
            );
        }

        var timestamp = Instant.now().getEpochSecond();
        validateTimestamp(timestamp);

        var folder = "transport/uploads/";
        var publicId = UUID.randomUUID();

        var eager = switch (resourceType) {
            case "image" -> "f_auto,q_auto";
            case "video" -> "q_auto";
            default -> null;
        };

        List<String> allowedFormats = switch (resourceType) {
            case "image" -> List.of("jpg", "jpeg", "png", "webp");
            case "video" -> List.of("mp4", "webm", "mp3", "ogg");
            default -> List.of("pdf");
        };

        String allowedFormatsStr = String.join(",", allowedFormats);

        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", timestamp);
        params.put("folder", folder);
        params.put("public_id", publicId);
//        params.put("allowed_formats", allowedFormatsStr);
//        params.put("overwrite", false);

//        if (eager != null) {
//            params.put("eager", eager);
//        }

        try {
            var signature = cloudinary.apiSignRequest(
                    params,
                    config.getApiSecret(),
                    config.getSignatureVersion()
            );

            return CloudinarySignedUploadResponse.builder()
                    .signature(signature)
                    .timestamp(timestamp)
                    .apiKey(config.getApiKey())
                    .cloudName(config.getCloudName())
                    .folder(folder)
                    .publicId(String.valueOf(publicId))
                    .resourceType(resourceType)
                    .eager(eager)
                    .maxFileSize(maxSize)
                    .allowedFormats(allowedFormats)
                    .build();

        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to generate Cloudinary signature", ex);
            throw new InternalServerException("Failed to generate upload signature");
        }
    }

    private Map<String, Object> buildUploadOptions(
            String publicId,
            String resourceType,
            String mimeType,
            String Id
    ) {

        Map<String, Object> options = new HashMap<>();

        options.put("public_id", publicId);
        options.put("resource_type", resourceType);
        options.put("overwrite", false);
        options.put("unique_filename", false);
        options.put("tags", List.of(
                "transport",
                Id
        ));

        if ("image".equals(resourceType)) {

            options.put(
                    "eager",
                    List.of(
                            new Transformation<>()
                                    .width(400)
                                    .height(400)
                                    .crop("thumb")
                                    .gravity("face")
                    )
            );

            options.put("eager_async", true);

        } else if ("video".equals(resourceType)
                && mimeType != null
                && mimeType.startsWith("video")) {

            options.put(
                    "eager",
                    List.of(
                            new Transformation<>()
                                    .width(320)
                                    .height(240)
                                    .crop("fit")
                                    .fetchFormat("jpg")
                    )
            );

            options.put("eager_async", true);
        }

        return options;
    }
}