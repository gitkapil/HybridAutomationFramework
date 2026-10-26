package com.enterprise.automation.models.booking;

import java.util.UUID;

/**
 * Fluent builder for {@link Booking} payloads. Names are made unique with a UUID fragment so
 * parallel tests never collide on data. Defaults produce a valid booking that a test can override
 * field by field.
 */
public final class BookingBuilder {

  private String firstname = "Test";
  private String lastname = "Guest-" + UUID.randomUUID().toString().substring(0, 8);
  private int totalprice = 150;
  private boolean depositpaid = true;
  private BookingDates bookingdates = new BookingDates("2030-01-01", "2030-01-05");
  private String additionalneeds;

  private BookingBuilder() {}

  /**
   * @return a new builder pre-filled with valid defaults
   */
  public static BookingBuilder aBooking() {
    return new BookingBuilder();
  }

  public BookingBuilder firstname(String value) {
    this.firstname = value;
    return this;
  }

  public BookingBuilder lastname(String value) {
    this.lastname = value;
    return this;
  }

  public BookingBuilder totalprice(int value) {
    this.totalprice = value;
    return this;
  }

  public BookingBuilder depositpaid(boolean value) {
    this.depositpaid = value;
    return this;
  }

  public BookingBuilder stay(String checkin, String checkout) {
    this.bookingdates = new BookingDates(checkin, checkout);
    return this;
  }

  public BookingBuilder additionalneeds(String value) {
    this.additionalneeds = value;
    return this;
  }

  /**
   * @return an immutable booking built from the current state
   */
  public Booking build() {
    return new Booking(firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
  }
}
