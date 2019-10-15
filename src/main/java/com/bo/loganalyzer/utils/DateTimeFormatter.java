package com.bo.loganalyzer.utils;

import com.bo.loganalyzer.exception.DateParseException;
import org.springframework.format.datetime.DateFormatter;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeFormatter {

    public static String getCurrentDateTime(){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return formatter.format(new Date());
    }

    public static Date formatDate(String date){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            throw new DateParseException(e.getMessage());
        }
    }

    public static Timestamp formatTime(String date){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date dateTime = null;
        try {
            dateTime = formatter.parse(date);
        } catch (ParseException e) {
            throw new DateParseException(e.getMessage());
        }
        return new Timestamp(dateTime.getTime());
    }
}
