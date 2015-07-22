package com.googlecode.i18n.format;

import java.util.FormatFlagsConversionMismatchException;
import java.util.IllegalFormatPrecisionException;
import java.util.MissingFormatWidthException;
import java.util.UnknownFormatConversionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertArrayEquals;

public class StringFormatParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFixedString() {
        String[] testString = {};
        assertArrayEquals(testString, 
                StringFormatParser.parse("Fixed string, %%, %n"));
    }
    
    @Test
    public void testExplicitIndexing() {
        String[] testString = {"4$s", "3$s", "2$s", "1$s"};
        assertArrayEquals(testString, 
                StringFormatParser.parse("%4$s %3$s %2$s %1$s"));
    }

    @Test
    public void testOrdinaryIndexing() {
        String[] testString = {"s", "d", "f", "c", "b", "o", "X"};
        assertArrayEquals(testString, 
                StringFormatParser.parse("String %s, integer %d, float %f, " +
                		"char %c, boolean %b, %o, %X"));
    }
    
    @Test
    public void testRelativeIndexing() {
        String[] testString = {"2$s", "s", "<s", "<s"};
        assertArrayEquals(testString, 
                StringFormatParser.parse("%2$s %s %<s %<s"));
    }
        
    @Test
    public void testDate() {
        String[] testString = {"1$tm", "1$te", "1$tY", "1$tz"};
        assertArrayEquals(testString, 
                StringFormatParser.parse("Duke's Birthday: %1$tm %1$te,%1$tY,%1$tz"));
    }
    
    @Test
    public void exceptionUnknownFormat() {
        thrown.expect(UnknownFormatConversionException.class);
        thrown.expectMessage("k");
        StringFormatParser.parse("unknown format %k");

        thrown.expect(UnknownFormatConversionException.class);
        StringFormatParser.parse("unknown format %");
    }
    
    @Test
    public void exceptionIllegalFormat() {
        thrown.expect(IllegalFormatPrecisionException.class);
        StringFormatParser.parse("illegal format %.2d");
    }
    
    @Test
    public void exceptionFormatFlags() {
        thrown.expect(FormatFlagsConversionMismatchException.class);
        StringFormatParser.parse("format flags %#b");
    }
    
    @Test
    public void exceptionMissingWidht() {
        thrown.expect(MissingFormatWidthException.class);
        StringFormatParser.parse("%-o");
    }
}
