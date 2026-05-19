package com.sportlink.scheduling.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportlink.scheduling.dto.AvailabilityWindowDto;
import com.sportlink.scheduling.dto.ReserveTimeSlotRequest;
import com.sportlink.scheduling.dto.ResolveTimeSlotsRequest;
import com.sportlink.scheduling.dto.TimeSlotDto;
import com.sportlink.scheduling.dto.UpdateAvailabilityRequest;
import com.sportlink.scheduling.exception.InvalidTimeRangeException;
import com.sportlink.scheduling.exception.TimeSlotConflictException;
import com.sportlink.scheduling.exception.TimeSlotNotFoundException;
import com.sportlink.scheduling.model.AvailabilityWindow;
import com.sportlink.scheduling.model.TimeSlot;
import com.sportlink.scheduling.model.TimeSlotStatus;
import com.sportlink.scheduling.repository.AvailabilityWindowRepository;
import com.sportlink.scheduling.repository.TimeSlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulingService {

    private final TimeSlotRepository timeSlotRepository;
    private final AvailabilityWindowRepository availabilityWindowRepository;

    // ============================================================
    //  TimeSlots
    // ============================================================

    /**
     * Find slots in status AVAILABLE within the given window.
     * Used by GET /api/timeslots/available.
     */
    @Transactional(readOnly = true)
    public List<TimeSlotDto> findAvailableSlots(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new InvalidTimeRangeException(
                    "to must be after from (got from=" + from + ", to=" + to + ")");
        }

        // If client doesn't pass a range, fall back to "now → now + 30 days".
        LocalDateTime effectiveFrom = (from != null) ? from : LocalDateTime.now();
        LocalDateTime effectiveTo = (to != null) ? to : effectiveFrom.plusDays(30);

        return timeSlotRepository
                .findByStatusAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
                        TimeSlotStatus.AVAILABLE, effectiveFrom, effectiveTo)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeSlotDto> findAllSlots() {
        return timeSlotRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Reserve a new time slot.
     * Used by POST /api/timeslots/reserve.
     *
     * In this minimal implementation the server creates a *new* slot
     * with status RESERVED on every call. For
     * Activity Management ↔ Scheduling integration the create-and-reserve
     * pattern is enough.
     */
    @Transactional
    public TimeSlotDto reserveTimeSlot(ReserveTimeSlotRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new InvalidTimeRangeException(
                    "endTime must be strictly after startTime");
        }

        TimeSlot slot = TimeSlot.builder()
                .slotId(UUID.randomUUID())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(TimeSlotStatus.RESERVED)
                .organizerId(request.getOrganizerId())
                .reservedForId(request.getReservedForId())
                .build();

        TimeSlot saved = timeSlotRepository.save(slot);
        log.info("Reserved time slot {} for {} (organizer {})",
                saved.getSlotId(), saved.getReservedForId(), saved.getOrganizerId());
        return toDto(saved);
    }

    /**
     * Release a reserved time slot.
     * Used by DELETE /api/timeslots/{id}/release.
     */
    @Transactional
    public void releaseTimeSlot(UUID slotId) {
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new TimeSlotNotFoundException(
                        "Time slot " + slotId + " not found"));

        
        if (slot.getStatus() == TimeSlotStatus.AVAILABLE) {
            throw new TimeSlotConflictException(
                    "Cannot release an AVAILABLE slot — only RESERVED slots can be released");
        }

        slot.setStatus(TimeSlotStatus.AVAILABLE);
        slot.setReservedForId(null); // free the logical reference
        timeSlotRepository.save(slot);
        log.info("Released time slot {} (now AVAILABLE)", slotId);
    }

    // ============================================================
    //  Availability windows
    // ============================================================

    /**
     * Read all availability windows of a user.
     * Used by GET /api/timeslots/users/{userId}/availability.
     */
    @Transactional(readOnly = true)
    public List<AvailabilityWindowDto> getUserAvailability(UUID userId) {
        return availabilityWindowRepository.findByUserId(userId)
                .stream()
                .map(this::toAvailabilityDto)
                .toList();
    }

    /**
     * Replace the user's full availability schedule with the new list.
     * Used by PUT /api/timeslots/users/{userId}/availability.
     *
     * Any windows the client did not include are
     * removed. This is simpler and atomic — a single PUT fully describes
     * the user's weekly schedule.
     */
    @Transactional
    public List<AvailabilityWindowDto> updateUserAvailability(UUID userId,
                                                              UpdateAvailabilityRequest request) {
        // Validate every window before touching the DB.
        for (AvailabilityWindowDto w : request.getWindows()) {
            if (!w.getTimeRangeTo().isAfter(w.getTimeRangeFrom())) {
                throw new InvalidTimeRangeException(
                        "timeRangeTo must be strictly after timeRangeFrom");
            }
        }

        // Wipe the old schedule.
        availabilityWindowRepository.deleteByUserId(userId);

        // Persist the new one.
        List<AvailabilityWindow> saved = new ArrayList<>();
        for (AvailabilityWindowDto w : request.getWindows()) {
            AvailabilityWindow entity = AvailabilityWindow.builder()
                    .windowId(UUID.randomUUID())
                    .userId(userId)
                    .timeRangeFrom(w.getTimeRangeFrom())
                    .timeRangeTo(w.getTimeRangeTo())
                    .dayOfWeek(w.getDayOfWeek())
                    .build();
            saved.add(availabilityWindowRepository.save(entity));
        }

        log.info("Updated availability of user {}: {} window(s)", userId, saved.size());
        return saved.stream().map(this::toAvailabilityDto).toList();
    }

    /**
     * Find time windows during which all listed users are simultaneously
     * available. Used by POST /api/timeslots/resolve (called by Matching).
     *
     * Minimal implementation: returns all windows that appear (by
     * dayOfWeek + overlapping time range) in *every* user's schedule.
     * Good enough for Checkpoint 2 — a richer implementation can be
     * added later.
     */
    @Transactional(readOnly = true)
    public List<AvailabilityWindowDto> resolveTimeSlots(ResolveTimeSlotsRequest request) {
        List<UUID> userIds = request.getUserIds();
        if (userIds.isEmpty()) {
            return List.of();
        }

        // Start with the first user's windows as the candidate set.
        List<AvailabilityWindow> candidates =
                availabilityWindowRepository.findByUserId(userIds.get(0));

        // Keep only those that have an overlapping window in every other user.
        List<AvailabilityWindow> overlap = new ArrayList<>(candidates);
        for (int i = 1; i < userIds.size(); i++) {
            List<AvailabilityWindow> other =
                    availabilityWindowRepository.findByUserId(userIds.get(i));
            overlap.removeIf(w -> other.stream().noneMatch(o -> overlaps(w, o)));
        }

        return overlap.stream().map(this::toAvailabilityDto).toList();
    }

    // ============================================================
    //  Helpers
    // ============================================================

    /**
     * Two windows overlap when they fall on the same day of the week and
     * their time ranges intersect (any non-empty overlap counts).
     */
    private boolean overlaps(AvailabilityWindow a, AvailabilityWindow b) {
        if (a.getDayOfWeek() != b.getDayOfWeek()) {
            return false;
        }
        return a.getTimeRangeFrom().isBefore(b.getTimeRangeTo())
                && b.getTimeRangeFrom().isBefore(a.getTimeRangeTo());
    }

    private TimeSlotDto toDto(TimeSlot ts) {
        return TimeSlotDto.builder()
                .slotId(ts.getSlotId())
                .startTime(ts.getStartTime())
                .endTime(ts.getEndTime())
                .status(ts.getStatus())
                .organizerId(ts.getOrganizerId())
                .reservedForId(ts.getReservedForId())
                .build();
    }

    private AvailabilityWindowDto toAvailabilityDto(AvailabilityWindow w) {
        return AvailabilityWindowDto.builder()
                .windowId(w.getWindowId())
                .timeRangeFrom(w.getTimeRangeFrom())
                .timeRangeTo(w.getTimeRangeTo())
                .dayOfWeek(w.getDayOfWeek())
                .build();
    }
}