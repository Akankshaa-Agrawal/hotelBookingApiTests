@HotelBookingTests

Feature: Hotel Booking Feature
  I want to use this feature file for testing hotel booking scenarios

Background:
 Given I create token
 And I create Booking


@RegressionTest
  Scenario: Test Partial Booking Update
    Given I get booking by ID
    When I update booking partially
    |firstName|Johny|
    |totalPrice|333|
    Then I get booking by ID
    And Validate booking details are updated as expected
    |firstName|Johny|
    |totalPrice|333|
    

@RegressionTest
  Scenario: Test Get All Booking IDs without Filter
    When I get all booking IDs
    Then Validate above created booking Id should be listed in response of get All booking IDs

@RegressionTest
  Scenario: Test Delete Booking endpoint
    When I delete booking
    Then I get booking by ID
    And Validate responseCode is 404
