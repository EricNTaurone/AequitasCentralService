package com.aequitas.aequitascentralservice.adapter.web;

import com.aequitas.aequitascentralservice.adapter.web.dto.CreateTimeEntryRequest;
import com.aequitas.aequitascentralservice.adapter.web.dto.TimeEntryResponse;
import com.aequitas.aequitascentralservice.adapter.web.dto.UpdateTimeEntryRequest;
import com.aequitas.aequitascentralservice.domain.command.CreateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.command.UpdateTimeEntryCommand;
import com.aequitas.aequitascentralservice.domain.model.TimeEntry;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Maps REST DTOs to domain commands and aggregates.
 */
@Component
public class TimeEntryDtoMapper {

    /**
     * Converts the request body into a create command.
     *
     * @param request REST payload.
     * @return domain command.
     */
    public CreateTimeEntryCommand toCommand(final CreateTimeEntryRequest request) {
        return new CreateTimeEntryCommand(
                request.customerId(),
                request.projectId(),
                request.matterId(),
                request.narrative(),
                request.durationMinutes());
    }

    /**
     * Converts the DTO into an update command.
     *
     * @param request update payload.
     * @return domain command with optional fields.
     */
    public UpdateTimeEntryCommand toCommand(final UpdateTimeEntryRequest request) {
        return new UpdateTimeEntryCommand(
                Optional.ofNullable(request.customerId()),
                Optional.ofNullable(request.projectId()),
                Optional.ofNullable(request.matterId()),
                Optional.ofNullable(request.narrative()),
                Optional.ofNullable(request.durationMinutes()));
    }

    /**
     * Maps the aggregate to the API response.
     *
     * @param entry aggregate.
     * @return response DTO.
     */
    public TimeEntryResponse toResponse(final TimeEntry entry) {
        return new TimeEntryResponse(
                entry.id(),
                entry.customerId(),
                entry.projectId(),
                entry.matterId(),
                entry.userId(),
                entry.narrative(),
                entry.durationMinutes(),
                entry.status(),
                entry.createdAt(),
                entry.updatedAt(),
                entry.approvedAt());
    }
}
