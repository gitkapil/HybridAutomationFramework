package com.enterprise.automation.models.booking;

/**
 * Response of {@code POST /booking}: the generated id together with the echoed booking.
 *
 * @param bookingid generated booking identifier
 * @param booking the booking as stored
 */
public record BookingReference(int bookingid, Booking booking) {}
