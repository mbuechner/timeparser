package de.ddb.labs.timeparser;

import de.ddb.labs.timeparser.data.Outputter;
import de.ddb.labs.timeparser.data.Token;
import de.ddb.labs.timeparser.data.TokenWithValue;
import de.ddb.labs.timeparser.data.Replacement;
import de.ddb.labs.timeparser.data.PatternParser;
import de.ddb.labs.timeparser.data.InputParser;
import de.ddb.labs.timeparser.facet.Facet;
import de.ddb.labs.timeparser.facet.FacetReader;
import de.ddb.labs.timeparser.rule.Rule;
import de.ddb.labs.timeparser.rule.RuleReader;
import de.ddb.labs.timeparser.timespan.TimeSpan;
import de.ddb.labs.timeparser.timespan.TimeSpanParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Converts a textual representation of a point or period in time into:
 * </p>
 * <ul>
 * <li>a value, that can be used to sort documents by (starting) date, and</li>
 * <li>a list of time periods in which it lies.</li>
 * </ul>
 * <p>
 * The process consists of four steps:
 * </p>
 * <ol>
 * <li>Rule selection</li>
 * <li>Input normalization</li>
 * <li>Time span calculation</li>
 * <li>Final output string generation</li>
 * </ol>
 */
@Slf4j
public class TimeParser {

    private static final TimeZone timezone = TimeZone.getTimeZone("UTC");
    private static final int MAX_ERROR_COUNT = 100;

    private final static List<Character> AUXILIAR_CHARS = stringToCharList(",=?/()-–.[]0acuorcfAMJhDVIZX"); // Achtung!! different kind of slashes - vs –
    private final static List<Character> DYNAMIC_CHARS = stringToCharList("#");
    private final static List<String> PERIOD_RULES = Arrays.asList("Ottonisch", "Römisch", "Karolingisch", "Klassizistisch");

    private static final List<Replacement> MONTH_REPLACEMENTS = new ArrayList<>();

    static {
        MONTH_REPLACEMENTS.add(new Replacement("Januar", "01"));
        MONTH_REPLACEMENTS.add(new Replacement("Februar", "02"));
        MONTH_REPLACEMENTS.add(new Replacement("März", "03"));
        MONTH_REPLACEMENTS.add(new Replacement("April", "04"));
        MONTH_REPLACEMENTS.add(new Replacement("Mai", "05"));
        MONTH_REPLACEMENTS.add(new Replacement("Juni", "06"));
        MONTH_REPLACEMENTS.add(new Replacement("Juli", "07"));
        MONTH_REPLACEMENTS.add(new Replacement("August", "08"));
        MONTH_REPLACEMENTS.add(new Replacement("September", "09"));
        MONTH_REPLACEMENTS.add(new Replacement("Oktober", "10"));
        MONTH_REPLACEMENTS.add(new Replacement("November", "11"));
        MONTH_REPLACEMENTS.add(new Replacement("Dezember", "12"));
        MONTH_REPLACEMENTS.add(new Replacement("Jan.", "01"));
        MONTH_REPLACEMENTS.add(new Replacement("Feb.", "02"));
        MONTH_REPLACEMENTS.add(new Replacement("März", "03"));
        MONTH_REPLACEMENTS.add(new Replacement("Apr.", "04"));
        MONTH_REPLACEMENTS.add(new Replacement("Jun.", "06"));
        MONTH_REPLACEMENTS.add(new Replacement("Jul.", "07"));
        MONTH_REPLACEMENTS.add(new Replacement("Aug.", "08"));
        MONTH_REPLACEMENTS.add(new Replacement("Sept.", "09"));
        MONTH_REPLACEMENTS.add(new Replacement("Okt.", "10"));
        MONTH_REPLACEMENTS.add(new Replacement("Nov.", "11"));
        MONTH_REPLACEMENTS.add(new Replacement("Dez.", "12"));
        MONTH_REPLACEMENTS.add(new Replacement("Nov", "11"));
    }

    private static final List<Replacement> WEEKDAY_REPLACEMENTS = new ArrayList<>();

    static {
        WEEKDAY_REPLACEMENTS.add(new Replacement("Montag", "GG"));
        WEEKDAY_REPLACEMENTS.add(new Replacement("Dienstag", "GG"));
        WEEKDAY_REPLACEMENTS.add(new Replacement("Mittwoch", "GG"));
        WEEKDAY_REPLACEMENTS.add(new Replacement("Donnerstag", "GG"));
        WEEKDAY_REPLACEMENTS.add(new Replacement("Freitag", "GG"));
        WEEKDAY_REPLACEMENTS.add(new Replacement("Samstag", "GG"));
        WEEKDAY_REPLACEMENTS.add(new Replacement("Sonntag", "GG"));
    }

