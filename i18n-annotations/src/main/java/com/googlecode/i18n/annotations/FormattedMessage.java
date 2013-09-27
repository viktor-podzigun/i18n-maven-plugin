
package com.googlecode.i18n.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that localized message is formatted. Default format is
 * <code>java.text.MessageFormat</code>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FormattedMessage {

    FormatType type() default FormatType.MESSAGE_FORMAT;

}
