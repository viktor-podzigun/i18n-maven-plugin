
package com.googlecode.i18n.annotations;


/**
 * Format type for formatted message.
 * 
 * @see FormattedMessage
 */
public enum FormatType {
    
    /** Message is formatted using <code>String.format()</code> format */
    STRING_FORMAT,
    
    /** Message is formatted using <code>MessageFormat.format()</code> format */
    MESSAGE_FORMAT,
    
}
