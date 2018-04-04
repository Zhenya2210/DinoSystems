package org.evgen.dinosystems;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Date;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;


//@RunWith(AllureTestRunner.class)
public class DinosystemsApplicationTests {

    protected static String uriPattern = "http://localhost:8082/time/current?time_offset={timeUTC}";

    @ParameterizedTest
    @ValueSource(strings = {"UTC+18:01", "UTC-18:01", "UTC-009:00",
            "UTC-09:60", "UTC-09:61", "UTC+08:000", "UTC+08:00:30", "ABC+08:00",
            "UTC+08-00", "utc+08:00", "UTC+0800", "UTC08:00", "UTC*08:00", "UTC%2B08:00"})
    public void getTimeUTCWithIncorrectValue(String timeOffset) throws Exception {  //Invalid parameters

        given().
                    pathParam("timeUTC", timeOffset).
                when().
                    get(uriPattern).
                then().
                    assertThat().
                    contentType(ContentType.TEXT).
                    statusCode(HttpStatus.SC_BAD_REQUEST).
                    body(equalTo("Invalid query"));
    }


    @ParameterizedTest
    @ValueSource(strings = {"UTC+08:00", "UTC-05:00", "UTC+18:00", "UTC-18:00",
            "UTC+17:59", "UTC-17:59", "UTC+00:00", "UTC-00:00"})
    public void getTimeUTCWithCorrectValue(String timeOffset) throws Exception {  // Correct parameters

        given().
                    pathParam("timeUTC", timeOffset).
                when().
                    get(uriPattern).
                then().
                    assertThat().
                    contentType(ContentType.JSON).
                    statusCode(HttpStatus.SC_OK);

        Date actualTime = HelperTest.getActualTime(timeOffset); // Date from the response body

        Date expectedTime = HelperTest.getExpectedTime(timeOffset); // Date from the NTP server

        long difference = HelperTest.getDifference(expectedTime, actualTime);

        assertTrue(difference <= 5);
    }
}
