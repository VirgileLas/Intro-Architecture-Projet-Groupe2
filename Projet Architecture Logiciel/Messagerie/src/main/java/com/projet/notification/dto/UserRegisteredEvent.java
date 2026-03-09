package com.projet.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserRegisteredEvent(
        String type,
        String eventId,
        String occurredAt,
        EventData data
) {
    public record EventData(
            String userId,
            String email,
            String tokenId
    ) {}
}