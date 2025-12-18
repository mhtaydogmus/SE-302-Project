package edu.ieu.se302.examscheduler.ui.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class I18n {
    private static Locale locale = Locale.ENGLISH;
    private static ResourceBundle bundle = loadBundle(locale);

    private I18n() {
    }

    public static void setLocale(Locale newLocale) {
        if (newLocale == null) {
            return;
        }
        locale = newLocale;
        bundle = loadBundle(locale);
    }

    public static Locale getLocale() {
        return locale;
    }

    public static String get(String key) {
        if (key == null) {
            return "";
        }
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        }
        return key;
    }

    public static String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }

    private static ResourceBundle loadBundle(Locale locale) {
        return ResourceBundle.getBundle("i18n.messages", locale);
    }
}
