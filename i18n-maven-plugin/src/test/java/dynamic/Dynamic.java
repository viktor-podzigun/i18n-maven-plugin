
package dynamic;

import java.util.ArrayList;
import java.util.List;
import com.googlecode.i18n.annotations.MessageProvider;


@MessageProvider
public enum Dynamic {
    
    // Static localized messages
    
    MSG_1, 
    MSG_2,
    ;

    
    /**
     * Returns dynamic localized messages.
     * @return dynamic localized messages
     */
    public static List<String> i18nMessages() {
        List<String> keys = new ArrayList<String>();
        keys.add("MSG_DYNAMIC_" + "1");
        keys.add("MSG_DYNAMIC_" + "2");
        return keys;
    }

}
