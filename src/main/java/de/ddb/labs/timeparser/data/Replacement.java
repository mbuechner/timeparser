package de.ddb.labs.timeparser.data;

/**
 * Contains a string that needs to be replaced and a string by which it should be replaced. Used to convert textual months and weekdays to numbers.
 */
public class Replacement {
    public final String from;
    public final String to;

    public Replacement(String from, String to) {
        this.from = from;
        this.to = to;
    }
}
