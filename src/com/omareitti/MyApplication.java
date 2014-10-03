package com.omareitti;

import android.app.Application;
import android.preference.PreferenceManager;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
	PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

	super.onCreate();
    }
}
