package org.foxteam.noisyfox.noexclamation;

import android.app.Application;

//This class allows global method for StringRes for translation

public class App extends Application {

    private static App app; //ApplicationContext reference (safe, no context leak)

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static String getStr(int resId) {
        return app.getString(resId);
    }
}
