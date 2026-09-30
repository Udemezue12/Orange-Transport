package com.astrotech.transport.events;


import java.util.List;
import java.util.UUID;

public record MultipleImagesDeleteEvent(UUID id, List<String> publicIds, List<String> resourceTypes) {
}
