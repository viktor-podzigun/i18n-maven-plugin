
package formatted.str;

import com.googlecode.i18n.annotations.StringFormatted;
import com.googlecode.i18n.annotations.LocalizedMessage;


public enum FormattedStr implements LocalizedMessage {
    
    @StringFormatted
    PARAMETR1("par.1"),
    
    @StringFormatted
    PARAMETR2("par.2"),
    
    PARAMETR3("par.3"),
    
    @StringFormatted
    PARAMETR4("par.4"),
    ;
    
    private final String id;
    
    FormattedStr(String id) {
        this.id = id;        
    }

    @Override
    public String getMessageId() {
        return id;
    }

}
