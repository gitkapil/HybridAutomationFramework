package com.enterprise.automation.tests.api.booking;

import com.enterprise.automation.models.booking.Booking;
import com.enterprise.automation.models.booking.BookingDates;
import java.util.UUID;

/**
 * Builds unique booking payloads so that tests never collide with each other or with existing data.
 */
public final class BookingTestData {

  private BookingTestData() {}

  /**
   * @return a booking with a unique guest name and fixed, valid stay dates
   */
  public static Booking randomBooking() {
    String unique = UUID.randomUUID().toString().substring(0, 8);
    return new Booking(
        "Test" + unique,
        "Auto" + unique,
        150,
        true,
        new BookingDates("2025-01-10", "2025-01-15"),
        "Late check-in");
  }
}
