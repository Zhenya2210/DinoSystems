package org.evgen.dinosystems;

import io.restassured.RestAssured;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.qatools.allure.annotations.Title;


//@RunWith(AllureTestRunner.class)
public class DinosystemsApplicationTests {

	@ParameterizedTest
	@ValueSource(strings = {"?time_offset=UTC+18:01",
			"?time_offset=UTC-18:01", "?time_offset=UTC-009:00",
			"?time_offset=UTC-09:60", "?time_offset=UTC-09:61" ,"?time_offset=UTC+08:000",
			"?time_offset=UTC+08:00:30", "?time_offset=ABC+08:00",
			"?time_offset=UTC+08-00", "?time_offset=utc+08:00", "?time_offset=UTC+0800"})
	public void testNumber1(String parameter) throws Exception {  //Invalid parameters
		RestAssured.baseURI = "http://localhost:8081/time/current";
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
			"?time_offset=UTC-17:59","?time_offset=UTC+00:00", "?time_offset=UTC-00:00",
	})
	public void testNumber2(String parameter) throws Exception {  // Correct parameters
		RestAssured.baseURI = "http://localhost:8081/time/current";
		RequestSpecification httpRequest = RestAssured.given();
		String parametrInput = parameter.substring(16, 22);
		Response response = httpRequest.get(parameter);
		int a = response.getStatusCode();
		ResponseBody body = response.getBody();
		String frombody = body.asString();
		Headers headers = response.getHeaders();
		String dateOfHeaders = headers.getValue("Date");
		String expectedValue = ConverterDateOfHeaders.dateOfHeadersToISO(dateOfHeaders, parametrInput);
		expectedValue = "{\"time\":\"" + expectedValue + "\"}";
		Assert.assertTrue(a == 200);
		if (expectedValue.equals(frombody)){
			Assert.assertEquals(expectedValue, frombody);}
		else{
			expectedValue = ConverterDateOfHeaders.pogreshnost(dateOfHeaders, parametrInput);
			expectedValue = "{\"time\":\"" + expectedValue + "\"}";
			Assert.assertEquals(expectedValue, frombody);
		}

	}

}
