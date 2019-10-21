package com.konkawise.dtv;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import com.konkawise.dtv.annotation.LocaleType;

import java.util.Locale;

public class LanguageManager {

    private static class LanguageManagerHolder {
        private static final LanguageManager INSTANCE = new LanguageManager();
    }

    public static LanguageManager getInstance() {
        return LanguageManagerHolder.INSTANCE;
    }

    /**
     * 修改应用内的语言
     */
    public void updateLanguage(Context context, @LocaleType int localeType) {
        Resources resources = context.getResources();
        resources.updateConfiguration(createAppLanguageConfiguration(context, localeType), resources.getDisplayMetrics());

        PreferenceManager.getInstance().putInt(Constants.PrefsKey.LOCALE_TYPE, localeType);
    }

    public boolean isSameLanguage(@LocaleType int localeType) {
        return localeType == PreferenceManager.getInstance().getInt(Constants.PrefsKey.LOCALE_TYPE);
    }

    private Configuration createAppLanguageConfiguration(Context context, @LocaleType int localeType) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale updateLocale = getLocale(localeType);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList newLocaleList = new LocaleList(updateLocale);
            LocaleList.setDefault(newLocaleList);
            configuration.setLocales(newLocaleList);

            configuration.setLocale(updateLocale);
        } else {
            configuration.locale = updateLocale;
        }
        return configuration;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Context wrapLocaleContext(Context context) {
        return context.createConfigurationContext(createAppLanguageConfiguration(context,
                PreferenceManager.getInstance().getInt(Constants.PrefsKey.LOCALE_TYPE)));
    }

    private Locale getLocale(int localeType) {
        switch (localeType) {
            case Constants.LocaleType.ITALIAN:
                return Locale.ITALIAN;
            case Constants.LocaleType.ENGLISH:
                return Locale.ENGLISH;
            case Constants.LocaleType.CHINESE:
                return Locale.CHINESE;
            default:
                return Resources.getSystem().getConfiguration().locale;
        }
    }
}
