
package com.googlecode.i18n;

import com.googlecode.i18n.annotations.FormatType;


/**
 * Class to store formatted messages. Stores the value of the message and 
 * format information.
 */
public final class MessageInfo {
    
    private final String     id; 
    private final FormatType type;
    
    
    public MessageInfo(String id, FormatType type) {
        this.id   = id;
        this.type = type;
    }
            
    public String getId() {
        return id;
    }        
    
    public FormatType getType() {
        return type;
    }

}
