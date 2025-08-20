package de.ddb.labs.timeparser.timespan;

import java.util.regex.Matcher;

/**
 * Indicates whether an attempt to read a certain string from the input string by {@link InputStringReader} was successful or not. If successful, the part of
 * the input string just read can be returned, as well as the groups from the regular expression, if used.
 */
class AcceptResult {

    private final boolean isAccepted;
    private final String parsedInputString;
    private final Matcher matcher;

    public AcceptResult(boolean isAccepted, String parsedInputString, Matcher matcher) {
        this.isAccepted = isAccepted;
        this.parsedInputString = parsedInputString;
        this.matcher = matcher;
    }

    public boolean isAccepted() {
        return this.isAccepted;
    }

    public String getParsedInputString() {
        return this.parsedInputString;
    }

    public String group(int group) {
        return this.matcher.group(group);
    }

    @Override
    public String toString() {
        return "AcceptResult[" + this.isAccepted + (this.isAccepted ? ": \"" + this.parsedInputString + "\"]" : "]");
    }

}
