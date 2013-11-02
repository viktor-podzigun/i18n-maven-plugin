
package formatted.msg;

import com.googlecode.i18n.annotations.MessageFormatted;
import com.googlecode.i18n.annotations.LocalizedMessage;


public enum FormattedMsg implements LocalizedMessage{
    
    @MessageFormatted
    PARAMETR1("par.1"),
    
    PARAMETR2("par.2"),
    
    @MessageFormatted
    PARAMETR3("par.3"),
    
    @MessageFormatted
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
