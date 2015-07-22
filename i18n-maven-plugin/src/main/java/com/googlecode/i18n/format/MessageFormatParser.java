package com.googlecode.i18n.format;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses <code>MessageFormat.format()</code>.
 * 
 * <p>Example: for input string
 * <blockquote>"Some message {0, number, #}, some number {1,time, full}"
 * </blockquote>
 * returns:
 * <blockquote>[0,number, 1,time]</blockquote>
 */
public final class MessageFormatParser {
    
    private final List<String>  result = new ArrayList<String>();

    public static String[] parse(String pattern) {
        MessageFormatParser parser = new MessageFormatParser();
        parser.applyPattern(pattern);
        
        return parser.result.toArray(new String[parser.result.size()]);
    }
       
    /**
     * Sets the pattern used by this message format.
     * The method parses the pattern and creates a list of subformats
     * for the format elements contained in it.
     * Patterns and their interpretation are specified in the
     * <a href="#patterns">class description</a>.
     * 
     * @param pattern the pattern for this message format
     * @exception IllegalArgumentException if the pattern is invalid
     */
    private void applyPattern(String pattern) {
        StringBuffer[] segments = new StringBuffer[4];
        for (int i = 0; i < segments.length; ++i) {
            segments[i] = new StringBuffer();
        }
        
        int part = 0;
        //int formatNumber = 0;
        boolean inQuote = false;    // in ''
        int braceStack = 0;         // in {}
        for (int i = 0; i < pattern.length(); ++i) {
            char ch = pattern.charAt(i);
            if (part == 0) {
                if (ch == '\'') {
                    if (i + 1 < pattern.length()
                        && pattern.charAt(i+1) == '\'') {
                        segments[part].append(ch);  // handle doubles
                        ++i;
                    } else {
                        inQuote = !inQuote;
                    }
                } else if (ch == '{' && !inQuote) {
                    part = 1;
                } else {
                    segments[part].append(ch); 
                }
            } else  if (inQuote) {              // just copy quotes in parts
                segments[part].append(ch);
                if (ch == '\'') {
                    inQuote = false;
                }
            } else {
                switch (ch) {
                case ',':
                    if (part < 3)
                        part += 1;
                    else
                        segments[part].append(ch);
                    break;
                case '{':
                    ++braceStack;
                    segments[part].append(ch);
                    break;
                case '}':
                    if (braceStack == 0) {
                        part = 0;
                        
                        addFormat(segments);
                        //formatNumber++;
                    } else {
                        --braceStack;
                        segments[part].append(ch);
                    }
                    break;
                case '\'':
                    inQuote = true;
                    // fall through, so we keep quotes in other parts
                default:
                    segments[part].append(ch);
                    break;
                }
            }
        }
        
        if (braceStack == 0 && part != 0) {
            throw new IllegalArgumentException(
                    "Unmatched braces in the pattern");
        }
    }
    
    /**
     * Adds format to result. Checks argument number. 
     * If it wrong throws <code>IllegalArgumentException</code>
     * 
     * @param segments array with message and format
     */
    private void addFormat(StringBuffer[] segments) {
        // get the argument number
        int argumentNumber;
        try {
            // always unlocalized!
            argumentNumber = Integer.parseInt(segments[1].toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("can't parse argument number "
                    + segments[1]);
        }
        if (argumentNumber < 0) {
            throw new IllegalArgumentException("negative argument number "
                    + argumentNumber);
        }

        String formatType = segments[2].toString();
        if (!(formatType.isEmpty() 
                || formatType.equals("number") 
                || formatType.equals("date") 
                || formatType.equals("time") 
                || formatType.equals("choice"))) {
            
            throw new IllegalArgumentException("Format type = '" 
                    + formatType + "'");
        }
        
        this.result.add(argumentNumber + "," + formatType);
        
        segments[1].setLength(0);   // throw away other segments
        segments[2].setLength(0);
        segments[3].setLength(0);
    }
}
