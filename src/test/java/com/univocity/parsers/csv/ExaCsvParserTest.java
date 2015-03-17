package com.univocity.parsers.csv;

import com.univocity.parsers.ParserTestCase;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.RowListProcessor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * Created by lin on 3/16/15.
 */
public class ExaCsvParserTest extends ParserTestCase {

    @DataProvider(name = "csvProvider")
    public Object[][] csvProvider() {
        return new Object[][] {
                { "/csv/essential.csv", new char[] { '\n' } },
                { "/csv/essential-dos.csv", new char[] { '\r', '\n' } },
                { "/csv/essential-mac.csv", new char[] { '\r' } },
                { "/csv/essential.csv", null },
                { "/csv/essential-dos.csv", null },
                { "/csv/essential-mac.csv", null }
        };
    }

    @Test(enabled = true, dataProvider = "csvProvider")
    public void parseIgnoringWhitespaces(String csvFile, char[] lineSeparator) throws Exception {
        CsvParserSettings settings = newCsvInputSettings(lineSeparator);
        settings.setRowProcessor(processor);
        settings.setHeaderExtractionEnabled(true);
        settings.setIgnoreLeadingWhitespaces(true);
        settings.setIgnoreTrailingWhitespaces(true);

        CsvParser parser = new CsvParser(settings, true);
        parser.parse(newReader(csvFile));

        String[] expectedHeaders = new String[] { "Year", "Make", "Model", "Description", "Price" };

        String[][] expectedResult = new String[][] {
                { "1997", "Ford", "E350", "ac, abs, moon", "3000.00" },
                { "1999", "Chevy", "Venture \"\"Extended Edition\"\"", null, "4900.00" },
                { "1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00" },
                { "1999", "Chevy", "Venture \"\"Extended Edition, Very Large\"\"", null, "5000.00" },
                { null, null, "Venture \"\"Extended Edition\"\"", null, "4900.00" },
                { null, null, null, null, null },
                { null, null, null, null, null },
                { null, null, "5", null, null },
                { "1997", "Ford", "E350", "ac, abs, moon", "3000.00" },
                { "1997", "Ford", "E350", " ac, abs, moon ", "3000.00" },
                { "1997", "Ford", "E350", " ac, abs, moon ", "3000.00" },
                { "19 97", "Fo rd", "E350", " ac, abs, moon ", "3000.00" },
                { null, " ", null, "  ", "30 00.00" },
                { "1997", "Ford", "E350", " \"\" ac, abs, moon \"\" ", "3000.00" },
                { "1997", "Ford", "E350", "\"\" ac, abs, moon \"\" ", "3000.00" },
        };

        assertHeadersAndValuesMatch(expectedHeaders, expectedResult);
    }

    protected CsvParserSettings newCsvInputSettings(char[] lineSeparator) {
        CsvParserSettings out = new CsvParserSettings();
        if (lineSeparator == null) {
            out.setLineSeparatorDetectionEnabled(true);
        } else {
            out.getFormat().setLineSeparator(lineSeparator);
        }
        return out;
    }

    @Test(enabled = true, dataProvider = "csvProvider")
    public void parseUsingWhitespaces(String csvFile, char[] lineSeparator) throws Exception {
        CsvParserSettings settings = newCsvInputSettings(lineSeparator);
        settings.setRowProcessor(processor);
        settings.setHeaderExtractionEnabled(true);
        settings.setNullValue("?????");
        settings.setEmptyValue("XXXXX");
        settings.setIgnoreLeadingWhitespaces(false);
        settings.setIgnoreTrailingWhitespaces(false);

        CsvParser parser = new CsvParser(settings, true);
        parser.parse(newReader(csvFile));

        String[] expectedHeaders = new String[] { "Year", "Make", "Model", "Description", "Price" };

        String[][] expectedResult = new String[][] {
                { "1997", "Ford", "E350", "ac, abs, moon", "3000.00" },
                { "1999", "Chevy", "Venture \"\"Extended Edition\"\"", "XXXXX", "4900.00" },
                { "1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00" },
                { "1999", "Chevy", "Venture \"\"Extended Edition, Very Large\"\"", "?????", "5000.00" },
                { "?????", "?????", "Venture \"\"Extended Edition\"\"", "XXXXX", "4900.00" },
                { "?????", "?????", "?????", "?????", "?????" },
                { " ", " ", " ", " ", " " },
                { "?????", "?????", " 5 ", "?????", "?????" },
                { "  " },
                { "1997 ", " Ford ", "E350", "ac, abs, moon", " \"3000.00\" \t" },
                { "1997", " Ford ", "E350", " ac, abs, moon ", "3000.00  \t" },
                { "  1997", " Ford ", "E350", " ac, abs, moon ", "3000.00" },
                { "    19 97 ", " Fo rd ", "E350", " ac, abs, moon ", "3000.00" },
                { "\t\t", " ", "  ", " \"  \"\t", "30 00.00\t" },
                { "1997", "Ford", "E350", " \"\" ac, abs, moon \"\" ", "3000.00" },
                { "1997", "Ford", "E350", "\"\" ac, abs, moon \"\" ", "3000.00" },
        };

        assertHeadersAndValuesMatch(expectedHeaders, expectedResult);
    }

