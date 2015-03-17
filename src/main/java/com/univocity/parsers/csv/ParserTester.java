package com.univocity.parsers.csv;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

/**
 * Created by lin on 3/10/15.
 */
public class ParserTester {

    public static void main(String[] args) throws FileNotFoundException {
        CsvParserSettings settings = new CsvParserSettings();
        settings.setNullValue("");
        settings.setEmptyValue("");
        settings.getFormat().setDelimiter(',');
        settings.getFormat().setQuote('"');
        //settings.setMaxCharsPerColumn(409800);
        CsvParser reader = new CsvParser(settings);
        reader.beginParsing(new FileReader("parse"));
        String[] line = reader.parseNext();
        while (line != null) {
            if (line.length > 0 && line[0].length()> 15)
                System.out.println("ERROR:" + line[0]);
            System.out.println(Arrays.toString(line));
            line = reader.parseNext();
        }


    }
}
