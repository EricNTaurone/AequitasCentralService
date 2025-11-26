package com.aequitas.aequitascentralservice.adapter.web.mapper;

import java.util.Optional;

import com.aequitas.aequitascentralservice.adapter.web.generated.dto.CreateTimeEntryRequest;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.EntryStatus;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.TimeEntryResponse;
import com.aequitas.aequitascentralservice.adapter.web.generated.dto.UpdateTimeEntryRequest;
import com.aequitas.aequitascentralservice.domain.command.CreateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;

import lombok.experimental.UtilityClass;

/**
 * Maps REST DTOs to domain commands and aggregates.
 */
@UtilityClass
public class TimeEntryDtoMapper {

    /**
     * Converts the request body into a create command.
     *
     * @param request REST payload.
     * @return domain command.
     */
    public static CreateTimeEntryCommand toCommand(final CreateTimeEntryRequest request) {
        return CreateTimeEntryCommand.builder()
                .customerId(request.getCustomerId())
                .projectId(request.getProjectId())
                .matterId(request.getMatterId())
                .narrative(request.getNarrative())
                .durationMinutes(request.getDurationMinutes())
                .build();
    }

    /**
     * Converts the DTO into an update command.
     *
     * @param request update payload.
     * @return domain command with optional fields.
     */
    public static UpdateTimeEntryCommand toCommand(final UpdateTimeEntryRequest request) {
        return UpdateTimeEntryCommand.builder()
                .customerId(Optional.ofNullable(request.getCustomerId()))
                .projectId(Optional.ofNullable(request.getProjectId()))
                .matterId(Optional.ofNullable(request.getMatterId()))
                .narrative(Optional.ofNullable(request.getNarrative()))
                .durationMinutes(Optional.ofNullable(request.getDurationMinutes()))
                .build();
    }

    /**
     * Maps the aggregate to the API response.
     *
     * @param entry aggregate.
     * @return response DTO.
     */
    public static TimeEntryResponse toResponse(final TimeEntry entry) {
        return TimeEntryResponse.builder()
                .id(entry.getId())
                .customerId(entry.getCustomerId())
                .projectId(entry.getProjectId())
                .matterId(entry.getMatterId())
                .userId(entry.getUserId())
                .narrative(entry.getNarrative())
                .durationMinutes(entry.getDurationMinutes())
                .status(EntryStatus.fromValue(entry.getStatus().name()))
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .approvedAt(entry.getApprovedAt())
                .build();
    }
}
