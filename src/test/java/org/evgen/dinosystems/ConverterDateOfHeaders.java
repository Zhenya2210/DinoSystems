package org.evgen.dinosystems;


import org.apache.commons.lang3.time.DateUtils;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ConverterDateOfHeaders {

    public static String dateOfHeadersToISO(String dateOfHeaders, String parametrInput) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

        Date date = dateFormat.parse(dateOfHeaders);
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        ZoneOffset offset = ZoneOffset.of(parametrInput);
        TimeZone tz = TimeZone.getTimeZone(offset);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        df.setTimeZone(tz);
        String result = df.format(date);

    return result;

    }

    public static String pogreshnost(String dateOfHeaders, String parametrInput) throws ParseException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        Date date = dateFormat.parse(dateOfHeaders);
        date = DateUtils.addSeconds(date, 1);
        ZoneOffset offset = ZoneOffset.of(parametrInput);
        TimeZone tz = TimeZone.getTimeZone(offset);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        df.setTimeZone(tz);
        String result = df.format(date);
        return result;
    }
}