    @Test(enabled = true, dataProvider = "csvProvider")
    public void parseColumns(String csvFile, char[] lineSeparator) throws Exception {
        CsvParserSettings settings = newCsvInputSettings(lineSeparator);
        settings.setRowProcessor(processor);
        settings.setHeaderExtractionEnabled(true);
        settings.setIgnoreLeadingWhitespaces(true);
        settings.setIgnoreTrailingWhitespaces(true);
        settings.selectFields("Year");
        settings.setColumnReorderingEnabled(false);

        CsvParser parser = new CsvParser(settings, true);
        parser.parse(newReader(csvFile));

        String[] expectedHeaders = new String[] { "Year", "Make", "Model", "Description", "Price" };

        String[][] expectedResult = new String[][] {
                { "1997", null, null, null, null },
                { "1999", null, null, null, null },
                { "1996", null, null, null, null },
                { "1999", null, null, null, null },
                { null, null, null, null, null },
                { null, null, null, null, null },
                { null, null, null, null, null },
                { null, null, null, null, null },
                { "1997", null, null, null, null },
                { "1997", null, null, null, null },
                { "1997", null, null, null, null },
                { "19 97", null, null, null, null },
                { null, null, null, null, null },
                { "1997", null, null, null, null },
                { "1997", null, null, null, null },
        };

        assertHeadersAndValuesMatch(expectedHeaders, expectedResult);
    }

    private String[] process(String input, Integer[] indexesToExclude, Integer[] indexesToSelect, String[] fieldsToExclude, String[] fieldsToSelect) {
        RowListProcessor processor = new RowListProcessor();
        StringReader reader = new StringReader(input);
        CsvParserSettings settings = new CsvParserSettings();
        settings.setRowProcessor(processor);
        settings.setHeaderExtractionEnabled(fieldsToExclude != null || fieldsToSelect != null);

        if (indexesToExclude != null) {
            settings.excludeIndexes(indexesToExclude);
        } else if (fieldsToExclude != null) {
            settings.excludeFields(fieldsToExclude);
        } else if (indexesToSelect != null) {
            settings.selectIndexes(indexesToSelect);
        } else if (fieldsToSelect != null) {
            settings.selectFields(fieldsToSelect);
        }

        CsvParser parser = new CsvParser(settings, true);
        parser.parse(reader);

        List<String[]> rows = processor.getRows();
        assertEquals(rows.size(), 1);
        return rows.get(0);
    }

    @Test
    public void columnSelectionTest() {
        String[] result;
        String input = "a,b,c,d,e";

        Integer[] indexesToExclude = new Integer[] { 0, 4 };
        result = process(input, indexesToExclude, null, null, null);
        assertEquals(result, new String[] { "b", "c", "d" });

        Integer[] indexesToSelect = new Integer[] { 0, 4 };
        result = process(input, null, indexesToSelect, null, null);
        assertEquals(result, new String[] { "a", "e" });

        input = "ha,hb,hc,hd,he\na,b,c,d,e";

        String[] fieldsToExclude = new String[] { "hb", "hd" };
        result = process(input, null, null, fieldsToExclude, null);
        assertEquals(result, new String[] { "a", "c", "e" });

        String[] fieldsToSelect = new String[] { "hb", "hd" };
        result = process(input, null, null, null, fieldsToSelect);
        assertEquals(result, new String[] { "b", "d" });
    }

    @Override
    protected RowListProcessor newRowListProcessor() {
        return new RowListProcessor() {
            @Override
            public void processStarted(ParsingContext context) {
                super.processStarted(context);
                context.skipLines(2);
            }

            @Override
            public void rowProcessed(String[] row, ParsingContext context) {
                super.rowProcessed(row, context);

                // for (int i = 0; i < row.length; i++) {
                // row[i] = ">>" + row[i] + "<<";
                // }
                // System.out.println(context.currentLine() + " => " + Arrays.toString(row));

                if (context.currentLine() == 9) {
                    context.skipLines(1);
                }
            }
        };
    }

