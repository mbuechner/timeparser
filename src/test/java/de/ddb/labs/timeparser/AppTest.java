package de.ddb.labs.timeparser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        assertTrue(TimeParser.getInstance().parseTime("-20000-02-21").equals("time_18000 -7304949|-7304949"));
    }
}
