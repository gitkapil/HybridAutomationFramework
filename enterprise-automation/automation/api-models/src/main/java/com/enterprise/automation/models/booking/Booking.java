package com.enterprise.automation.models.booking;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A booking payload, matching the restful-booker {@code /booking} contract.
 *
 * @param firstname guest first name
 * @param lastname guest last name
 * @param totalprice total price in whole currency units
 * @param depositpaid whether the deposit has been paid
 * @param bookingdates stay dates
 * @param additionalneeds optional free-text requirements
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Booking(
    String firstname,
    String lastname,
    int totalprice,
    boolean depositpaid,
    BookingDates bookingdates,
    String additionalneeds) {}
