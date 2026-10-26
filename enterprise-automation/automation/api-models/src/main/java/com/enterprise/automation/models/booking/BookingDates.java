package com.enterprise.automation.models.booking;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Booking dates as sent to and returned by the booking API.
 *
 * @param checkin check-in date in {@code yyyy-MM-dd} format
 * @param checkout check-out date in {@code yyyy-MM-dd} format
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingDates(String checkin, String checkout) {}
