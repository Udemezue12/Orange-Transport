package com.astrotech.transport.dto.response;

import java.util.List;

public record VehicleWithImagesAndSeatsResponse(
        VehicleResponse vehicle,
        List<SimpleSeatResponse> seats,
        List<UploadResponse> images
) {
}
