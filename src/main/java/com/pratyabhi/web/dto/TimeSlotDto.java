package com.pratyabhi.web.dto;

import com.pratyabhi.service.AvailabilityService;
import java.time.Instant;

public record TimeSlotDto(Instant startTime, Instant endTime) {

    public static TimeSlotDto from(AvailabilityService.TimeSlot slot) {
        return new TimeSlotDto(slot.start(), slot.end());
    }
}
