
package formatted.msg;

import com.googlecode.i18n.annotations.FormatType;
import com.googlecode.i18n.annotations.FormattedMessage;
import com.googlecode.i18n.annotations.LocalizedMessage;


public enum FormattedMsg implements LocalizedMessage{
    
    @FormattedMessage(type = FormatType.MESSAGE_FORMAT)
    PARAMETR1("par.1"),
    
    PARAMETR2("par.2"),
    @FormattedMessage(type = FormatType.MESSAGE_FORMAT)
    
    PARAMETR3("par.3"),
    
    @FormattedMessage(type = FormatType.MESSAGE_FORMAT)
    PARAMETR4("par.4"),
    ;
    
    private final String id;
    
    FormattedMsg(String id) {
        this.id = id;        
    }

    @Override
    public String getMessageId() {
        return id;
    }

}
