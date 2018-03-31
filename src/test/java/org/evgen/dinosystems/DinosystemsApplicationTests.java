package org.evgen.dinosystems;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.http.HttpStatus;
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

	String uriPattern = "http://localhost:8082/time/current?time_offset={timeUTC}";

	@ParameterizedTest
	@ValueSource(strings = {"UTC+18:01", "UTC-18:01", "UTC-009:00",
			"UTC-09:60", "UTC-09:61" ,"UTC+08:000", "UTC+08:00:30", "ABC+08:00",
			"UTC+08-00", "utc+08:00", "UTC+0800", "UTC08:00","UTC*08:00", "UTC%2B08:00"})
	public void checkGetTimeUTCWithIncorrectValue(String timeOffset) throws Exception {  //Invalid parameters

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
			"UTC+17:59", "UTC-17:59","UTC+00:00", "UTC-00:00"})
	public void checkGetTimeUTCWithCorrectValue(String timeOffset) throws Exception {  // Correct parameters

		String parameterInput = timeOffset.substring(3, timeOffset.length());

		given().
					pathParam("timeUTC", timeOffset).
				when().
					get(uriPattern).
				then().
					assertThat().
					contentType(ContentType.JSON).
					statusCode(HttpStatus.SC_OK);

		String jsonString = given().
					pathParam("timeUTC", timeOffset).
					accept(ContentType.JSON).
				when().
					get(uriPattern).
				thenReturn().
					asString();
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
