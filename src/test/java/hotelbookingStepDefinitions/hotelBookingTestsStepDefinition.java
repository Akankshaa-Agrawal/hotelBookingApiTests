package hotelbookingStepDefinitions;

import static io.restassured.RestAssured.given;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import com.github.javafaker.Faker;

import Utils.ScenarioContext;
import hotelbookingDataContracts.Authorization;
import hotelbookingDataContracts.Booking;
import hotelbookingDataContracts.Bookingdates;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class hotelBookingTestsStepDefinition {

	ScenarioContext scenarioContext;

	@Before("@HotelBookingTests")
	public void setBaseURI() {
		RestAssured.baseURI = "https://restful-booker.herokuapp.com";
		scenarioContext = new ScenarioContext();
	
	}

	@Given("I create token")
	public void i_create_token() {

		Authorization usrDetails = new Authorization("admin", "password123");

		String response = 
				given()
				.contentType("application/json")
				.body(usrDetails)
				.when()
				.post("/auth")
				.then()
				.assertThat().statusCode(200).extract().response().jsonPath().get("token");
		scenarioContext.put("token", response);
		//System.out.println(response);
	}

	@Given("I create Booking")
	public void i_create_booking() {

		Faker faker = new Faker();

		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = formatter.format(date);

		Bookingdates dts = new Bookingdates(currentDate, currentDate);
		Booking booking = new Booking(faker.name().firstName(), faker.name().lastName(), 111, true, dts, "Breakfast");

		Response response = 
				given()
				.contentType("application/json")
				.body(booking)
				.when()
				.post("/booking")
				.then()
				.assertThat().statusCode(200).extract().response();
		JsonPath path = response.jsonPath();
		scenarioContext.put("createdBookingId", path.get("bookingid"));
		//System.out.println(path.get("bookingid").toString());
	}

	@Then("I get booking by ID")
	public void i_get_booking_byID() {

		Integer bookingId = (Integer) scenarioContext.get("createdBookingId");
		Response response = 
				given()
				.contentType("application/json")
				.pathParam("id", bookingId)
				.when()
				.get("/booking/{id}")
				.then()
				.extract().response();

		int statusCode = response.statusCode();
		if (statusCode == 200) {
			Booking bookingobj = response.jsonPath().getObject("$", Booking.class);
			scenarioContext.put("getbookingObj", bookingobj);
		}

		scenarioContext.put("getbookingbyIDStatusCode", statusCode);
		//System.out.println(response.asString());

	}

	@When("I get all booking IDs")
	public void i_get_all_booking_IDs() {

		Response response = 
				given()
				.contentType("application/json")
				.when()
				.get("/booking")
				.then()
				.assertThat()
				.statusCode(200)
				.extract().response();

		List<Integer> bookingIds = response.jsonPath().getList("bookingid");

		scenarioContext.put("bookingIDs", bookingIds);
		//System.out.println(bookingIds);
	}

	@When("I update booking partially")
	public void i_update_booking_partially(DataTable table) {
		String token = (String) scenarioContext.get("token");
		Integer bookingId = (Integer) scenarioContext.get("createdBookingId");
		Booking bookingObj = (Booking) scenarioContext.get("getbookingObj");
		bookingObj = prepareBookingObjectAsperDataTable(bookingObj, table);

		String response = 
				given()
				.header("Cookie", "token=" + token)
				.contentType("application/json")
				.pathParam("id", bookingId)
				.body(bookingObj)
				.when()
				.patch("/booking/{id}")
				.then()
				.assertThat()
				.statusCode(200)
				.extract()
				.response().asString();
		scenarioContext.put("UpdatedbookingObj", response);
		//System.out.println(response);

	}

	@When("I delete booking")
	public void i_delete_booking() {

		String token = (String) scenarioContext.get("token");
		Integer bookingId = (Integer) scenarioContext.get("createdBookingId");

		String response = 
				given()
				.header("Cookie", "token=" + token)
				.contentType("application/json")
				.pathParam("id", bookingId)
				.when().delete("/booking/{id}")
				.then()
				.assertThat().statusCode(201).extract().response().asString();
		//System.out.println(response);

	}

	@Then("Validate booking details are updated as expected")
	public void validate_booking_details_are_updated_as_expected(DataTable table) {
		Map updatedValues = table.asMap();

		Booking getbookingObj = (Booking) scenarioContext.get("getbookingObj");

		if (updatedValues.containsKey("firstName")) {
			Assert.assertTrue(getbookingObj.getFirstname().equals(updatedValues.get("firstName")));
		}
		if (updatedValues.containsKey("totalPrice")) {
			Assert.assertTrue(getbookingObj.getTotalprice().intValue() == Integer.parseInt((String)updatedValues.get("totalPrice")));
		}
		if (updatedValues.containsKey("lastname")) {

			Assert.assertTrue(getbookingObj.getLastname().equals(updatedValues.get("lastname")));
		}
		if (updatedValues.containsKey("depositpaid")) {
			Assert.assertTrue(getbookingObj.getDepositpaid().equals(updatedValues.get("depositpaid")));
		}

		if (updatedValues.containsKey("additionalneeds")) {
			Assert.assertTrue(getbookingObj.getAdditionalneeds().equals(updatedValues.get("additionalneeds")));
		}

	}

	@Then("Validate above created booking Id should be listed in response of get All booking IDs")
	public void validate_above_created_booking_id_should_be_listed_in_response_of_get_all_booking_i_ds() {
		Integer bookingId = (Integer) scenarioContext.get("createdBookingId");
		List<Integer> empList = (List<Integer>) scenarioContext.get("bookingIDs");
		Assert.assertTrue(empList.contains(bookingId));
	}

	@Then("Validate responseCode is {int}")
	public void validate_response_code_is(Integer statusCode) {
		Integer statusCodeReturned = (Integer) scenarioContext.get("getbookingbyIDStatusCode");
		Assert.assertTrue(statusCode.intValue() == statusCodeReturned.intValue());
	}

	private Booking prepareBookingObjectAsperDataTable(Booking booking, DataTable table) {

		Map map = table.asMap();

		if (map.containsKey("firstName")) {
			String fname = (String) map.get("firstName");
			booking.setFirstname(fname);
		}
		if (map.containsKey("totalPrice")) {
			Integer totalPrice = Integer.parseInt((String) map.get("totalPrice"));
			booking.setTotalprice(totalPrice);
		}
		if (map.containsKey("lastname")) {
			String lastname = (String) map.get("lastname");
			booking.setLastname(lastname);
		}
		if (map.containsKey("depositpaid")) {
			Boolean depositpaid = Boolean.parseBoolean((String) map.get("depositpaid"));
			booking.setDepositpaid(depositpaid);
		}

		if (map.containsKey("additionalneeds")) {
			String additionalneeds = (String) map.get("additionalneeds");
			booking.setAdditionalneeds(additionalneeds);
		}
		if (map.containsKey("additionalneeds")) {
			String additionalneeds = (String) map.get("additionalneeds");
			booking.setAdditionalneeds(additionalneeds);
		}
		if (map.containsKey("checkin") && map.containsKey("checkout")) {
			String checkin = (String) map.get("checkin");
			String checkout = (String) map.get("checkout");
			booking.setBookingdates(new Bookingdates(checkin, checkout));
		}
		return booking;
	}

}
