package de.ddb.labs.timeparser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main(String[] args) {
        log.info("Hello World!");
        log.info("Time is {}", TimeParser.getInstance().parseTime("-20000-02-21"));
    }
}
