package com.omareitti;

import com.omareitti.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class SettingsScreen extends PreferenceActivity {
	
	public SharedPreferences prefs;
	public SharedPreferences.Editor editor;
	
	public ListPreference prefLanguage;
	public Preference prefAllowCoords;
	public ListPreference prefMapZoomLevel;
	public Preference prefRouteSearchOptions;
	public Preference prefTimeType;
	public ListPreference prefWalkingSpeed;
	public ListPreference prefDepNotifInterval;
	
	public Dialog moreOptionsDialog; 
	public ToggleButton tbBus;
	public ToggleButton tbTram;
	public ToggleButton tbMetro;
	public ToggleButton tbTrain;
	public ToggleButton tbWalk;
	public Spinner spinnerOptions;
    public String transport_types;
    public String optimize;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	addPreferencesFromResource(R.xml.preferences);

    	prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	/*
    	prefLanguage = (ListPreference) findPreference("prefLanguage");
    	prefLanguage.setSummary(prefLanguage.getValue());
    	prefLanguage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    		public boolean onPreferenceChange(Preference preference, Object newValue) {
    			prefLanguage.setSummary(newValue.toString());
    			return true;
    		}
    	});
    	 */
    	
    	prefAllowCoords = (Preference) findPreference("prefAllowCoords");
    	prefAllowCoords.setSummary(prefs.getString("prefAllowCoords", "Yes"));
    	prefAllowCoords.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
    		public boolean onPreferenceClick(Preference preference) {
    			editor = prefs.edit();
    			if (prefs.getString("prefAllowCoords", "Yes").equals("Yes")) 
    				editor.putString("prefAllowCoords", "No");
    			else 
    				editor.putString("prefAllowCoords", "Yes");
    			editor.commit();
    			prefAllowCoords.setSummary(prefs.getString("prefAllowCoords", "Yes"));
    			return false;
    		}
    	});
    	
    	prefMapZoomLevel = (ListPreference) findPreference("prefMapZoomLevel");
    	prefMapZoomLevel.setSummary(prefMapZoomLevel.getValue());
    	prefMapZoomLevel.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    		public boolean onPreferenceChange(Preference preference, Object newValue) {
    			prefMapZoomLevel.setSummary(newValue.toString());
    			return true;
    		}
    	});

    	prefRouteSearchOptions = (Preference) findPreference("prefRouteSearchOptions");
    	prefRouteSearchOptions.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
    		public boolean onPreferenceClick(Preference preference) {
    			Context context = SettingsScreen.this;
    			View inflatedView = View.inflate(context, R.layout.moreoptionsdialog, null);

    			AlertDialog.Builder builder = new AlertDialog.Builder(context);
    			builder.setTitle(getString(R.string.moTitle)).setView(inflatedView);
    			builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog,	int which) {
    					setMoreOptions();
    				}
    			});
    			moreOptionsDialog = builder.create();

    			transport_types = prefs.getString("prefRouteSearchOptionsTT", "all");
    			optimize = prefs.getString("prefRouteSearchOptionsOptimize", "default");

    			tbBus = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonBus);
    			Drawable d = getResources().getDrawable(R.drawable.bus);
    			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
    			tbBus.setCompoundDrawables(null, d, null, null);
    			tbBus.setCompoundDrawablePadding(8);
    			tbBus.setChecked(transport_types.contains("bus|uline|service") || transport_types.equals("all"));
    			tbBus.setOnClickListener(toggleListener);

    			tbTram = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonTram);
    			d = getResources().getDrawable(R.drawable.tram);
    			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
    			tbTram.setCompoundDrawables(null, d, null, null);
    			tbTram.setCompoundDrawablePadding(8);
    			tbTram.setChecked(transport_types.contains("tram") || transport_types.equals("all"));
    			tbTram.setOnClickListener(toggleListener);

    			tbMetro = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonMetro);
    			d = getResources().getDrawable(R.drawable.metro);
    			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
    			tbMetro.setCompoundDrawables(null, d, null, null);
    			tbMetro.setCompoundDrawablePadding(8);
    			tbMetro.setChecked(transport_types.contains("metro") || transport_types.equals("all"));
    			tbMetro.setOnClickListener(toggleListener);

    			tbTrain = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonTrain);
    			d = getResources().getDrawable(R.drawable.train);
    			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
    			tbTrain.setCompoundDrawables(null, d, null, null);
    			tbTrain.setCompoundDrawablePadding(8);
    			tbTrain.setChecked(transport_types.contains("train") || transport_types.equals("all"));
    			tbTrain.setOnClickListener(toggleListener);

    			tbWalk = (ToggleButton) inflatedView.findViewById(R.id.toggleButtonWalk);
    			d = getResources().getDrawable(R.drawable.man);
    			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
    			tbWalk.setCompoundDrawables(null, d, null, null);
    			tbWalk.setCompoundDrawablePadding(8);
    			tbWalk.setChecked(transport_types.contains("walk"));
    			tbWalk.setOnClickListener(toggleListener);

    			spinnerOptions = (Spinner) inflatedView.findViewById(R.id.moreOptionsSpinner);
    			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(inflatedView.getContext(), 
    					R.array.moOptimize, android.R.layout.simple_spinner_item);
    			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    			spinnerOptions.setAdapter(adapter);
    			if (optimize.equals("default"))	spinnerOptions.setSelection(0);
    			if (optimize.equals("fastest"))	spinnerOptions.setSelection(1);
    			if (optimize.equals("least_transfers"))	spinnerOptions.setSelection(2);
    			if (optimize.equals("least_walking")) spinnerOptions.setSelection(3);

    			moreOptionsDialog.show();
    			return false;
    		}
    	});

    	prefTimeType = (Preference) findPreference("prefTimeType");
    	prefTimeType.setSummary(prefs.getString("prefTimeType", getString(R.string.srDeparture)));
    	prefTimeType.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
    		public boolean onPreferenceClick(Preference preference) {
    			editor = prefs.edit();
    			if (prefs.getString("prefTimeType", getString(R.string.srDeparture)).equals(getString(R.string.srDeparture)))
    				editor.putString("prefTimeType", getString(R.string.srArrival));
    			else {
    				editor.putString("prefTimeType", getString(R.string.srDeparture));
    			}
    			editor.commit();
    			prefTimeType.setSummary(prefs.getString("prefTimeType",	getString(R.string.srDeparture)));
    			return false;
    		}
    	});

    	prefWalkingSpeed = (ListPreference) findPreference("prefWalkingSpeed");
    	prefWalkingSpeed.setSummary(String.format(getString(R.string.prefMperS), prefs.getString("prefWalkingSpeed", "1")));
    	prefWalkingSpeed.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    		public boolean onPreferenceChange(Preference preference, Object newValue) {
    			prefWalkingSpeed.setSummary(String.format(getString(R.string.prefMperS), newValue.toString()));
    			return true;
    		}
    	});
    	
    	prefDepNotifInterval = (ListPreference) findPreference("prefDepNotifInterval");
    	prefDepNotifInterval.setSummary(String.format(getString(R.string.minuteAbbr), Integer.parseInt(prefs.getString("prefDepNotifInterval", "5"))));
    	prefDepNotifInterval.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
    		public boolean onPreferenceChange(Preference preference, Object newValue) {
    			prefDepNotifInterval.setSummary(String.format(getString(R.string.minuteAbbr), Integer.parseInt(newValue.toString())));
    			return true;
    		}
    	});
    }

    private OnClickListener toggleListener = new OnClickListener() {
    	public void onClick(View v) {
    		if (v.getId() == tbWalk.getId()) {
    			tbBus.setChecked(false);
    			tbTram.setChecked(false);
    			tbMetro.setChecked(false);
    			tbTrain.setChecked(false);
    		} else {
    			if (tbWalk.isChecked())	tbWalk.setChecked(false);
    		}
    	}
    };
    
    private void setMoreOptions() {
    	transport_types = "";
    	if (tbBus.isChecked()) transport_types = (transport_types.equals("")) ? "bus|uline|service" : transport_types+"|bus|uline|service";
    	if (tbTram.isChecked()) transport_types = (transport_types.equals("")) ? "tram" : transport_types+"|tram";
    	if (tbMetro.isChecked()) transport_types = (transport_types.equals("")) ? "metro" : transport_types+"|metro";
    	if (tbTrain.isChecked()) transport_types = (transport_types.equals("")) ? "train" : transport_types+"|train";
    	if (tbWalk.isChecked()) transport_types = "walk";

    	if (transport_types.equals("bus|uline|service|tram|metro|train")) transport_types = "all";
    	
    	optimize = "";
    	switch (spinnerOptions.getSelectedItemPosition()) {
    		case 0: 
    			optimize = "default";
    			break;
    		case 1:
    			optimize = "fastest";
    			break;
    		case 2:
    			optimize = "least_transfers";
    			break;
    		case 3:
    			optimize = "least_walking";
    			break;
    		default:
    			break;
    	}
    	
    	editor = prefs.edit();
    	editor.putString("prefRouteSearchOptionsTT", transport_types);
    	editor.putString("prefRouteSearchOptionsOptimize", optimize);
    	editor.commit();
    	moreOptionsDialog.dismiss();    	
    }
}


//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
//prefs.getString("prefLanguage", "swedish")