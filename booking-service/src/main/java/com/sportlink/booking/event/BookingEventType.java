package com.sportlink.booking.event;

/**
 * The four event types the Booking Service emits to Kafka.
 *
 * The mapping below matches the original (RabbitMQ) routing-key plan from
 * Assignment 3, so existing consumers can stay unchanged at the conceptual
 * level - only the transport (RabbitMQ -> Kafka) has changed:
 *
 *   BOOKING_CREATED    <-  booking.created
 *   BOOKING_CONFIRMED  <-  booking.confirmed
 *   BOOKING_CANCELLED  <-  booking.cancelled
 *   PAYMENT_REFUNDED   <-  booking.refunded
 */
public enum BookingEventType {
    BOOKING_CREATED,
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    PAYMENT_REFUNDED
}
