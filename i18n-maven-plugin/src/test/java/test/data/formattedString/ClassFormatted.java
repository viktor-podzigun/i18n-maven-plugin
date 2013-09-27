
package test.data.formattedString;

import com.googlecode.i18n.annotations.FormatType;
import com.googlecode.i18n.annotations.FormattedMessage;
import com.googlecode.i18n.annotations.LocalizedMessage;


public enum ClassFormatted implements LocalizedMessage {
    
    @FormattedMessage(type = FormatType.STRING_FORMAT)
    PARAMETR1("par.1"),
    
    @FormattedMessage(type = FormatType.STRING_FORMAT)
    PARAMETR2("par.2"),
    
    PARAMETR3("par.3"),
    
    @FormattedMessage(type = FormatType.STRING_FORMAT)
    PARAMETR4("par.4"),
    ;
    
    private final String id;
    
    ClassFormatted(String id) {
        this.id = id;        
    }

    @Override
    public String getMessageId() {
        return id;
    }

}
