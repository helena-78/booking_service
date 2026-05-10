package com.sportlink.scheduling.model;

/**
 * Lifecycle of a time slot
 *   AVAILABLE — slot exists and can be reserved
 *   RESERVED  — slot is currently held by an Activity or Booking
 *   RELEASED  — slot has been released after being reserved (cancelled/refunded)
 */

public enum TimeSlotStatus {
    AVAILABLE,
    RESERVED,
    RELEASED
}