package com.googlecode.i18n;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PlainMessageAnalyzerTest {

    private static final String ROOT_PATH = "target/classes";

    @Test
    public void shouldPassForNotFormatted() {
        //given
        final String baseFilePath = "plain/";

        //when
        final PlainMessageAnalyzer analyzer = PlainMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH, "en,ru", "en", baseFilePath, null);

        //then
        assertThat(analyzer.getErrorCount(), is(0));
        assertThat(analyzer.getWarningCount(), is(0));
    }

    @Test
    public void shouldRunWithWarnings() {
        //given
        final String baseFilePath = "plain/warn";

        //when
        final PlainMessageAnalyzer analyzer = PlainMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH, "en,ru", "en", baseFilePath, null);

        //then
        assertThat(analyzer.getErrorCount(), is(0));
        assertThat(analyzer.getWarningCount(), is(1));
    }

    @Test
    public void shouldRunWithErrors() {
        //given
        final String baseFilePath = "plain/error";

        //when
        final PlainMessageAnalyzer analyzer = PlainMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH, "en,ru", "en", baseFilePath, null);

        //then
        assertThat(analyzer.getErrorCount(), is(1));
        assertThat(analyzer.getWarningCount(), is(0));
    }

    @Test
    public void shouldPassForMessageFormatted() {
        //given
        final String baseFilePath = "plain/msg_fmt";

        //when
        final PlainMessageAnalyzer analyzer = PlainMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH, "en,ru", "en", baseFilePath, "MESSAGE");

        //then
        assertThat(analyzer.getErrorCount(), is(0));
        assertThat(analyzer.getWarningCount(), is(0));
    }

    @Test
    public void shouldRunWithErrorsForMessageFormatted() {
        //given
        final String baseFilePath = "plain/msg_fmt_error";

        //when
        final PlainMessageAnalyzer analyzer = PlainMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH, "en,ru", "en", baseFilePath, "MESSAGE");

        //then
        assertThat(analyzer.getErrorCount(), is(1));
        assertThat(analyzer.getWarningCount(), is(0));
    }

    @Test
    public void shouldPassForStringFormatted() {
        //given
        final String baseFilePath = "plain/str_fmt";

        //when
        final PlainMessageAnalyzer analyzer = PlainMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH, "en,ru", "en", baseFilePath, "STRING");

        //then
        assertThat(analyzer.getErrorCount(), is(0));
        assertThat(analyzer.getWarningCount(), is(0));
    }

    @Test
    public void shouldRunWithErrorsForStringFormatted() {
        //given
        final String baseFilePath = "plain/str_fmt_error";

        //when
        final PlainMessageAnalyzer analyzer = PlainMessageAnalyzer.check(new SystemStreamLog(),
                ROOT_PATH, "en,ru", "en", baseFilePath, "STRING");

        //then
        assertThat(analyzer.getErrorCount(), is(1));
        assertThat(analyzer.getWarningCount(), is(0));
    }
}
