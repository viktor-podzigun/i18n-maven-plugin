
package test.data.classTest;

import java.util.ArrayList;
import java.util.List;
import com.googlecode.i18n.annotations.LocalizedMessage;


public enum MyClass implements LocalizedMessage{
        
    PARAMETR1("par.1"), 
    PARAMETR2("par.2"),
    ;

    private static final String PARAMETR3 = "par.3";

    private final String id;

    private MyClass(String id) {
        this.id = id;
    }

    public static List<String> getI18nMessagesKeys() {
        // add constant keys
        List<String> keys = new ArrayList<String>();
        keys.add(PARAMETR3);

        return keys;
    }

    @Override
    public String getMessageId() {
        return id;
    }

}
