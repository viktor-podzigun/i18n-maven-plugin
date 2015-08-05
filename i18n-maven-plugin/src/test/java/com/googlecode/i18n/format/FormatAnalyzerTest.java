package com.googlecode.i18n.format;

import java.util.HashMap;
import java.util.Properties;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;
import com.googlecode.i18n.AbstractMessageAnalyzer;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

public class FormatAnalyzerTest {

    private final AbstractFormatParser formatParser = createMock(AbstractFormatParser.class);

    @Test
    public void shouldPassBaseCheck() {
        //given
        final AbstractMessageAnalyzer messageAnalyzer = getMessageAnalyzer();
        final FormatAnalyzer analyzer = new FormatAnalyzer(messageAnalyzer, formatParser);
        final Properties props = new Properties();
        final String key = "key.1";
        final String value = "message 1";
        props.put(key, value);
        final HashMap<String, FormatType> keys = new HashMap<String, FormatType>();
        keys.put(key, FormatType.MESSAGE);
        expect(formatParser.getFormatType()).andReturn(FormatType.MESSAGE);
        expect(formatParser.parse(value)).andReturn(new String[]{""});
        replayAll();

        //when
        analyzer.check(1, props, keys);

        //then
        assertThat(props.size(), is(0));
        assertThat(keys.size(), is(0));
        assertThat(messageAnalyzer.getErrorCount(), is(0));
        assertThat(messageAnalyzer.getWarningCount(), is(0));
        verifyAll();
    }

    @Test
    public void shouldPassSecondCheck() {
        //given
        final AbstractMessageAnalyzer messageAnalyzer = getMessageAnalyzer();
        final FormatAnalyzer analyzer = new FormatAnalyzer(messageAnalyzer, formatParser);
        final Properties baseProps = new Properties();
        final String key = "key.1";
        final String value = "message 1";
        baseProps.put(key, value);
        final Properties props = new Properties();
        props.put(key, value);
        final HashMap<String, FormatType> keys = new HashMap<String, FormatType>();
        keys.put(key, FormatType.MESSAGE);
        expect(formatParser.getFormatType()).andReturn(FormatType.MESSAGE);
        expect(formatParser.parse(value)).andReturn(new String[]{"1"}).times(2);
        replayAll();

        //when
        analyzer.check(1, baseProps, keys);
        analyzer.check(1, props, keys);

        //then
        assertThat(baseProps.size(), is(0));
        assertThat(props.size(), is(0));
        assertThat(keys.size(), is(0));
        assertThat(messageAnalyzer.getErrorCount(), is(0));
        assertThat(messageAnalyzer.getWarningCount(), is(0));
        verifyAll();
    }

    @Test
    public void shouldSkipUnrecognizedFormat() {
        //given
        final AbstractMessageAnalyzer messageAnalyzer = getMessageAnalyzer();
        final FormatAnalyzer analyzer = new FormatAnalyzer(messageAnalyzer, formatParser);
        final Properties props = new Properties();
        final String key = "key.1";
        props.put(key, "message 1");
        final HashMap<String, FormatType> keys = new HashMap<String, FormatType>();
        keys.put(key, null);
        expect(formatParser.getFormatType()).andReturn(FormatType.MESSAGE);
        replayAll();

        //when
        analyzer.check(1, props, keys);

        //then
        assertThat(props.size(), is(1));
        assertThat(keys.size(), is(1));
        assertThat(messageAnalyzer.getErrorCount(), is(0));
        assertThat(messageAnalyzer.getWarningCount(), is(0));
        verifyAll();
    }

    @Test
    public void shouldReportErrorIfBaseMessageNotSpecified() {
        //given
        final AbstractMessageAnalyzer messageAnalyzer = getMessageAnalyzer();
        final FormatAnalyzer analyzer = new FormatAnalyzer(messageAnalyzer, formatParser);
        final Properties props = new Properties();
        final String key = "key.1";
        props.put(key, "");
        final HashMap<String, FormatType> keys = new HashMap<String, FormatType>();
        keys.put(key, FormatType.MESSAGE);
        expect(formatParser.getFormatType()).andReturn(FormatType.MESSAGE);
        replayAll();

        //when
        analyzer.check(1, props, keys);

        //then
        assertThat(props.size(), is(0));
        assertThat(keys.size(), is(0));
        assertThat(messageAnalyzer.getErrorCount(), is(1));
        assertThat(messageAnalyzer.getWarningCount(), is(0));
        verifyAll();
    }

