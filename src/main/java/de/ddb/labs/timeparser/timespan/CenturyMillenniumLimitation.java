package de.ddb.labs.timeparser.timespan;

public class CenturyMillenniumLimitation {

    private final String parsedInputString;
    private final Integer number;
    private final LimitationType limitation;

    public enum LimitationType {
        QUARTER, THIRD, HALF, DECADE, START, MIDDLE, END
    }

    public CenturyMillenniumLimitation(String parsedInputString, Integer number, LimitationType limitation) {
        this.parsedInputString = parsedInputString;
        this.number = number;
        this.limitation = limitation;
    }

    public String getParsedInputString() {
        return this.parsedInputString;
    }

    public Integer getNumber() {
        return this.number;
    }

    public LimitationType getLimitation() {
        return this.limitation;
    }
}
