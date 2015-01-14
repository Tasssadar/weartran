package com.tassadar.weartran;

import android.app.Application;
import android.content.Context;

public class WeartranApp extends Application {
    private static Context m_context;

    @Override
    public void onCreate() {
        super.onCreate();
        m_context = getApplicationContext();
    }

    public static Context ctx() {
        return m_context;
    }
}
