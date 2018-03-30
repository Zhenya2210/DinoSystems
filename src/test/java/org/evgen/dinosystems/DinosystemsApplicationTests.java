package org.evgen.dinosystems;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;


//@RunWith(AllureTestRunner.class)
public class DinosystemsApplicationTests {

	@BeforeAll
	public static void setUP(){
		RestAssured.baseURI = "http://localhost:8082/time/current";
	}

	@ParameterizedTest
	@ValueSource(strings = {"?time_offset=UTC+18:01",
			"?time_offset=UTC-18:01", "?time_offset=UTC-009:00",
			"?time_offset=UTC-09:60", "?time_offset=UTC-09:61" ,"?time_offset=UTC+08:000",
			"?time_offset=UTC+08:00:30", "?time_offset=ABC+08:00",
			"?time_offset=UTC+08-00", "?time_offset=utc+08:00", "?time_offset=UTC+0800",
			"?time_offset=UTC08:00","?time_offset=UTC*08:00", "?time_offset=UTC%2B08:00",
			"?tIMe_OfFsEt=UTC+03:00", "?time_offset:UTC+03:00", "?itstime_offset=UTC+03:00",
			"?time offset=UTC+03:00"})
	public void checkGetTimeUTCWithIncorrectValue(String timeOffset) throws Exception {  //Invalid parameters

		given().
				when().
					get(timeOffset).
				then().
					assertThat().
					contentType(ContentType.TEXT).
					statusCode(HttpStatus.SC_BAD_REQUEST).
					body(equalTo("Invalid query"));
	}


	@ParameterizedTest
	@ValueSource(strings = {"?time_offset=UTC+08:00", "?time_offset=UTC-05:00",
			"?time_offset=UTC+18:00", "?time_offset=UTC-18:00", "?time_offset=UTC+17:59",
			"?time_offset=UTC-17:59","?time_offset=UTC+00:00", "?time_offset=UTC-00:00"
	})
	public void checkGetTimeUTCWithCorrectValue(String timeOffset) throws Exception {  // Correct parameters

		String parameterInput = timeOffset.substring(16, 22);

		given().
				when().
					get(timeOffset).
				then().
					assertThat().
					contentType(ContentType.JSON).
					statusCode(HttpStatus.SC_OK);

		String jsonString = given()
				.accept(ContentType.JSON)
				.when()
					.get(timeOffset)
				.thenReturn()
					.asString();
		JsonPath json = new JsonPath(jsonString);
		String frombody = json.getString("time");

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		Date dateFromBody = df.parse(frombody); // Date from response body

		String TIME_SERVER = "ntp4.stratum2.ru";
		NTPUDPClient timeClient = new NTPUDPClient();
		InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
		TimeInfo timeInfo = timeClient.getTime(inetAddress);
		long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
		Date dateNTP = new Date(returnTime);
		ZoneOffset offset = ZoneOffset.of(parameterInput);
		TimeZone tz = TimeZone.getTimeZone(offset);
		df.setTimeZone(tz);
		String dateNTPString = df.format(dateNTP);
		dateNTP = df.parse(dateNTPString); // Date from the NTP server with the parameter UTC

		long difference = (dateFromBody.getTime() - dateNTP.getTime()) / 1000;
		difference = Math.abs(difference);
		assertTrue(difference <= 5);
	}
}
