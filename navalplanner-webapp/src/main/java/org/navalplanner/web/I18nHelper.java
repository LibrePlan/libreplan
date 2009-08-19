package org.navalplanner.web;

import java.util.HashMap;
import java.util.Locale;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;
import org.zkoss.util.Locales;


public class I18nHelper {

    private static HashMap<Locale, I18n> localesCache = new HashMap<Locale, I18n>();

    public static I18n getI18n() {
        if (localesCache.keySet().contains(Locales.getCurrent())) {
            return localesCache.get(Locales.getCurrent());
        }

        I18n i18n = I18nFactory.getI18n(I18nHelper.class, Locales
                .getCurrent(),
                org.xnap.commons.i18n.I18nFactory.FALLBACK);
        localesCache.put(Locales.getCurrent(), i18n);

        return i18n;
    }

    public static String _(String str) {
        return getI18n().tr(str);
    }

    public static String _(String text, Object o1) {
        return getI18n().tr(text, o1);
    }

    public static String _(String text, Object o1, Object o2) {
        return getI18n().tr(text, o1, o2);
    }

    public static String _(String text, Object o1, Object o2, Object o3) {
        return getI18n().tr(text, o1, o2, o3);
    }

    public static String _(String text, Object o1, Object o2, Object o3,
            Object o4) {
        return getI18n().tr(text, o1, o2, o3, o4);
    }

    public static String _(String text, Object[] objects) {
        return getI18n().tr(text, objects);
    }
}
