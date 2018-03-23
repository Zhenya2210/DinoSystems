package org.evgen.dinosystems;

import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import ru.yandex.qatools.allure.annotations.Title;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static io.restassured.RestAssured.given;


//@RunWith(AllureTestRunner.class)
public class DinosystemsApplicationTests {


	@ParameterizedTest
	@ValueSource(strings = {"?time_offset=UTC+18:01",
			"?time_offset=UTC-18:01", "?time_offset=UTC-009:00",
			"?time_offset=UTC-09:60", "?time_offset=UTC-09:61" ,"?time_offset=UTC+08:000",
			"?time_offset=UTC+08:00:30", "?time_offset=ABC+08:00",
			"?time_offset=UTC+08-00", "?time_offset=utc+08:00", "?time_offset=UTC+0800",
			"?time_offset=UTC08:00","?time_offset=UTC*08:00", "?time_offset=UTC%2B08:00",
			"?tIMe_OfFsEt=UTC+03:00", "?time_offset:UTC+03:00", "?itstime_offset=UTC+03:00",
			"?time offset=UTC+03:00"})
	public void testNumber1(String parameter) throws Exception {  //Invalid parameters
		RestAssured.baseURI = "http://localhost:8082/time/current";
		RequestSpecification httpRequest = RestAssured.given();
		Response response = httpRequest.get(parameter);
		int a = response.getStatusCode();
		ResponseBody body = response.getBody();
		String frombody = body.asString();
		Assert.assertTrue(a == 400);
		Assert.assertEquals("Invalid query", frombody);
	}


	@ParameterizedTest
	@ValueSource(strings = {"?time_offset=UTC+03:00", "?time_offset=UTC-03:00",
			"?time_offset=UTC+18:00", "?time_offset=UTC-18:00", "?time_offset=UTC+17:59",
			"?time_offset=UTC-17:59","?time_offset=UTC+00:00", "?time_offset=UTC-00:00"
	})
	public void checkGetTimeWithCorrectValue(String timeOffset) throws Exception {  // Correct parameters
		RestAssured.baseURI = "http://localhost:8082/time/current";
		RequestSpecification httpRequest = RestAssured.given();
		String parametrInput = timeOffset.substring(16, 22);
		Response response = httpRequest.get(timeOffset);
		int a = response.getStatusCode();
		Assert.assertTrue(a == 200);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		ResponseBody body = response.getBody();
		String frombody = body.asString();
		frombody = frombody.substring(9, 28);

		Date dateFromBody = df.parse(frombody);

		Headers headers = response.getHeaders();
		String dateOfHeaders = headers.getValue("Date");
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		Date dateFromHeaders = dateFormat.parse(dateOfHeaders);

		ZoneOffset offset = ZoneOffset.of(parametrInput);
		TimeZone tz = TimeZone.getTimeZone(offset);
		DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
		df2.setTimeZone(tz);
		String result = df2.format(dateFromHeaders);

		result = result.substring(0, 19);
		Date dateFromHeadersResult = df.parse(result);

		long difference = (dateFromBody.getTime() - dateFromHeadersResult.getTime()) / 1000;
		difference = Math.abs(difference);

		Assert.assertTrue(difference <= 5);

	}

}
