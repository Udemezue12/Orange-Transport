package com.astrotech.transport.service;


import com.astrotech.transport.cloudinary.CloudinaryService;
import com.astrotech.transport.dto.response.UploadResponse;
import com.astrotech.transport.dto.response.VehicleImageResponse;
import com.astrotech.transport.entities.VehicleImages;
import com.astrotech.transport.enums.ImageUploadStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.VehicleImageMapper;
import com.astrotech.transport.repositories.VehicleImagesRepository;
import com.astrotech.transport.utilities.hash.RequestHashUtil;
import com.astrotech.transport.validators.role.RoleRequired;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class VehicleImageService {
    private final VehicleService vehicleService;
    private final VehicleImagesRepository vehicleImagesRepository;
    private final RequestHashUtil requestHashUtil;
    private final CloudinaryService cloudinaryService;


    @Transactional
    public void saveVehicleImage(
            UUID vehicleId,
            String assetId,
            String publicId,
            String resourceType,
            String thumbNailUrl,
            String secureUrl
    ) {
        var vehicle = vehicleService.getVehicle(vehicleId);

        if (vehicleImagesRepository.countByVehicleId(vehicleId) >= 3) {
            throw new BadRequestException("Maximum of 3 images allowed.");
        }


        if (vehicleImagesRepository.existsByVehicleIdAndAssetId(vehicleId, assetId)) {
            return;
        }

        var hash = requestHashUtil.hash(assetId);

        var image = VehicleImageMapper.createImage(
                vehicle,
                assetId,
                resourceType,
                hash,
                secureUrl,
                publicId,
                thumbNailUrl
        );

        vehicleImagesRepository.save(image);

        if (vehicle.getThumbNailUrl() == null) {
            vehicle.setThumbNailUrl(image.getThumbnailUrl());
        }

        if (secureUrl != null) {
            vehicle.setUploadStatus(ImageUploadStatus.VERIFIED);
        } else {
            vehicle.setUploadStatus(ImageUploadStatus.FAILED);
        }

    }

    @Transactional
    public void updateVehicleImage(
            VehicleImages vehicleImage,
            String assetId,
            String publicId,
            String resourceType,
            String thumbNailUrl,
            String secureUrl
    ) {


        var vehicleId = vehicleImage.getVehicle().getId();

        if (vehicleImagesRepository.countByVehicleId(vehicleId) >= 3) {
            throw new BadRequestException("Maximum of 3 images allowed.");
        }


        if (vehicleImagesRepository.existsByVehicleIdAndAssetId(vehicleId, assetId)) {
            return;
        }


        var hash = requestHashUtil.hash(assetId);

        vehicleImage.setAssetId(assetId);
        vehicleImage.setResourceType(resourceType);
        vehicleImage.setImageHash(hash);
        vehicleImage.setImageUrl(secureUrl);
        vehicleImage.setPublicId(publicId);
        vehicleImage.setThumbnailUrl(thumbNailUrl);


        vehicleImagesRepository.save(vehicleImage);

        if (!vehicleImage.getVehicle().getThumbNailUrl().equals(thumbNailUrl)) {
            vehicleImage.getVehicle().setThumbNailUrl(thumbNailUrl);
        }

        if (secureUrl != null) {
            vehicleImage.getVehicle().setUploadStatus(ImageUploadStatus.VERIFIED);
        } else {
            vehicleImage.getVehicle().setUploadStatus(ImageUploadStatus.FAILED);
        }

    }


    @Transactional(readOnly = true)
    public VehicleImages getVehicleImages(UUID vehicleImageId, UUID vehicleId) {
        return vehicleImagesRepository.findByIdAndVehicleId(vehicleImageId, vehicleId).orElse(null);
    }

    @RoleRequired(UserRole.ADMIN)
    public void deleteVehicleImage(UUID vehicleId) {

        var vehicleImage = vehicleImagesRepository.findByVehicleIdWithDetails(
                vehicleId
        ).orElseThrow(() -> new ResourceNotFoundException("Image not found"));
        var result = cloudinaryService.deleteResource(vehicleImage.getPublicId(), vehicleImage.getResourceType());
        if (result) {
            deleteImage(vehicleImage.getVehicle().getId());


        }
    }

    @Transactional
    public void deleteImage(UUID vehicleId) {

        vehicleImagesRepository.deleteByVehicleId(vehicleId);


    }

    @Transactional(readOnly = true)
    public List<VehicleImageResponse> getVehicleWithImages(UUID vehicleId) {

        var vehicle = vehicleService.getVehicle(vehicleId);

        return vehicleImagesRepository
                .findByVehicleId(vehicleId)
                .stream()
                .map(img -> VehicleImageMapper.toImageResponse(
                        vehicle,
                        img
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public UploadResponse getVehicleImage(UUID vehicleId, UUID vehicleImageId) {


        return vehicleImagesRepository
                .findByIdAndVehicleId(vehicleId, vehicleImageId)
                .map(VehicleImageMapper::toUploadResponse)
                .orElse(null);
    }


}
