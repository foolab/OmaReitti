package com.omareitti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsScreen extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.preferences);

	Preference pref = (Preference) findPreference("prefRouteSearchOptions");;
	pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		@Override
    		public boolean onPreferenceClick(Preference preference) {
		    final SharedPreferences prefs =
			PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		    String transport_types = prefs.getString("prefRouteSearchOptionsTT", "all");
		    String optimize = prefs.getString("prefRouteSearchOptionsOptimize", "default");

		    TravelOptionsDialog.TravelOptionsDialogResults listener =
			new TravelOptionsDialog.TravelOptionsDialogResults() {
			    public void onDone(String optimize, String transportTypes) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("prefRouteSearchOptionsTT", transportTypes);
				editor.putString("prefRouteSearchOptionsOptimize", optimize);
				editor.commit();
			    }
			};

		    AlertDialog dlg =
			TravelOptionsDialog.create(SettingsScreen.this, transport_types,
						   optimize, listener);
		    dlg.show();

		    return false;
		}
	    });
    }
}
