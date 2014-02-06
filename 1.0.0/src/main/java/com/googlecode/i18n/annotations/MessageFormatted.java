
package com.googlecode.i18n.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;


/**
 * Indicates that localized message(s) formatted using
 * {@link MessageFormat#format(String, Object...)}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface MessageFormatted {

}
