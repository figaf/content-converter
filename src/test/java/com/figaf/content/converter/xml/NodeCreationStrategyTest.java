package com.figaf.content.converter.xml;

import com.figaf.content.converter.ConversionConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the line splitting used both for filling the fields and for extracting
 * the key field value: the same line must be read the same way in both places.
 * Covers the literal handling of the field separator and the enclosure sign handling.
 */
class NodeCreationStrategyTest {

    // ---------- splitLineIntoFieldValues ----------

    @Test
    void splitLineIntoFieldValues_withoutEnclosureSigns_splitsBySeparator() {
        assertArrayEquals(
            new String[]{"a", "b", "c"},
            NodeCreationStrategy.splitLineIntoFieldValues("a,b,c", sectionParameters(",", null, null))
        );
    }

    // IRT-5890: the separator is literal text, never a regular expression. Every regex metacharacter
    // must split the same way as ";" — with the old String.split(fieldSeparator) a lone "|" cut the line
    // after every character and "*" or "+" threw a PatternSyntaxException. Multi-character separators
    // are included because they were not just wrong but a regex error before the fix.
    @ParameterizedTest
    @ValueSource(strings = {"|", ".", "*", "+", "?", "(", ")", "[", "]", "{", "}", "^", "$", "\\", "||", "|*", ".*"})
    void splitLineIntoFieldValues_regexSpecialSeparator_isTreatedAsLiteralText(String separator) {
        String line = "a" + separator + "b" + separator + "c";
        assertArrayEquals(
            new String[]{"a", "b", "c"},
            NodeCreationStrategy.splitLineIntoFieldValues(line, sectionParameters(separator, null, null))
        );
    }

    @Test
    void splitLineIntoFieldValues_regexSpecialSeparatorInsideEnclosure_isKeptLiterally() {
        // the enclosure path joins the parts back with the separator: it must append the plain separator too
        assertArrayEquals(
            new String[]{"a", "b|c", "d"},
            NodeCreationStrategy.splitLineIntoFieldValues("a|\"b|c\"|d", sectionParameters("|", null, null))
        );
    }

    @Test
    void splitLineIntoFieldValues_withDefaultDoubleQuote_removesQuotesAndKeepsSeparatorInsideValue() {
        // the csv-with-quotes fixture line: quotes are removed, "Graversen,Daniel" stays one value
        assertArrayEquals(
            new String[]{"H", "", "DANIEL", "Graversen,Daniel"},
            NodeCreationStrategy.splitLineIntoFieldValues(
                "\"H\",\"\",\"DANIEL\",\"Graversen,Daniel\"",
                sectionParameters(",", null, null)
            )
        );
    }

    @Test
    void splitLineIntoFieldValues_separatorInsideQuotes_doesNotShiftFollowingFields() {
        // index 2 must still hold "42" although the Name value contains the separator
        assertArrayEquals(
            new String[]{"H", "Smith, John", "42"},
            NodeCreationStrategy.splitLineIntoFieldValues(
                "\"H\",\"Smith, John\",\"42\"",
                sectionParameters(",", null, null)
            )
        );
    }

    @Test
    void splitLineIntoFieldValues_withCustomEnclosureSign_removesItInsteadOfDoubleQuote() {
        assertArrayEquals(
            new String[]{"H", "Smith, John"},
            NodeCreationStrategy.splitLineIntoFieldValues(
                "'H','Smith, John'",
                sectionParameters(",", "'", null)
            )
        );
    }

    @Test
    void splitLineIntoFieldValues_withCustomEnclosureSign_keepsDoubleQuotesAsOrdinaryCharacters() {
        assertArrayEquals(
            new String[]{"H", "\"Smith\""},
            NodeCreationStrategy.splitLineIntoFieldValues(
                "'H',\"Smith\"",
                sectionParameters(",", "'", null)
            )
        );
    }

    @Test
    void splitLineIntoFieldValues_withDifferentBeginAndEndSigns_handlesEnclosedValues() {
        assertArrayEquals(
            new String[]{"H", "Smith, John"},
            NodeCreationStrategy.splitLineIntoFieldValues(
                "<H>,<Smith, John>",
                sectionParameters(",", "<", ">")
            )
        );
    }

    @Test
    void splitLineIntoFieldValues_mixOfEnclosedAndPlainValues() {
        assertArrayEquals(
            new String[]{"H", "plain", "a,b"},
            NodeCreationStrategy.splitLineIntoFieldValues(
                "\"H\",plain,\"a,b\"",
                sectionParameters(",", null, null)
            )
        );
    }

    @Test
    void splitLineIntoFieldValues_keepTrailingEmptyFields_keepsThem() {
        assertArrayEquals(
            new String[]{"a", "b", "", ""},
            NodeCreationStrategy.splitLineIntoFieldValues("a;b;;", sectionParameters(";", null, null), true)
        );
    }

    @Test
    void splitLineIntoFieldValues_byDefault_dropsTrailingEmptyFields() {
        assertArrayEquals(
            new String[]{"a", "b"},
            NodeCreationStrategy.splitLineIntoFieldValues("a;b;;", sectionParameters(";", null, null))
        );
    }

    @Test
    void splitLineIntoFieldValues_unterminatedEnclosure_runsToEndOfLine() {
        // one unbalanced sign must not silently drop every field after it
        assertArrayEquals(
            new String[]{"a", "b,c"},
            NodeCreationStrategy.splitLineIntoFieldValues("a,\"b,c", sectionParameters(",", null, null))
        );
    }

    @Test
    void splitLineIntoFieldValues_unterminatedEnclosureAtLineStart_yieldsWholeLine() {
        assertArrayEquals(
            new String[]{"a,b"},
            NodeCreationStrategy.splitLineIntoFieldValues("\"a,b", sectionParameters(",", null, null))
        );
    }

    // ---------- removeEnclosureSigns ----------

    @Test
    void removeEnclosureSigns_withDefaultDoubleQuote() {
        // a real PI channel may store keyFieldValue as "H" with the quotes
        assertEquals("H", NodeCreationStrategy.removeEnclosureSigns("\"H\"", sectionParameters(",", null, null)));
    }

    @Test
    void removeEnclosureSigns_valueWithoutSignsStaysUnaltered() {
        assertEquals("H", NodeCreationStrategy.removeEnclosureSigns("H", sectionParameters(",", null, null)));
    }

    @Test
    void removeEnclosureSigns_withCustomSign() {
        assertEquals("H", NodeCreationStrategy.removeEnclosureSigns("'H'", sectionParameters(",", "'", null)));
    }

    @Test
    void removeEnclosureSigns_withDifferentBeginAndEndSigns() {
        assertEquals("H", NodeCreationStrategy.removeEnclosureSigns("<H>", sectionParameters(",", "<", ">")));
    }

    @Test
    void removeEnclosureSigns_removesAllOccurrences() {
        // same semantics as the field values: every occurrence of the sign is removed, not only the outer pair
        assertEquals("AB", NodeCreationStrategy.removeEnclosureSigns("A\"B", sectionParameters(",", null, null)));
    }

    private static ConversionConfig.SectionParameters sectionParameters(String fieldSeparator, String enclosureSign, String enclosureSignEnd) {
        ConversionConfig.SectionParameters sectionParameters = new ConversionConfig.SectionParameters();
        sectionParameters.setFieldSeparator(fieldSeparator);
        sectionParameters.setEnclosureSign(enclosureSign);
        sectionParameters.setEnclosureSignEnd(enclosureSignEnd);
        return sectionParameters;
    }
}
