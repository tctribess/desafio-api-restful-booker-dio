package SuiteTests;

import Entities.Booking;
import Entities.BookingDates;
import Entities.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingTests {
    private static Faker faker;
    private static RequestSpecification request;
    private static Booking booking;
    private static BookingDates bookingDates;
    private static User user;
    public static String token = "";

    @BeforeAll
    public static void Setup(){
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
            user = new User(
                    faker.name().firstName(),
                    faker.name().lastName()
            );

        bookingDates = new BookingDates("2023-01-10", "2023-01-10");
        booking = new Booking(
                user.getFirstname(),
                user.getLastname(),
                (float) faker.number().randomDouble(2, 50, 1000),
                true,
                bookingDates,
                ""
        );
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(), new ErrorLoggingFilter());

        }
    @BeforeEach
    void setRequest(){
        request = given().config(RestAssured.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .contentType(ContentType.JSON)
                .auth().basic("admin","password123");
    }
@Order(1)
    @Test
    public void createAuthToken(){
        Map<String, String> body = new HashMap<>();
        body.put("username", "admin");
        body.put("password", "password123");

        token = request
                .header("ContentType", "application/json") //.contentType(ContentType.JSON)
                .when()
                .body(body)
                .post("/auth")
                .then()
                .assertThat()
                .statusCode(200)
                .extract()
                .path("token");
    }

    @Test
    public void getBookingsIds_returnOk(){
        Response response = request
                .when()
                .get("/booking")
                .then()
                .extract()
                .response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.statusCode());
    }
    @Test
    public void getBooking_returnOk(){
            request
                    .when()
                    .get("/booking/" + faker.number()
                            .digits(1))
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .contentType(ContentType.JSON);
    }
    @Test
    public void createBooking_returnOk(){
        Booking test = booking;
                given()
                .contentType(ContentType.JSON)
                .when()
                .body(booking)
                .post("/booking")
                .then()
                .body(matchesJsonSchemaInClasspath("bookingRequestSchema.json"))
                .and()
                .assertThat()
                .statusCode(200);
    }
    @Test
    public void deleteBookingById(){
        request
                .header("Cookie", "token=" + token)
                .when()
                .delete("/booking/" + faker.number()
                        .digits(2))
                .then()
                .assertThat()
                .statusCode(201);
    }
}
