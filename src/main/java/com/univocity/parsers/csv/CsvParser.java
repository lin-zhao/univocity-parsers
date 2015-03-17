/*******************************************************************************
 * Copyright 2014 uniVocity Software Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.univocity.parsers.csv;

import com.univocity.parsers.common.*;
import com.univocity.parsers.common.input.*;

/**
 * A very fast CSV parser implementation.
 *
 * @see CsvFormat
 * @see CsvParserSettings
 * @see CsvWriter
 * @see AbstractParser
 *
 * @author uniVocity Software Pty Ltd - <a href="mailto:parsers@univocity.com">parsers@univocity.com</a>
 *
 */
public class CsvParser extends AbstractParser<CsvParserSettings> {

	private final boolean ignoreTrailingWhitespace;
	private final boolean ignoreLeadingWhitespace;
	private final boolean parseUnescapedQuotes;
    private boolean ignoreQuoteEscape;

	private final char delimiter;
	private final char quote;
	private final char quoteEscape;
	private final char newLine;
	private final DefaultCharAppender whitespaceAppender;

	/**
	 * The CsvParser supports all settings provided by {@link CsvParserSettings}, and requires this configuration to be properly initialized.
	 * @param settings the parser configuration
	 */
	public CsvParser(CsvParserSettings settings, boolean ignoreQuoteEscape) {
		super(settings);
		ignoreTrailingWhitespace = settings.getIgnoreTrailingWhitespaces();
		ignoreLeadingWhitespace = settings.getIgnoreLeadingWhitespaces();
		parseUnescapedQuotes = settings.isParseUnescapedQuotes();
        this.ignoreQuoteEscape = ignoreQuoteEscape;

		CsvFormat format = settings.getFormat();
		delimiter = format.getDelimiter();
		quote = format.getQuote();
		quoteEscape = format.getQuoteEscape();
		newLine = format.getNormalizedNewline();

		whitespaceAppender = new DefaultCharAppender(settings.getMaxCharsPerColumn(), "");
	}

    public CsvParser(CsvParserSettings settings) {
        this(settings, false);
    }
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void parseRecord() {
		if (ch <= ' ' && ignoreLeadingWhitespace) {
			skipWhitespace();
		}

		while (ch != newLine) {
			parseField();
			if (ch != newLine) {
				ch = input.nextChar();
				if (ch == newLine) {
					output.emptyParsed();
				}
			}
		}
	}

	private void parseValue() {
		if (ignoreTrailingWhitespace) {
			while (ch != delimiter && ch != newLine) {
				output.appender.appendIgnoringWhitespace(ch);
				ch = input.nextChar();
			}
		} else {
			while (ch != delimiter && ch != newLine) {
				output.appender.append(ch);
				ch = input.nextChar();
			}
		}
	}

	private void parseQuotedValue(char prev) {
		ch = input.nextChar();
        // If a quote is the only character in a row, return an empty row. This is for fixing
        if (ch == newLine && output.getCurrentColumn() == 0)
            return;

		while (!(prev == quote && (ch == delimiter || ch == newLine || ch <= ' '))) {
			if (ch != quote) {
				if (prev == quote) { //unescaped quote detected
					if (parseUnescapedQuotes) {
						output.appender.append(quote);
						output.appender.append(ch);
						parseQuotedValue(ch);
						break;
					} else {
						throw new TextParsingException(context, "Unescaped quote character '" + quote
								+ "' inside quoted value of CSV field. To allow unescaped quotes, set 'parseUnescapedQuotes' to 'true' in the CSV parser settings. Cannot parse CSV input.");
					}
				}
				output.appender.append(ch);
				prev = ch;
			} else if (prev == quoteEscape && !ignoreQuoteEscape) {
				output.appender.append(quote);
				prev = '\0';
			} else if (prev == quoteEscape) {
                // Seeing consecutive quotes. Include the last one.
                output.appender.append(quote);
                prev = ch;
            } else {
				prev = ch;
			}
			ch = input.nextChar();
		}

		// handles whitespaces after quoted value: whitespaces are ignored. Content after whitespaces may be parsed if 'parseUnescapedQuotes' is enabled.
		if (ch != newLine && ch <= ' ') {
			whitespaceAppender.reset();
			do {
				//saves whitespaces after value
				whitespaceAppender.append(ch);
				ch = input.nextChar();
				//found a new line, go to next record.
				if (ch == newLine) {
					return;
				}
			} while (ch <= ' ');

			//there's more stuff after the quoted value, not only empty spaces.
			if (!(ch == delimiter || ch == newLine) && parseUnescapedQuotes) {
				if (output.appender instanceof DefaultCharAppender) {
					//puts the quote before whitespaces back, then restores the whitespaces
					output.appender.append(quote);
					((DefaultCharAppender) output.appender).append(whitespaceAppender);
				}
				//the next character is not the escape character, put it there
				if (ch != quoteEscape) {
					output.appender.append(ch);
				}
				//sets this caracter as the previous character (may be escaping)
				//calls recursively to keep parsing potentially quoted content
				parseQuotedValue(ch);
			}
		}

		if (!(ch == delimiter || ch == newLine)) {
			throw new TextParsingException(context, "Unexpected character '" + ch + "' following quoted value of CSV field. Expecting '" + delimiter + "'. Cannot parse CSV input.");
		}
	}

	private void parseField() {
		if (ch <= ' ' && ignoreLeadingWhitespace) {
			skipWhitespace();
		}

		if (ch == delimiter) {
			output.emptyParsed();
		} else {
			if (ch == quote) {
				parseQuotedValue('\0');
			} else {
				parseValue();
			}
			output.valueParsed();
		}
	}

	private void skipWhitespace() {
		while (ch <= ' ' && ch != delimiter && ch != newLine) {
			ch = input.nextChar();
		}
	}

}
