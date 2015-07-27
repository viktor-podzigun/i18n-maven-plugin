
package com.googlecode.i18n.demo;

import java.text.MessageFormat;
import java.util.Locale;


public final class FullExampleMain {

    public static void main(String[] args) {
        Locale.setDefault(new Locale("en"));
        System.out.println(FullExample.HELLO_WORLD);
        System.out.println(MessageFormat.format(
                FullExample.NAME_MSG.toString(), "Viktor"));
        System.out.println(String.format(
                FullExample.LANGUAGE_STR.toString(), "Java"));
    
        Locale.setDefault(new Locale("ru"));
        System.out.println(FullExample.HELLO_WORLD);
        System.out.println(MessageFormat.format(
                FullExample.NAME_MSG.toString(), "Viktor"));
        System.out.println(String.format(
                FullExample.LANGUAGE_STR.toString(), "Java"));
    }

}
