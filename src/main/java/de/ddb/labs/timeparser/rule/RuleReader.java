package de.ddb.labs.timeparser.rule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;

/**
 * <p>
 * Reads transformation rules from a file.
 * </p>
 * <ul>
 * <li>The first line in the file is ignored and can be used as a header
 * line.</li>
 * <li>Each line must consist of six columns, separated by a tab character (\t).
 * The columns must correspond to the following, in the specified order:
 * <ol>
 * <li>Input specification mask</li>
 * <li>Input specification pattern</li>
 * <li>Example input string</li>
 * <li>Output specification mask</li>
 * <li>Output specification pattern</li>
 * <li>Required output string for the example input string</li>
 * </ol>
 * </li>
 * </ul>
 */
public class RuleReader {

    public List<Rule> read(String path, String charsetName) throws IOException, ParseException {
        ArrayList<Rule> rules = new ArrayList<>();
        ArrayList<String> inputMasks = new ArrayList<>();

        try (final InputStream in = RuleReader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Rule file does could not be found for the given path \"" + path + "\"");
            }
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in, charsetName))) {
                int i = 0;
                String line;
                boolean skippedFirstLine = false;
                while ((line = reader.readLine()) != null) {
                    if (skippedFirstLine) {
                        line = line.trim();
                        if (line.length() > 0) {
                            String[] columns = line.split("\t");
                            if (columns.length < 7) {
                                final String errorMsg = "Expected 7 columns instead of " + columns.length + " in rule file \"" + path + "\", line " + i + ": \"" + line + "\"";
                                throw new ParseException(errorMsg, i);
                            }

                            final Rule rule = new Rule(columns[0], columns[1], columns[2], columns[3], columns[4], columns[5], columns[6]);
                            final String inputMask = columns[0];

                            // We filter rules by repeated inputMask
                            if (!inputMasks.contains(inputMask)) {
                                inputMasks.add(inputMask);
                                rules.add(rule);
                            }
                        }
                    } else {
                        skippedFirstLine = true;
                    }
                    i++;
                }
            }
            return rules;
        }
    }
}
