package de.ddb.labs.timeparser.timespan;

public class Operator {

    private final String parsedInputString;
    private final OperatorType type;

    public enum OperatorType {
        OR, BETWEEN
    }

    public Operator(String parsedInputString, OperatorType type) {
        this.parsedInputString = parsedInputString;
        this.type = type;
    }

    public String getParsedInputString() {
        return this.parsedInputString;
    }

    public OperatorType getType() {
        return this.type;
    }
}
