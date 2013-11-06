
package com.googlecode.i18n.demo;

import com.googlecode.i18n.annotations.MessageFormatted;
import com.googlecode.i18n.annotations.MessageProvider;
import com.googlecode.i18n.annotations.StringFormatted;


/**
 * Simple localized messages for demo.
 */
@MessageProvider
public enum Messages {

    HELLO_WORLD,
    
    @MessageFormatted
    HELLO_WORLD_MSG,
    
    @StringFormatted
    HELLO_WORLD_STR,
    
}
