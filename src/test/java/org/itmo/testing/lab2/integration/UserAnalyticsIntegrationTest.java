package org.itmo.testing.lab2.integration;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.itmo.testing.lab2.controller.UserAnalyticsController;
import org.junit.jupiter.api.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import java.time.YearMonth;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserAnalyticsIntegrationTest {

    private Javalin app;
    private int port = 7000;

    @BeforeAll
    void setUp() {
        app = UserAnalyticsController.createApp();
        app.start(port);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterAll
    void tearDown() {
        app.stop();
    }

    @Test
    @Order(1)
    @DisplayName("Тест регистрации пользователя")
    void testUserRegistration() {
        given()
                .queryParam("userId", "user1")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(equalTo("User registered: true"));
    }

    @Test
    @Order(2)
    @DisplayName("Тест записи сессии")
    void testRecordSession() {
        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", now.minusHours(1).toString())
                .queryParam("logoutTime", now.toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200)
                .body(equalTo("Session recorded"));
    }

    @Test
    @Order(3)
    @DisplayName("Тест получения общего времени активности")
    void testGetTotalActivity() {
        given()
                .queryParam("userId", "user1")
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(200)
                .body(containsString("Total activity:"))
                .body(containsString("minutes"));
    }

    @Test
    @Order(4)
    void testGetTotalActivityMissingParameter() {
        given()
                .when()
                .get("/totalActivity")
                .then()
                .statusCode(400)
                .body(containsString("Missing userId"));
    }

    @Test
    @Order(5)
    void testRegistrationMissingParameters() {
        given()
                .when()
                .post("/register")
                .then()
                .statusCode(400)
                .body(containsString("Missing parameters"));
    }

    @Test
    @Order(6)
    void testRecordSessionInvalidDates() {
        given()
                .queryParam("userId", "user1")
                .queryParam("loginTime", "invalid")
                .queryParam("logoutTime", "invalid")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data"));
    }

    @Test
    @Order(7)
    void testRecordSessionMissingParams() {
        given()
                .queryParam("userId", "user1")
                .when()
                .post("/recordSession")
                .then()
                .statusCode(400)
                .body(containsString("Missing parameters"));
    }

    @Test
    @Order(8)
    void testInactiveUsersValid() {
        given()
                .queryParam("days", 1)
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(200)
                .body(notNullValue());
    }

    @Test
    @Order(9)
    void testInactiveUsersMissingParameter() {
        given()
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(containsString("Missing days parameter"));
    }

    @Test
    @Order(10)
    void testInactiveUsersInvalidDays() {
        given()
                .queryParam("days", "abc")
                .when()
                .get("/inactiveUsers")
                .then()
                .statusCode(400)
                .body(containsString("Invalid number format for days"));
    }

    @Test
    @Order(11)
    void testMonthlyActivityMissingParameters() {
        given()
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(containsString("Missing parameters"));
    }

    @Test
    @Order(12)
    void testMonthlyActivityInvalidData() {
        given()
                .queryParam("userId", "user2")
                .queryParam("month", "someMonth")
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(400)
                .body(containsString("Invalid data: Text 'someMonth' could not be parsed at index 0"));
    }

    @Test
    @Order(13)
    void testMonthlyActivityValid() {
        given()
                .queryParam("userId", "user2")
                .queryParam("userName", "Alice")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(containsString("User registered: true"));

        LocalDateTime now = LocalDateTime.now();
        given()
                .queryParam("userId", "user2")
                .queryParam("loginTime", now.minusDays(1).toString())
                .queryParam("logoutTime", now.minusDays(1).plusHours(1).toString())
                .when()
                .post("/recordSession")
                .then()
                .statusCode(200);

        YearMonth month = YearMonth.now();
        given()
                .queryParam("userId", "user2")
                .queryParam("month", month.toString())
                .when()
                .get("/monthlyActivity")
                .then()
                .statusCode(200)
                .body(notNullValue());
    }
}
