package org.evgen.dinosystems.controller.dto;

import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

public class TimeResponse {

    private String nowAsISO;

    public TimeResponse() {
    }

    public TimeResponse(Date time, String timeOffset) {
        int i = timeOffset.length();
        if (i != 9) {
            throw new IllegalArgumentException("Invalid query");
        }
        String s2 = timeOffset.substring(0, 3);
        if (!s2.equals("UTC")) {
            throw new IllegalArgumentException("Invalid query");
        }
//      time = DateUtils.addSeconds(time, 30);   //For failed test
        String utc = timeOffset.substring(3, 9);
        ZoneOffset offset = ZoneOffset.of(utc);
        TimeZone tz = TimeZone.getTimeZone(offset);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        df.setTimeZone(tz);
        nowAsISO = df.format(time);

    }

    public void setTime() {

    }

    public String getTime() {
        return nowAsISO;
    }


}