    @Test(enabled = true, dataProvider = "csvProvider")
    public void parseOneByOne(String csvFile, char[] lineSeparator) throws Exception {
        CsvParserSettings settings = newCsvInputSettings(lineSeparator);
        settings.setRowProcessor(processor);
        settings.setHeaderExtractionEnabled(true);
        settings.setIgnoreLeadingWhitespaces(true);
        settings.setIgnoreTrailingWhitespaces(true);
        settings.setHeaders("YR", "MK", "MDL", "DSC", "PRC");

        List<Object[]> results = new ArrayList<Object[]>();
        CsvParser parser = new CsvParser(settings, true);
        try {
            parser.beginParsing(newReader(csvFile));

            Object[] row = null;
            while ((row = parser.parseNext()) != null) {
                if (row.length == 5) {
                    results.add(row);
                }
            }
        } finally {
            parser.stopParsing();
        }

        String[] expectedHeaders = new String[] { "YR", "MK", "MDL", "DSC", "PRC" };

        String[][] expectedResult = new String[][] {
                { "1997", "Ford", "E350", "ac, abs, moon", "3000.00" },
                { "1999", "Chevy", "Venture \"\"Extended Edition\"\"", null, "4900.00" },
                { "1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00" },
                { "1999", "Chevy", "Venture \"\"Extended Edition, Very Large\"\"", null, "5000.00" },
                { null, null, "Venture \"\"Extended Edition\"\"", null, "4900.00" },
                { null, null, null, null, null },
                { null, null, null, null, null },
                { null, null, "5", null, null },
                { "1997", "Ford", "E350", "ac, abs, moon", "3000.00" },
                { "1997", "Ford", "E350", " ac, abs, moon ", "3000.00" },
                { "1997", "Ford", "E350", " ac, abs, moon ", "3000.00" },
                { "19 97", "Fo rd", "E350", " ac, abs, moon ", "3000.00" },
                { null, " ", null, "  ", "30 00.00" },
                { "1997", "Ford", "E350", " \"\" ac, abs, moon \"\" ", "3000.00" },
                { "1997", "Ford", "E350", "\"\" ac, abs, moon \"\" ", "3000.00" },
        };

        Object[] headers = processor.getHeaders();
        TestUtils.assertEquals(headers, expectedHeaders);

        assertEquals(results.size(), expectedResult.length);

        for (int i = 0; i < expectedResult.length; i++) {
            Object[] result = results.get(i);
            String[] expectedRow = expectedResult[i];
            assertEquals(result, expectedRow);
        }
    }

    @Test(enabled = true, dataProvider = "csvProvider")
    public void parse3Records(String csvFile, char[] lineSeparator) throws Exception {
        CsvParserSettings settings = newCsvInputSettings(lineSeparator);
        settings.setRowProcessor(processor);
        settings.setHeaderExtractionEnabled(true);
        settings.setIgnoreLeadingWhitespaces(true);
        settings.setIgnoreTrailingWhitespaces(true);
        settings.setNumberOfRecordsToRead(3);

        CsvParser parser = new CsvParser(settings, true);
        parser.parse(newReader(csvFile));

        String[] expectedHeaders = new String[] { "Year", "Make", "Model", "Description", "Price" };

        String[][] expectedResult = new String[][] {
                { "1997", "Ford", "E350", "ac, abs, moon", "3000.00" },
                { "1999", "Chevy", "Venture \"\"Extended Edition\"\"", null, "4900.00" },
                { "1996", "Jeep", "Grand Cherokee", "MUST SELL!\nair, moon roof, loaded", "4799.00" },
        };

        assertHeadersAndValuesMatch(expectedHeaders, expectedResult);
    }

    @Test
    public void parseBrokenQuoteEscape() {

        CsvParserSettings settings = newCsvInputSettings(new char[] { '\n' });
        settings.setHeaderExtractionEnabled(false);
        CsvParser parser = new CsvParser(settings, true);

        parser.beginParsing(new StringReader(""
                + "something,\"a quoted value \"with unescaped quotes\" can be parsed\", something\n"
                + "1997 , Ford ,E350,\"s, m\"\"\"	, \"3000.00\"\n"
                + "1997 , Ford ,E350,\"ac, abs, moon\"	, \"3000.00\" \n"
                + "something,\"a \"quoted\" \"\"value\"\" \"\"with unescaped quotes\"\" can be parsed\" , something\n"));

        String[] row = parser.parseNext();

        System.out.println(Arrays.toString(row));
        assertEquals(row[0], "something");
        assertEquals(row[2], "something");
        assertEquals(row[1], "a quoted value \"with unescaped quotes\" can be parsed");

        row = parser.parseNext();
        System.out.println(Arrays.toString(row));

        assertEquals(row, new String[] { "1997", "Ford", "E350", "s, m\"\"", "3000.00" });

        row = parser.parseNext();
        System.out.println(Arrays.toString(row));
        assertEquals(row, new String[] { "1997", "Ford", "E350", "ac, abs, moon", "3000.00" });

        row = parser.parseNext();
        System.out.println(Arrays.toString(row));
        assertEquals(row[0], "something");
        assertEquals(row[2], "something");
        assertEquals(row[1], "a \"quoted\" \"\"value\"\" \"\"with unescaped quotes\"\" can be parsed");
    }

    @Test
    public void testReadEmptyValue() {
        CsvParserSettings settings = newCsvInputSettings(new char[] { '\n' });
        settings.setEmptyValue("");
        settings.setHeaderExtractionEnabled(false);
        CsvParser parser = new CsvParser(settings, true);

        parser.beginParsing(new StringReader("a,b,,c,\"\",\r\n"));
        String[] row = parser.parseNext();

        assertEquals(row[0], "a");
        assertEquals(row[1], "b");
        assertEquals(row[2], null);
        assertEquals(row[3], "c");
        assertEquals(row[4], "");
        assertEquals(row[5], "");
    }
}
