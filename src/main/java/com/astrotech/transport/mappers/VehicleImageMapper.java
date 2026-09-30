package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.Vehicle;
import com.astrotech.transport.entities.VehicleImages;


public class VehicleImageMapper {
    public static VehicleImages createImage(Vehicle vehicle, String assetId, String resourceType, String imageHash, String secureUrl, String publicId, String thumbNailUrl){
        return VehicleImages
                .builder()
                .imageUrl(secureUrl)
                .vehicle(vehicle)
                .imageHash(imageHash)
                .thumbnailUrl(thumbNailUrl)
                .assetId(assetId)
                .publicId(publicId)
                .resourceType(resourceType)
                .build();
    }
    public static UploadResponse toUploadResponse(VehicleImages img) {
        return UploadMapper.toUploadResponse(
                img.getImageUrl(),
                img.getPublicId(),
                img.getResourceType(),
                img.getId()
        );
    }

    public static VehicleImageResponse toImageResponse(Vehicle vehicle, VehicleImages img) {
        var vehicleResponse = VehicleMapper.toResponse(vehicle);
        var uploadResponse =  toUploadResponse(img);
        return new VehicleImageResponse(vehicleResponse, uploadResponse);
    }
}
