package hotelbookingStepDefinitions;

import static io.restassured.RestAssured.given;

import java.util.List;

import org.junit.Assert;

import Utils.ScenarioContext;
import hotelbookingDataContracts.Authorization;
import hotelbookingDataContracts.Booking;
import hotelbookingDataContracts.Bookingdates;
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
    public void setBaseURI()
    {
    	RestAssured.baseURI="https://restful-booker.herokuapp.com";
    	scenarioContext = new ScenarioContext();
    }
	
	
	@Given("I create token")
	public void i_create_token() {
	    
		Authorization usrDetails = new Authorization("admin","password123");
		
		String response = 
		given().
        contentType("application/json").
        body(usrDetails).
        when().
        	post("/auth").
        then().
        assertThat().
        statusCode(200).
        extract().response().jsonPath().get("token");
		scenarioContext.put("token",response);
		System.out.println(response);
	}

	
	@Given("I create Booking")
	public void i_create_booking() {
		Bookingdates dts = new Bookingdates("2022-09-09","2022-09-10");
		Booking booking = new Booking("Jim","Brown",111,true,dts,"Breakfast");
		
		Response response = 
				given().
		        contentType("application/json").
		        body(booking).
		        when().
		        	post("/booking").
		        then().
		        assertThat().
		        statusCode(200).
		        extract().response();
		        JsonPath path = response.jsonPath();
				scenarioContext.put("createdBookingId",path.get("bookingid"));
				System.out.println(path.get("bookingid").toString());
	}
	
	@Then("I get booking by ID")
	public void i_get_booking_byID() {
		
		Integer bookingId = (Integer) scenarioContext.get("createdBookingId");
		Response response = 
				given().
		        contentType("application/json").
		        pathParam("id",bookingId).
		        when().
		        	get("/booking/{id}").
		        then().
		        assertThat().
		        extract().response();
		    
				
				int statusCode = response.statusCode();
				if(statusCode == 200) {
					Booking bookingobj = response.jsonPath().getObject("$", Booking.class);
					scenarioContext.put("getbookingObj",bookingobj);
				}
				
				
				scenarioContext.put("getbookingbyIDStatusCode",statusCode);
				System.out.println(response.asString());
				
				
	}
	
	@When("I get all booking IDs")
	public void i_get_all_booking_IDs() {
		
		
		Response response = 
				given().
		        contentType("application/json").
		        when().
		        	get("/booking").
		        then().
		        assertThat().
		        statusCode(200).
		        extract().response();
		
		List<Integer> bookingIds = response.jsonPath().getList("bookingid");
		
		        scenarioContext.put("bookingIDs",bookingIds);
				System.out.println(bookingIds);
	}
	
	@When("I update booking partially {string} and {int}")
	public void i_update_booking_partially(String firstName , int totalPrice) {
		
		String token = (String)scenarioContext.get("token");
		System.out.println(token);
		Integer bookingId = (Integer) scenarioContext.get("createdBookingId");
		System.out.println(bookingId);
		Booking booking = (Booking)scenarioContext.get("getbookingObj");
		booking.setTotalprice(totalPrice);
		booking.setFirstname(firstName);
		
		String response = 
				given().header("Cookie","token="+token).
				contentType("application/json").
		        pathParam("id",bookingId).
		        body(booking).
		        when().
		        	patch("/booking/{id}").
		        then().
		        assertThat().
		        statusCode(200).
		        extract().response().asString();
				scenarioContext.put("UpdatedbookingObj",response);
				System.out.println(response);
	
	
	}
	
	
	@When("I delete booking")
	public void i_delete_booking() {
		
		String token = (String)scenarioContext.get("token");
		Integer bookingId = (Integer) scenarioContext.get("createdBookingId");
		
		String response = 
				given().header("Cookie","token="+token).
				
		        contentType("application/json").
		        pathParam("id",bookingId).
		        when().
		        	delete("/booking/{id}").
		        then().
		        assertThat().
		        statusCode(201).
		        extract().response().asString();
				System.out.println(response);
	
	
	}
	
	@Then("Validate booking details are updated as expected {string} and {int}")
	public void validate_booking_details_are_updated_as_expected(String firstName , int totalPrice) {
		Booking booking = (Booking) scenarioContext.get("getbookingObj");
		Assert.assertTrue (booking.getFirstname().equals(firstName) );
		Assert.assertTrue (booking.getTotalprice() == totalPrice );
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






	

}
