package de.ddb.labs.timeparser.timespan;

public class Range {

    private final String parsedInputString;
    private final RangeType type;

    public enum RangeType {
        FROM, BEFORE, UNTIL, AFTER, AROUND, PRESUMABLY
    }

    public Range(String parsedInputString, RangeType type) {
        this.parsedInputString = parsedInputString;
        this.type = type;
    }

    public String getParsedInputString() {
        return this.parsedInputString;
    }

    public RangeType getType() {
        return this.type;
    }
}