    private static final PatternParser patternParser = new PatternParser();
    private static final TimeSpanParser timeSpanParser = new TimeSpanParser();

    private static List<Rule> rules;
    private static List<Facet> facets;

    private static int errorCounter;

    private static class Holder {

        private static final TimeParser INSTANCE = new TimeParser();
    }

    /**
     * Private Constructor
     */
    private TimeParser() {
        try {
            final File rulesFile = new File("conf/timeparser/rules.csv");
            final File facetsFile = new File("conf/timeparser/facets.csv");

            rules = new RuleReader().read(rulesFile.toString(), "UTF-8");
            facets = new FacetReader().read(facetsFile.toString(), "UTF-8");

            errorCounter = 0;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static TimeParser getInstance() {
        return Holder.INSTANCE;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public String parseTime(String input) {
        if (input.isEmpty()) {
            return "";
        }
        try {
            return doParseTime(input);
        } catch (Exception e) {
            if (errorCounter < MAX_ERROR_COUNT) {
                log.warn("TimeParser: {}", e.getMessage());
                errorCounter++;
            }
        }
        return "";
    }

    private String doParseTime(String input) throws Exception {
        final List<Rule> matchingRules = findRulesForInput(input);
        if (matchingRules.size() > 1) {
            if (errorCounter < MAX_ERROR_COUNT) {
                final String msg = "Multiple rules found for input string \"" + input + "\"";
                log.warn("TimeParser: {}", msg);
                errorCounter++;
            }
            return "";
        }

        // Transform the input string into a normalized date string.
        String transformedInput = input;
        if (matchingRules.size() == 1) {
            Rule rule = matchingRules.get(0);
            transformedInput = transform(input, rule);
        }

        // Calculate a time span for the output string.
        final TimeSpan timeSpan = timeSpanParser.parse(transformedInput);
        // LOG.info("Timespan: {}", timeSpan);

        // Create the final output string.
        final String finalOutput = createFacetString(timeSpan) + " " + createDaysFromZeroString(timeSpan);

        return finalOutput;
    }

    /**
     * Does the first step of the transformation, i.e. the transformation into a
     * normalized string according to pattern rules.
     */
    private String transform(String input, Rule rule) throws Exception {
        // Compile the input mask and input pattern into an (also) input pattern.
        final List<Token> inputPattern = patternParser.parse(rule.getInputMask(), rule.getInputPattern());

        // Parse the input string.
        final InputParser inputParser = new InputParser(inputPattern, TimeParser.MONTH_REPLACEMENTS, TimeParser.WEEKDAY_REPLACEMENTS);
        final List<TokenWithValue> parsedInputTokens = inputParser.parseInputString(input);

        // Compile the output mask and output pattern into an (also) output pattern.
        final List<Token> outputPattern = patternParser.parse(true, rule.getOutputMask(), rule.getOutputPattern());

        // Create the output string.
        final Outputter outputter = new Outputter(outputPattern);
        return outputter.createOutputString(parsedInputTokens);
    }

    private List<Rule> findRulesForInput(String input) {
        for (Replacement replacement : TimeParser.MONTH_REPLACEMENTS) {
            input = input.replace(replacement.from, "MM");
        }

        for (Replacement replacement : TimeParser.WEEKDAY_REPLACEMENTS) {
            input = input.replace(replacement.from, "GG");
        }

        List<Rule> foundRules = getRules(input);
        foundRules = cleanupRules(foundRules);

        return foundRules;
    }

    private List<Rule> getRules(String input) {
        final List<Rule> foundRules = new ArrayList<>();
        for (Rule rule : rules) {
            final String ruleInputMask = rule.getInputMask();

            if (!isInputPassingBasicChecks(input, ruleInputMask)) {
                continue;
            }

            boolean correct = true;
            for (int i = 0; i < ruleInputMask.length(); i++) {
                char maskChar = ruleInputMask.charAt(i);
                char inputChar = input.charAt(i);
                if (!isMatching(maskChar, inputChar)) {
                    correct = false;
                    break;
                }
            }

            if (correct) {
                foundRules.add(rule);
            }
        }

        return foundRules;
    }

    private boolean isMatching(char maskChar, char inputChar) {
        return !((maskChar == '#' && !Character.isDigit(inputChar))
                || (Character.isSpaceChar(maskChar) && !Character.isSpaceChar(inputChar)) // Spaces should match
                || (!Character.isSpaceChar(maskChar) && Character.isSpaceChar(inputChar)) // Spaces should match
                || (AUXILIAR_CHARS.contains(maskChar) && maskChar != inputChar) // auxiliar chars are expected to be the same
                || (DYNAMIC_CHARS.contains(maskChar) && maskChar == inputChar)); // dynamic chars are expected to be a value
    }

    private boolean isInputPassingBasicChecks(String input, String ruleInputMask) {
        // Lengths should be the same with and without blank spaces
        if (ruleInputMask.length() != input.length()
                || ruleInputMask.replaceAll("\\s", "").length()
                != input.replaceAll("\\s", "").length()) {
            return false;
        }
        // If the rule is one of the period literals, we expect the input to be exactly the same
        return !(PERIOD_RULES.contains(ruleInputMask) && !input.equals(ruleInputMask));
    }

    private List<Rule> cleanupRules(List<Rule> foundRules) {
        // Keep rules with the least hashes.
        if (foundRules.size() > 1) {
            int smallestNumberOfHashes = getSmallestNumberOfHashes(foundRules);
            final Iterator<Rule> it = foundRules.iterator();
            while (it.hasNext()) {
                Rule rule = it.next();
                int n = countCharacterOccurrences(rule.getInputMask(), '#');
                if (n > smallestNumberOfHashes) {
                    it.remove();
                }
            }
        }
        // Remove duplicate rules.
        return new ArrayList<>(new LinkedHashSet<>(foundRules));
    }

    private int getSmallestNumberOfHashes(List<Rule> foundRules) {
        int smallestNumberOfHashes = -1;
        for (Rule rule : foundRules) {
            if (smallestNumberOfHashes == -1) {
                smallestNumberOfHashes = countCharacterOccurrences(rule.getInputMask(), '#');
            } else {
                int n = countCharacterOccurrences(rule.getInputMask(), '#');
                if (n < smallestNumberOfHashes) {
                    smallestNumberOfHashes = n;
                }
            }
        }
        return smallestNumberOfHashes;
    }

    private int countCharacterOccurrences(String string, char c) {
        int n = 0;
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == c) {
                n++;
            }
        }
        return n;
    }

    private String createFacetString(TimeSpan timeSpan) {
        StringBuilder facetString = new StringBuilder("");
        List<String> facetTokens = new ArrayList<>();
        int startYear = timeSpan.getStart().get(Calendar.YEAR);
        int endYear = timeSpan.getEnd().get(Calendar.YEAR);
        if (timeSpan.getStart().get(Calendar.ERA) == GregorianCalendar.BC) {
            startYear *= -1;
        }
        if (timeSpan.getEnd().get(Calendar.ERA) == GregorianCalendar.BC) {
            endYear *= -1;
        }
        for (Facet facet : facets) {
            if ((startYear <= facet.getLatestDate()) && (endYear >= facet.getEarliestDate())
                    && !facetTokens.contains(facet.getNotation())) {
                facetTokens.add(facet.getNotation());
            }
        }
        if (facetTokens.isEmpty()) {
            return "";
        } else {
            for (String facetToken : facetTokens) {
                if (facetString.length() > 0) {
                    facetString.append("|");
                }
                facetString.append(facetToken);
            }
        }
        return facetString.toString();
    }

    private String createDaysFromZeroString(TimeSpan timeSpan) {
        final long startDays = getCalendarAsIndexDays(timeSpan.getStart());
        // LOG.info("Timespan (Start): {} - {}", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(timeSpan.getStart()), startDays);
        final long endDays = getCalendarAsIndexDays(timeSpan.getEnd());
        // LOG.info("Timespan (End): {} - {}", DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(timeSpan.getEnd()), endDays);
        return startDays + "|" + endDays;
    }

    private long getCalendarAsIndexDays(Calendar calendar) {
        final Calendar temp = new GregorianCalendar(timezone);
        calendar.get(Calendar.ERA);
        temp.clear();
        temp.setLenient(false);

        // ERA unbedingt übernehmen!
        temp.set(Calendar.ERA, calendar.get(Calendar.ERA));
        temp.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        temp.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        temp.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));

        long days = temp.getTimeInMillis() / 86400000L; // 1000*60*60*24 (approximate number of milliseconds per day)
        days += 719164; // approximate number of days from 0001-01-01 to 1970-01-01 (this is where getTimeInMillis

//        LOG.info("Temp. Date: {} (ISO), {} (ms), {} (days), {} (timestamp)", 
//                DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(temp),
//                temp.getTimeInMillis(),
//                (days-719164),
//                days
//        );
        // starts)
        return days;
    }

    private static List<Character> stringToCharList(String input) {
        final List<Character> result = new ArrayList<>();

        for (char c : input.toCharArray()) {
            result.add(c);
        }
        return result;
    }
}
