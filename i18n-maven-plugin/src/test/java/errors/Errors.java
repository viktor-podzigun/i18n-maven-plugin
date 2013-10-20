
package errors;

import com.googlecode.i18n.annotations.LocalizedMessage;


public enum Errors implements LocalizedMessage{
    
    PARAMETR1("par.1"),
    PARAMETR2("par.2"),
    PARAMETR3("par.3");
    
    private final String id;
    
    private Errors(String id) {
        this.id = id;
    }

    @Override
    public String getMessageId() {
        return id;
    }

}
