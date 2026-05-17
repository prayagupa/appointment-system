package com.pratyabhi.web;

import com.pratyabhi.service.AppointmentService;
import com.pratyabhi.service.AvailabilityService;
import com.pratyabhi.service.ProviderService;
import com.pratyabhi.service.TenantService;
import com.pratyabhi.web.dto.AppointmentDto;
import com.pratyabhi.web.dto.BookAppointmentRequest;
import com.pratyabhi.web.dto.ProviderDto;
import com.pratyabhi.web.dto.TenantDto;
import com.pratyabhi.web.dto.TimeSlotDto;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AppointmentApiController {

    private final TenantService tenantService;
    private final ProviderService providerService;
    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;

    public AppointmentApiController(
            TenantService tenantService,
            ProviderService providerService,
            AppointmentService appointmentService,
            AvailabilityService availabilityService) {
        this.tenantService = tenantService;
        this.providerService = providerService;
        this.appointmentService = appointmentService;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/tenants")
    public List<TenantDto> listTenants() {
        return tenantService.listAll().stream().map(TenantDto::from).toList();
    }

    @GetMapping("/providers")
    public List<ProviderDto> listProviders() {
        return providerService.listByTenant().stream().map(ProviderDto::from).toList();
    }

    @GetMapping("/providers/{providerId}")
    public ProviderDto getProvider(@PathVariable UUID providerId) {
        return ProviderDto.from(providerService.get(providerId));
    }

    @GetMapping("/availability")
    public List<TimeSlotDto> listAvailability(
            @RequestParam UUID providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "30") int slotMinutes) {
        return availabilityService.listAvailability(providerId, from, to, slotMinutes).stream()
                .map(TimeSlotDto::from)
                .toList();
    }

    @GetMapping("/appointments")
    public List<AppointmentDto> listAppointments(@RequestParam(required = false) UUID providerId) {
        return appointmentService.list(providerId).stream().map(AppointmentDto::from).toList();
    }

    @GetMapping("/appointments/{appointmentId}")
    public AppointmentDto getAppointment(@PathVariable UUID appointmentId) {
        return AppointmentDto.from(appointmentService.findById(appointmentId));
    }

    @PostMapping("/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentDto book(@Valid @RequestBody BookAppointmentRequest request) {
        return AppointmentDto.from(appointmentService.book(
                request.providerId(),
                request.patientRef(),
                request.startTime(),
                request.durationMinutes()));
    }

    @PostMapping("/appointments/{appointmentId}/cancel")
    public AppointmentDto cancel(@PathVariable UUID appointmentId) {
        return AppointmentDto.from(appointmentService.cancel(appointmentId));
    }
}
