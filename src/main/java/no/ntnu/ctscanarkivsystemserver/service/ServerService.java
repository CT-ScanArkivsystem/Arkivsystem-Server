package no.ntnu.ctscanarkivsystemserver.service;

import no.ntnu.ctscanarkivsystemserver.CtScanArkivsystemServerApplication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ServerService {

    /**
     * Schedule a server restart at the date and time of parameter.
     * @param timeOfRestart date and time of restart formatted as: YYYY-MM-ddThh:mm+0200
     */
    public void scheduleServerRestart(Date timeOfRestart) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        long timeUntilRestart = getTimeUntilRestart(timeOfRestart);
        executorService.schedule(CtScanArkivsystemServerApplication::restart, timeUntilRestart, TimeUnit.MINUTES);
        System.out.println("Server is restarting in ~" + timeUntilRestart + " minutes.");
    }

    /**
     * Gets the time of restart in minutes.
     * @param timeOfRestart date and time of restart.
     * @return time until restart in minutes.
     */
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
