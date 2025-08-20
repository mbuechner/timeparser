package de.ddb.labs.timeparser.timespan;

import java.util.Calendar;
import java.util.GregorianCalendar;

/** A time period between two points in time. Contains the input string that was parsed to generate this period. */
public class TimeSpan {
    private final String parsedInputString;
    private final Calendar start;
    private final Calendar end;

    public TimeSpan(String parsedInputString, Calendar start, Calendar end) {
        this.parsedInputString = parsedInputString;
        this.start = start;
        this.end = end;
    }

    public String getParsedInputString() {
        return this.parsedInputString;
    }

    public Calendar getStart() {
        return this.start;
    }

    public Calendar getEnd() {
        return this.end;
    }

    @Override
    public String toString() {
        String startEra = "AD";
        if ( this.start.get(Calendar.ERA) == GregorianCalendar.BC ) {
            startEra = "BC";
        }
        String endEra = "AD";
        if ( this.end.get(Calendar.ERA) == GregorianCalendar.BC ) {
            endEra = "BC";
        }
        return "TimeSpan[" + this.start.getTime() + " (" + startEra + ") - " + this.end.getTime() + " (" + endEra + "), \"" + this.parsedInputString + "\"]";
    }
}