    @Test
    public void shouldReportErrorIfSecondMessageNotSpecified() {
        //given
        final AbstractMessageAnalyzer messageAnalyzer = getMessageAnalyzer();
        final FormatAnalyzer analyzer = new FormatAnalyzer(messageAnalyzer, formatParser);
        final Properties baseProps = new Properties();
        final String key = "key.1";
        final String value = "message 1";
        baseProps.put(key, value);
        final Properties props = new Properties();
        props.put(key, "");
        final HashMap<String, FormatType> keys = new HashMap<String, FormatType>();
        keys.put(key, FormatType.MESSAGE);
        expect(formatParser.getFormatType()).andReturn(FormatType.MESSAGE);
        expect(formatParser.parse(value)).andReturn(new String[]{"1"});
        replayAll();

        //when
        analyzer.check(1, baseProps, keys);
        analyzer.check(1, props, keys);

        //then
        assertThat(baseProps.size(), is(0));
        assertThat(props.size(), is(0));
        assertThat(keys.size(), is(0));
        assertThat(messageAnalyzer.getErrorCount(), is(1));
        assertThat(messageAnalyzer.getWarningCount(), is(0));
        verifyAll();
    }

    @Test
    public void shouldReportErrorIfCannotParseBaseMessage() {
        //given
        final AbstractMessageAnalyzer messageAnalyzer = getMessageAnalyzer();
        final FormatAnalyzer analyzer = new FormatAnalyzer(messageAnalyzer, formatParser);
        final Properties props = new Properties();
        final String key = "key.1";
        final String value = "message 1";
        props.put(key, value);
        final HashMap<String, FormatType> keys = new HashMap<String, FormatType>();
        keys.put(key, FormatType.MESSAGE);
        expect(formatParser.getFormatType()).andReturn(FormatType.MESSAGE);
        expect(formatParser.parse(value)).andThrow(new IllegalArgumentException());
        replayAll();

        //when
        analyzer.check(1, props, keys);

        //then
        assertThat(props.size(), is(0));
        assertThat(keys.size(), is(0));
        assertThat(messageAnalyzer.getErrorCount(), is(1));
        assertThat(messageAnalyzer.getWarningCount(), is(0));
        verifyAll();
    }

    @Test
    public void shouldReportErrorIfCannotParseSecondMessage() {
        //given
        final AbstractMessageAnalyzer messageAnalyzer = getMessageAnalyzer();
        final FormatAnalyzer analyzer = new FormatAnalyzer(messageAnalyzer, formatParser);
        final Properties baseProps = new Properties();
        final String key = "key.1";
        final String value1 = "message 1";
        baseProps.put(key, value1);
        final Properties props = new Properties();
        final String value2 = "message 2";
        props.put(key, value2);
        final HashMap<String, FormatType> keys = new HashMap<String, FormatType>();
        keys.put(key, FormatType.MESSAGE);
        expect(formatParser.getFormatType()).andReturn(FormatType.MESSAGE);
        expect(formatParser.parse(value1)).andReturn(new String[]{"1"});
        expect(formatParser.parse(value2)).andThrow(new IllegalArgumentException());
        replayAll();

        //when
        analyzer.check(1, baseProps, keys);
        analyzer.check(1, props, keys);

        //then
        assertThat(baseProps.size(), is(0));
        assertThat(props.size(), is(0));
        assertThat(keys.size(), is(0));
        assertThat(messageAnalyzer.getErrorCount(), is(1));
        assertThat(messageAnalyzer.getWarningCount(), is(0));
        verifyAll();
    }

    @Test
    public void shouldReportErrorIfSecondFormatNotMatch() {
        //given
        final AbstractMessageAnalyzer messageAnalyzer = getMessageAnalyzer();
        final FormatAnalyzer analyzer = new FormatAnalyzer(messageAnalyzer, formatParser);
        final Properties baseProps = new Properties();
        final String key = "key.1";
        final String value1 = "message 1";
        baseProps.put(key, value1);
        final Properties props = new Properties();
        final String value2 = "message 2";
        props.put(key, value2);
        final HashMap<String, FormatType> keys = new HashMap<String, FormatType>();
        keys.put(key, FormatType.MESSAGE);
        expect(formatParser.getFormatType()).andReturn(FormatType.MESSAGE);
        expect(formatParser.parse(value1)).andReturn(new String[]{"1"});
        expect(formatParser.parse(value2)).andReturn(new String[]{"2"});
        replayAll();

        //when
        analyzer.check(1, baseProps, keys);
        analyzer.check(1, props, keys);

        //then
        assertThat(baseProps.size(), is(0));
        assertThat(props.size(), is(0));
        assertThat(keys.size(), is(0));
        assertThat(messageAnalyzer.getErrorCount(), is(1));
        assertThat(messageAnalyzer.getWarningCount(), is(0));
        verifyAll();
    }

    private AbstractMessageAnalyzer getMessageAnalyzer() {
        return new AbstractMessageAnalyzer(new SystemStreamLog(), "", "") {
        };
    }
}
