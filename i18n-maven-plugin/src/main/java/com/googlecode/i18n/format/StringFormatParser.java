package com.googlecode.i18n.format;

import java.util.ArrayList;
import java.util.DuplicateFormatFlagsException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.IllegalFormatFlagsException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllegalFormatWidthException;
import java.util.List;
import java.util.MissingFormatWidthException;
import java.util.UnknownFormatConversionException;
import java.util.UnknownFormatFlagsException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses <code>String.format()</code>.
 * 
 * <p>Example: for input string 
 * <blockquote>Some message %s, some number %d</blockquote>
 * returns
 * <blockquote>[%s, %d]</blockquote>
 */
public final class StringFormatParser extends AbstractFormatParser {
    
    // %[argument_index$][flags][width][.precision][t]conversion
    private static Pattern fsPattern = Pattern.compile(
            "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

    @Override
    public FormatType getFormatType() {
        return FormatType.STRING;
    }

    @Override
    public String[] parse(String format) {
        List<String> list = new ArrayList<String>();

        for (FormatString fs : parseFormat(format)) {
            switch (fs.index()) {
            case -2: // fixed string, "%n", or "%%"
                continue;
            case -1: // relative index
                list.add(fs.toString());
                break;
            case 0: // ordinary index
                list.add(fs.toString());
                break;
            default: // explicit index
                list.add(fs.toString());
                break;
            }
        }

        return list.toArray(new String[list.size()]);
    }

    // Look for format specifiers in the format string.
    private FormatString[] parseFormat(String s) {
        List<FormatString> al = new ArrayList<FormatString>();
        Matcher m = fsPattern.matcher(s);
        int i = 0;
        while (i < s.length()) {
            if (m.find(i)) {
                // Anything between the start of the string and the beginning
                // of the format specifier is either fixed text or contains
                // an invalid format string.
                if (m.start() != i) {
                    // Make sure we didn't miss any invalid format specifiers
                    checkText(s.substring(i, m.start()));
                    // Assume previous characters were fixed text
                    al.add(new FixedString(s.substring(i, m.start())));
                }

                // Expect 6 groups in regular expression
                String[] sa = new String[6];
                for (int j = 0; j < m.groupCount(); j++) {
                    sa[j] = m.group(j + 1);
                }
                al.add(new FormatSpecifier(sa));
                i = m.end();
            } else {
                // No more valid format specifiers. Check for possible invalid
                // format specifiers.
                checkText(s.substring(i));
                // The rest of the string is fixed text
                al.add(new FixedString(s.substring(i)));
                break;
            }
        }

        return al.toArray(new FormatString[al.size()]);
    }

    private void checkText(String s) {
        int idx;
        // If there are any '%' in the given string, we got a bad format
        // specifier.
        if ((idx = s.indexOf('%')) != -1) {
            char c = (idx > s.length() - 2 ? '%' : s.charAt(idx + 1));
            throw new UnknownFormatConversionException(String.valueOf(c));
        }
    }

    private interface FormatString {

        int index();

        String toString();
    }

    private class FixedString implements FormatString {

        private String s;

        FixedString(String s) {
            this.s = s;
        }

        public int index() {
            return -2;
        }

        public String toString() {
            return s;
        }
    }

    
    public enum BigDecimalLayoutForm {
        SCIENTIFIC, DECIMAL_FLOAT
    };

    
    private class FormatSpecifier implements FormatString {

        private int         index = -1;
        private Flags       f = Flags.NONE;
        private int         width;
        private int         precision;
        private boolean     dt = false;
        private char        c;

        private int index(String s) {
            if (s != null) {
                try {
                    index = Integer.parseInt(s.substring(0, s.length() - 1));
                } catch (NumberFormatException x) {
                    assert (false);
                }
            } else {
                index = 0;
            }
            return index;
        }

        public int index() {
            return index;
        }

        private Flags flags(String s) {
            f = Flags.parse(s);
            if (f.contains(Flags.PREVIOUS))
                index = -1;
            return f;
        }

        private int width(String s) {
            width = -1;
            if (s != null) {
                try {
                    width = Integer.parseInt(s);
                    if (width < 0)
                        throw new IllegalFormatWidthException(width);
                } catch (NumberFormatException x) {
                    assert (false);
                }
            }
            return width;
        }

        private int precision(String s) {
            precision = -1;
            if (s != null) {
                try {
                    // remove the '.'
                    precision = Integer.parseInt(s.substring(1));
                    if (precision < 0)
                        throw new IllegalFormatPrecisionException(precision);
                } catch (NumberFormatException x) {
                    assert (false);
                }
            }
            return precision;
        }

        private char conversion(String s) {
            c = s.charAt(0);
            if (!dt) {
                if (!Conversion.isValid(c))
                    throw new UnknownFormatConversionException(
                            String.valueOf(c));
                if (Character.isUpperCase(c))
                    f.add(Flags.UPPERCASE);
                c = Character.toLowerCase(c);
                if (Conversion.isText(c))
                    index = -2;
            }
            return c;
        }

        FormatSpecifier(String[] sa) {
            int idx = 0;

            index(sa[idx++]);
            flags(sa[idx++]);
            width(sa[idx++]);
            precision(sa[idx++]);

            if (sa[idx] != null) {
                dt = true;
                if (sa[idx].equals("T"))
                    f.add(Flags.UPPERCASE);
            }
            conversion(sa[++idx]);

            if (dt)
                checkDateTime();
            else if (Conversion.isGeneral(c))
                checkGeneral();
            else if (Conversion.isCharacter(c))
                checkCharacter();
            else if (Conversion.isInteger(c))
                checkInteger();
            else if (Conversion.isFloat(c))
                checkFloat();
            else if (Conversion.isText(c))
                checkText();
            else
                throw new UnknownFormatConversionException(String.valueOf(c));
        }

        public String toString() {
            StringBuilder sb = new StringBuilder('%');
            // Flags.UPPERCASE is set internally for legal conversions.
            Flags dupf = f.dup().remove(Flags.UPPERCASE);
            sb.append(dupf.toString());
            if (index > 0)
                sb.append(index).append('$');
            if (width != -1)
                sb.append(width);
            if (precision != -1)
                sb.append('.').append(precision);
            if (dt)
                sb.append(f.contains(Flags.UPPERCASE) ? 'T' : 't');
            sb.append(f.contains(Flags.UPPERCASE) ? Character.toUpperCase(c)
                    : c);
            return sb.toString();
        }

        private void checkGeneral() {
            if ((c == Conversion.BOOLEAN || c == Conversion.HASHCODE)
                    && f.contains(Flags.ALTERNATE))
                failMismatch(Flags.ALTERNATE, c);
            // '-' requires a width
            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY))
                throw new MissingFormatWidthException(toString());
            checkBadFlags(Flags.PLUS, Flags.LEADING_SPACE, Flags.ZERO_PAD,
                    Flags.GROUP, Flags.PARENTHESES);
        }

