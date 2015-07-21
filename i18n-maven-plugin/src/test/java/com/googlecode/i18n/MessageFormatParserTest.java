
package com.googlecode.i18n;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.googlecode.i18n.MessageFormatParser;


public class MessageFormatParserTest {
    
    
    @Test
    public void testNumber() {
        String[] testString = {"0,number", "0,number"};
        assertArrayEquals(testString, 
                MessageFormatParser.parse("some numb {0,number,#} {0,number,short}"));
    }

    @Test
    public void testDate() {
        String[] testString = {"0,date", "1,date"};
        assertArrayEquals(testString, 
                MessageFormatParser.parse("some date {0,date,full} {1,date,short}"));
    }
    
    @Test
    public void testTime() {
        String[] testString = {"0,time", "1,time"};
        assertArrayEquals(testString, 
                MessageFormatParser.parse("some time {0,time,full} {1,time,short}"));

    }
    
    @Test
    public void testChoise() {
        String[] testString = {"0,choice"};
        assertArrayEquals(testString, 
                MessageFormatParser.parse("There {0,choice,0#are no files|1#is " +
        		"one file|1<are {0,number,integer} files}."));       
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void exceptionUnknownFormat() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Format type = 'wrong'");
        MessageFormatParser.parse("some {0,wrong,full}");
        thrown.expect(IllegalArgumentException.class);
        MessageFormatParser.parse("some {h,time,full}");
        thrown.expect(IllegalArgumentException.class);
        MessageFormatParser.parse("some {-12,time,full}");
    }

}
