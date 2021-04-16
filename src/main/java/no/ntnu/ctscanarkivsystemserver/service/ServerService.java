package no.ntnu.ctscanarkivsystemserver.service;

import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Service
public class ServerService {

    public boolean scheduleServerRestart(Date timeOfRestart) throws ParseException {
        System.out.println("Restarting!");
        System.out.println(getTimeUntilRestart(timeOfRestart));
        return true;
    }

    private long getTimeUntilRestart(Date timeOfRestart) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));

        Calendar timeOfRestartCal = Calendar.getInstance();
        Calendar timeNow = Calendar.getInstance();
        timeOfRestartCal.setTime(timeOfRestart);
        timeNow.setTime(Date.from(Instant.now()));

        System.out.println("Time now: " + timeNow.getTime() + "\nTime of restart: " + timeOfRestartCal.getTime());

        Date test = timeNow.getTime();
        long timeDifference = timeOfRestart.getTime() - test.getTime();

        TimeUnit time = TimeUnit.MINUTES;
        return time.convert(timeDifference, TimeUnit.MILLISECONDS);
    }
}