        private void checkDateTime() {
            if (precision != -1)
                throw new IllegalFormatPrecisionException(precision);
            if (!DateTime.isValid(c))
                throw new UnknownFormatConversionException("t" + c);
            checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,
                    Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
            // '-' requires a width
            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY))
                throw new MissingFormatWidthException(toString());
        }

        private void checkCharacter() {
            if (precision != -1)
                throw new IllegalFormatPrecisionException(precision);
            checkBadFlags(Flags.ALTERNATE, Flags.PLUS, Flags.LEADING_SPACE,
                    Flags.ZERO_PAD, Flags.GROUP, Flags.PARENTHESES);
            // '-' requires a width
            if (width == -1 && f.contains(Flags.LEFT_JUSTIFY))
                throw new MissingFormatWidthException(toString());
        }

        private void checkInteger() {
            checkNumeric();
            if (precision != -1)
                throw new IllegalFormatPrecisionException(precision);

            if (c == Conversion.DECIMAL_INTEGER)
                checkBadFlags(Flags.ALTERNATE);
            else if (c == Conversion.OCTAL_INTEGER)
                checkBadFlags(Flags.GROUP);
            else
                checkBadFlags(Flags.GROUP);
        }

        private void checkBadFlags(Flags... badFlags) {
            for (int i = 0; i < badFlags.length; i++)
                if (f.contains(badFlags[i]))
                    failMismatch(badFlags[i], c);
        }

        private void checkFloat() {
            checkNumeric();
            if (c == Conversion.DECIMAL_FLOAT) {
            } else if (c == Conversion.HEXADECIMAL_FLOAT) {
                checkBadFlags(Flags.PARENTHESES, Flags.GROUP);
            } else if (c == Conversion.SCIENTIFIC) {
                checkBadFlags(Flags.GROUP);
            } else if (c == Conversion.GENERAL) {
                checkBadFlags(Flags.ALTERNATE);
            }
        }

        private void checkNumeric() {
            if (width != -1 && width < 0)
                throw new IllegalFormatWidthException(width);

            if (precision != -1 && precision < 0)
                throw new IllegalFormatPrecisionException(precision);

            // '-' and '0' require a width
            if (width == -1
                    && (f.contains(Flags.LEFT_JUSTIFY) || f
                            .contains(Flags.ZERO_PAD)))
                throw new MissingFormatWidthException(toString());

            // bad combination
            if ((f.contains(Flags.PLUS) && f.contains(Flags.LEADING_SPACE))
                    || (f.contains(Flags.LEFT_JUSTIFY) && f
                            .contains(Flags.ZERO_PAD)))
                throw new IllegalFormatFlagsException(f.toString());
        }

        private void checkText() {
            if (precision != -1)
                throw new IllegalFormatPrecisionException(precision);
            switch (c) {
            case Conversion.PERCENT_SIGN:
                if (f.valueOf() != Flags.LEFT_JUSTIFY.valueOf()
                        && f.valueOf() != Flags.NONE.valueOf())
                    throw new IllegalFormatFlagsException(f.toString());
                // '-' requires a width
                if (width == -1 && f.contains(Flags.LEFT_JUSTIFY))
                    throw new MissingFormatWidthException(toString());
                break;
            case Conversion.LINE_SEPARATOR:
                if (width != -1)
                    throw new IllegalFormatWidthException(width);
                if (f.valueOf() != Flags.NONE.valueOf())
                    throw new IllegalFormatFlagsException(f.toString());
                break;
            default:
                assert false;
            }
        }

        // -- Methods to support throwing exceptions --

        private void failMismatch(Flags f, char c) {
            String fs = f.toString();
            throw new FormatFlagsConversionMismatchException(fs, c);
        }

    }

    
    private static class Flags {

        private int flags;

        static final Flags NONE = new Flags(0); // ''

        // duplicate declarations from Formattable.java
        static final Flags LEFT_JUSTIFY = new Flags(1 << 0); // '-'
        static final Flags UPPERCASE = new Flags(1 << 1); // '^'
        static final Flags ALTERNATE = new Flags(1 << 2); // '#'

        // numerics
        static final Flags PLUS = new Flags(1 << 3); // '+'
        static final Flags LEADING_SPACE = new Flags(1 << 4); // ' '
        static final Flags ZERO_PAD = new Flags(1 << 5); // '0'
        static final Flags GROUP = new Flags(1 << 6); // ','
        static final Flags PARENTHESES = new Flags(1 << 7); // '('

        // indexing
        static final Flags PREVIOUS = new Flags(1 << 8); // '<'

        private Flags(int f) {
            flags = f;
        }

        private int valueOf() {
            return flags;
        }

        private boolean contains(Flags f) {
            return (flags & f.valueOf()) == f.valueOf();
        }

        private Flags dup() {
            return new Flags(flags);
        }

        private Flags add(Flags f) {
            flags |= f.valueOf();
            return this;
        }

        private Flags remove(Flags f) {
            flags &= ~f.valueOf();
            return this;
        }

        private static Flags parse(String s) {
            char[] ca = s.toCharArray();
            Flags f = new Flags(0);
            for (int i = 0; i < ca.length; i++) {
                Flags v = parse(ca[i]);
                if (f.contains(v))
                    throw new DuplicateFormatFlagsException(v.toString());
                f.add(v);
            }
            return f;
        }

        // parse those flags which may be provided by users
        private static Flags parse(char c) {
            switch (c) {
            case '-':
                return LEFT_JUSTIFY;
            case '#':
                return ALTERNATE;
            case '+':
                return PLUS;
            case ' ':
                return LEADING_SPACE;
            case '0':
                return ZERO_PAD;
            case ',':
                return GROUP;
            case '(':
                return PARENTHESES;
            case '<':
                return PREVIOUS;
            default:
                throw new UnknownFormatFlagsException(String.valueOf(c));
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (contains(LEFT_JUSTIFY))
                sb.append('-');
            if (contains(UPPERCASE))
                sb.append('^');
            if (contains(ALTERNATE))
                sb.append('#');
            if (contains(PLUS))
                sb.append('+');
            if (contains(LEADING_SPACE))
                sb.append(' ');
            if (contains(ZERO_PAD))
                sb.append('0');
            if (contains(GROUP))
                sb.append(',');
            if (contains(PARENTHESES))
                sb.append('(');
            if (contains(PREVIOUS))
                sb.append('<');
            return sb.toString();
        }
    }

    
    private static class Conversion {

        // Byte, Short, Integer, Long, BigInteger
        // (and associated primitives due to autoboxing)
        static final char DECIMAL_INTEGER = 'd';
        static final char OCTAL_INTEGER = 'o';
        static final char HEXADECIMAL_INTEGER = 'x';
        static final char HEXADECIMAL_INTEGER_UPPER = 'X';

        // Float, Double, BigDecimal
        // (and associated primitives due to autoboxing)
        static final char SCIENTIFIC = 'e';
        static final char SCIENTIFIC_UPPER = 'E';
        static final char GENERAL = 'g';
        static final char GENERAL_UPPER = 'G';
        static final char DECIMAL_FLOAT = 'f';
        static final char HEXADECIMAL_FLOAT = 'a';
        static final char HEXADECIMAL_FLOAT_UPPER = 'A';

        // Character, Byte, Short, Integer
        // (and associated primitives due to autoboxing)
        static final char CHARACTER = 'c';
        static final char CHARACTER_UPPER = 'C';

        // if (arg.TYPE != boolean) return boolean
        // if (arg != null) return true; else return false;
        static final char BOOLEAN = 'b';
        static final char BOOLEAN_UPPER = 'B';
        // if (arg instanceof Formattable) arg.formatTo()
        // else arg.toString();
        static final char STRING = 's';
        static final char STRING_UPPER = 'S';
        // arg.hashCode()
        static final char HASHCODE = 'h';
        static final char HASHCODE_UPPER = 'H';

        static final char LINE_SEPARATOR = 'n';
        static final char PERCENT_SIGN = '%';

        static boolean isValid(char c) {
            return (isGeneral(c) || isInteger(c) || isFloat(c) || isText(c)
                    || c == 't' || isCharacter(c));
        }

        // Returns true iff the Conversion is applicable to all objects.
        static boolean isGeneral(char c) {
            switch (c) {
            case BOOLEAN:
            case BOOLEAN_UPPER:
            case STRING:
            case STRING_UPPER:
            case HASHCODE:
            case HASHCODE_UPPER:
                return true;
            default:
                return false;
            }
        }

        // Returns true iff the Conversion is applicable to character.
        static boolean isCharacter(char c) {
            switch (c) {
            case CHARACTER:
            case CHARACTER_UPPER:
                return true;
            default:
                return false;
            }
        }

        // Returns true iff the Conversion is an integer type.
        static boolean isInteger(char c) {
            switch (c) {
            case DECIMAL_INTEGER:
            case OCTAL_INTEGER:
            case HEXADECIMAL_INTEGER:
            case HEXADECIMAL_INTEGER_UPPER:
                return true;
            default:
                return false;
            }
        }

        // Returns true iff the Conversion is a floating-point type.
        static boolean isFloat(char c) {
            switch (c) {
            case SCIENTIFIC:
            case SCIENTIFIC_UPPER:
            case GENERAL:
            case GENERAL_UPPER:
            case DECIMAL_FLOAT:
            case HEXADECIMAL_FLOAT:
            case HEXADECIMAL_FLOAT_UPPER:
                return true;
            default:
                return false;
            }
        }

        // Returns true iff the Conversion does not require an argument
        static boolean isText(char c) {
            switch (c) {
            case LINE_SEPARATOR:
            case PERCENT_SIGN:
                return true;
            default:
                return false;
            }
        }
    }

    private static class DateTime {

        static final char HOUR_OF_DAY_0 = 'H'; // (00 - 23)
        static final char HOUR_0 = 'I'; // (01 - 12)
        static final char HOUR_OF_DAY = 'k'; // (0 - 23) -- like H
        static final char HOUR = 'l'; // (1 - 12) -- like I
        static final char MINUTE = 'M'; // (00 - 59)
        static final char NANOSECOND = 'N'; // (000000000 - 999999999)
        static final char MILLISECOND = 'L'; // jdk, not in gnu (000 - 999)
        static final char MILLISECOND_SINCE_EPOCH = 'Q'; // (0 - 99...?)
        static final char AM_PM = 'p'; // (am or pm)
        static final char SECONDS_SINCE_EPOCH = 's'; // (0 - 99...?)
        static final char SECOND = 'S'; // (00 - 60 - leap second)
        static final char TIME = 'T'; // (24 hour hh:mm:ss)
        static final char ZONE_NUMERIC = 'z'; // (-1200 - +1200) - ls minus?
        static final char ZONE = 'Z'; // (symbol)

        // Date
        static final char NAME_OF_DAY_ABBREV = 'a'; // 'a'
        static final char NAME_OF_DAY = 'A'; // 'A'
        static final char NAME_OF_MONTH_ABBREV = 'b'; // 'b'
        static final char NAME_OF_MONTH = 'B'; // 'B'
        static final char CENTURY = 'C'; // (00 - 99)
        static final char DAY_OF_MONTH_0 = 'd'; // (01 - 31)
        static final char DAY_OF_MONTH = 'e'; // (1 - 31) -- like d
        static final char NAME_OF_MONTH_ABBREV_X = 'h'; // -- same b
        static final char DAY_OF_YEAR = 'j'; // (001 - 366)
        static final char MONTH = 'm'; // (01 - 12)
        static final char YEAR_2 = 'y'; // (00 - 99)
        static final char YEAR_4 = 'Y'; // (0000 - 9999)

        // Composites
        static final char TIME_12_HOUR = 'r'; // (hh:mm:ss [AP]M)
        static final char TIME_24_HOUR = 'R'; // (hh:mm same as %H:%M)
        static final char DATE_TIME = 'c';
        // (Sat Nov 04 12:02:33 EST 1999)
        static final char DATE = 'D'; // (mm/dd/yy)
        static final char ISO_STANDARD_DATE = 'F'; // (%Y-%m-%d)

        static boolean isValid(char c) {
            switch (c) {
            case HOUR_OF_DAY_0:
            case HOUR_0:
            case HOUR_OF_DAY:
            case HOUR:
            case MINUTE:
            case NANOSECOND:
            case MILLISECOND:
            case MILLISECOND_SINCE_EPOCH:
            case AM_PM:
            case SECONDS_SINCE_EPOCH:
            case SECOND:
            case TIME:
            case ZONE_NUMERIC:
            case ZONE:

                // Date
            case NAME_OF_DAY_ABBREV:
            case NAME_OF_DAY:
            case NAME_OF_MONTH_ABBREV:
            case NAME_OF_MONTH:
            case CENTURY:
            case DAY_OF_MONTH_0:
            case DAY_OF_MONTH:
            case NAME_OF_MONTH_ABBREV_X:
            case DAY_OF_YEAR:
            case MONTH:
            case YEAR_2:
            case YEAR_4:

                // Composites
            case TIME_12_HOUR:
            case TIME_24_HOUR:
            case DATE_TIME:
            case DATE:
            case ISO_STANDARD_DATE:
                return true;
            default:
                return false;
            }
        }
    }
}
