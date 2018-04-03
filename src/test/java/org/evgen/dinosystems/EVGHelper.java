package org.evgen.dinosystems;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

import static io.restassured.RestAssured.given;
import static org.evgen.dinosystems.DinosystemsApplicationTests.uriPattern;

public class EVGHelper {

    private EVGHelper(){}

    public static Date getActualTime(String timeOffset) throws ParseException {

        String json = given().
                        pathParam("timeUTC", timeOffset).
                        accept(ContentType.JSON).
                    when().
                        get(uriPattern).
                    thenReturn().
                        asString();

        JsonPath jsonPath = new JsonPath(json);
        String timeStringFromBody = jsonPath.getString("time");

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date actualTime = df.parse(timeStringFromBody);

        return actualTime;
    }

    public static Date getExpectedTime(String timeOffset) throws IOException, ParseException {

        String parameterInput = timeOffset.substring(3, timeOffset.length());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        String TIME_SERVER = "ntp2.vniiftri.ru";
        NTPUDPClient timeClient = new NTPUDPClient();
        InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
        TimeInfo timeInfo = timeClient.getTime(inetAddress);
        long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
        Date dateNTP = new Date(returnTime);
        ZoneOffset offset = ZoneOffset.of(parameterInput);
        TimeZone tz = TimeZone.getTimeZone(offset);
        df.setTimeZone(tz);
        String dateNTPString = df.format(dateNTP);
        Date expectedTime = df.parse(dateNTPString);

        return expectedTime;
    }

    public static long getDifference(Date expectedTime, Date actualTime){

        long difference = (actualTime.getTime() - expectedTime.getTime()) / 1000;
        difference = Math.abs(difference);

        return difference;
    }
}
