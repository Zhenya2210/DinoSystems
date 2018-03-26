package org.evgen.dinosystems;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;



//@RunWith(AllureTestRunner.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DinosystemsApplicationTests {

	@BeforeAll
	public void setUP(){
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
		RequestSpecification httpRequest = RestAssured.given();
		Response response = httpRequest.get(timeOffset);
		int statusCode = response.getStatusCode();
		ResponseBody body = response.getBody();
		String frombody = body.asString();
		Assert.assertTrue(statusCode == 400);
		Assert.assertEquals("Invalid query", frombody);
	}


	@ParameterizedTest
	@ValueSource(strings = {"?time_offset=UTC+08:00", "?time_offset=UTC-05:00",
			"?time_offset=UTC+18:00", "?time_offset=UTC-18:00", "?time_offset=UTC+17:59",
			"?time_offset=UTC-17:59","?time_offset=UTC+00:00", "?time_offset=UTC-00:00"
	})
	public void checkGetTimeUTCWithCorrectValue(String timeOffset) throws Exception {  // Correct parameters
		RequestSpecification httpRequest = RestAssured.given();
		String parameterInput = timeOffset.substring(16, 22);
		Response response = httpRequest.get(timeOffset);
		int statusCode = response.getStatusCode();
		Assert.assertTrue(statusCode == 200);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		ResponseBody body = response.getBody();
		String frombody = body.asString();
		frombody = frombody.substring(9, frombody.length() - 2);
		Date dateFromBody = df.parse(frombody); // Date from response body

//		Date dateFromSystem = new Date();
//		ZoneOffset offset = ZoneOffset.of(parameterInput);
//		TimeZone tz = TimeZone.getTimeZone(offset);
//		df.setTimeZone(tz);
//		String dateFromSystemString = df.format(dateFromSystem);
//		dateFromSystem = df.parse(dateFromSystemString); // Date from the system with the parameter UTC

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
		Assert.assertTrue(difference <= 5);
	}
}
