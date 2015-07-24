package com.googlecode.i18n.format;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MessageFormatParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final MessageFormatParser messageFormatParser = new MessageFormatParser();

    @Test
    public void checkFormatType() {
        //when
        final FormatType formatType = messageFormatParser.getFormatType();

        //then
        assertThat(formatType, is(FormatType.MESSAGE));
    }

    @Test
    public void testNumber() {
        //given
        final String pattern = "some numb {0,number,#} {0,number,short}";

        //when
        final String[] result = messageFormatParser.parse(pattern);

        //then
        final String[] testString = {"0,number", "0,number"};
        assertThat(result, is(testString));
    }

    @Test
    public void testDate() {
        //given
        final String pattern = "some date {0,date,full} {1,date,short}";

        //when
        final String[] result = messageFormatParser.parse(pattern);

        //then
        final String[] testString = {"0,date", "1,date"};
        assertThat(result, is(testString));
    }
    
    @Test
    public void testTime() {
        //given
        final String pattern = "some time {0,time,full} {1,time,short}";

        //when
        final String[] result = messageFormatParser.parse(pattern);

        //then
        final String[] testString = {"0,time", "1,time"};
        assertThat(result, is(testString));
    }
    
    @Test
    public void testChoice() {
        //given
        final String pattern = "There {0,choice,0#are no files|1#is " +
                "one file|1<are {0,number,integer} files}.";

        //when
        final String[] result = messageFormatParser.parse(pattern);

        //then
        final String[] testString = {"0,choice"};
        assertThat(result, is(testString));
    }
    
    @Test
    public void exceptionUnknownFormat() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Format type = 'wrong'");
        messageFormatParser.parse("some {0,wrong,full}");

        thrown.expect(IllegalArgumentException.class);
        messageFormatParser.parse("some {h,time,full}");

        thrown.expect(IllegalArgumentException.class);
        messageFormatParser.parse("some {-12,time,full}");
    }
}
