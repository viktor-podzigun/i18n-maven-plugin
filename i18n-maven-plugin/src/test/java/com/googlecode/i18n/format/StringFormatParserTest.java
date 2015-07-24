package com.googlecode.i18n.format;

import java.util.FormatFlagsConversionMismatchException;
import java.util.IllegalFormatPrecisionException;
import java.util.MissingFormatWidthException;
import java.util.UnknownFormatConversionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class StringFormatParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final StringFormatParser stringFormatParser = new StringFormatParser();

    @Test
    public void checkFormatType() {
        //when
        final FormatType formatType = stringFormatParser.getFormatType();

        //then
        assertThat(formatType, is(FormatType.STRING));
    }

    @Test
    public void testFixedString() {
        //given
        final String format = "Fixed string, %%, %n";

        //when
        final String[] result = stringFormatParser.parse(format);

        //then
        final String[] testString = {};
        assertThat(result, is(testString));
    }
    
    @Test
    public void testExplicitIndexing() {
        //given
        final String format = "%4$s %3$s %2$s %1$s";

        //when
        final String[] result = stringFormatParser.parse(format);

        //then
        final String[] testString = {"4$s", "3$s", "2$s", "1$s"};
        assertThat(result, is(testString));
    }

    @Test
    public void testOrdinaryIndexing() {
        //given
        final String format = "String %s, integer %d, float %f, " +
                "char %c, boolean %b, %o, %X";

        //when
        final String[] result = stringFormatParser.parse(format);

        //then
        final String[] testString = {"s", "d", "f", "c", "b", "o", "X"};
        assertThat(result, is(testString));
    }
    
    @Test
    public void testRelativeIndexing() {
        //given
        final String format = "%2$s %s %<s %<s";

        //when
        final String[] result = stringFormatParser.parse(format);

        //then
        final String[] testString = {"2$s", "s", "<s", "<s"};
        assertThat(result, is(testString));
    }

    @Test
    public void testDate() {
        //given
        final String format = "Duke's Birthday: %1$tm %1$te,%1$tY,%1$tz";

        //when
        final String[] result = stringFormatParser.parse(format);

        //then
        final String[] testString = {"1$tm", "1$te", "1$tY", "1$tz"};
        assertThat(result, is(testString));
    }
    
    @Test
    public void exceptionUnknownFormat() {
        thrown.expect(UnknownFormatConversionException.class);
        thrown.expectMessage("k");
        stringFormatParser.parse("unknown format %k");

        thrown.expect(UnknownFormatConversionException.class);
        stringFormatParser.parse("unknown format %");
    }
    
    @Test
    public void exceptionIllegalFormat() {
        thrown.expect(IllegalFormatPrecisionException.class);
        stringFormatParser.parse("illegal format %.2d");
    }
    
    @Test
    public void exceptionFormatFlags() {
        thrown.expect(FormatFlagsConversionMismatchException.class);
        stringFormatParser.parse("format flags %#b");
    }
    
    @Test
    public void exceptionMissingWidth() {
        thrown.expect(MissingFormatWidthException.class);
        stringFormatParser.parse("%-o");
    }
}
