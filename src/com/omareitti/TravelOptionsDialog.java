package com.omareitti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.Button;
import java.util.ArrayList;
import java.util.Arrays;
import android.text.TextUtils;

public class TravelOptionsDialog {
    public interface TravelOptionsDialogResults {
	public void onDone(String optimize, String transportTypes);
    };

    static AlertDialog create(Context context, String transportTypes,
			      String optimize, final TravelOptionsDialogResults listener) {
	final View view = View.inflate(context, R.layout.moreoptionsdialog, null);
	final ArrayList<String> strings =
	    new ArrayList(Arrays.asList(context.getResources().getTextArray(R.array.moOptimizeValues)));
	final Spinner spinner = (Spinner) view.findViewById(R.id.moreOptionsSpinner);
	final ToggleButton bus = (ToggleButton) view.findViewById(R.id.toggleButtonBus);
	final ToggleButton tram = (ToggleButton) view.findViewById(R.id.toggleButtonTram);
        final ToggleButton metro = (ToggleButton) view.findViewById(R.id.toggleButtonMetro);
	final ToggleButton train = (ToggleButton) view.findViewById(R.id.toggleButtonTrain);
	final ToggleButton ferry = (ToggleButton) view.findViewById(R.id.toggleButtonFerry);
	final ToggleButton walk = (ToggleButton) view.findViewById(R.id.toggleButtonWalk);

	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	builder.setTitle(context.getResources().getString(R.string.moTitle));
	builder.setView(view);

	DialogInterface.OnClickListener clickListener =
	    new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
		    String optimize = strings.get(spinner.getSelectedItemPosition());

		    ArrayList<String> types = new ArrayList();

		    if (bus.isChecked())
			types.add("bus|uline|service");

		    if (tram.isChecked())
			types.add("tram");

		    if (metro.isChecked())
			types.add("metro");

		    if (train.isChecked())
			types.add("train");

		    if (ferry.isChecked())
			types.add("ferry");

		    if (walk.isChecked()) {
			types.clear();
			types.add("walk");
		    }

		    String transport = android.text.TextUtils.join("|", types.toArray());
		    if (transport == null ||
			transport.equals("") ||
			transport.equals("bus|uline|service|tram|metro|train|ferry")) {
			transport = "all";
		    }

		    listener.onDone(optimize, transport);
		}

	    };

	builder.setPositiveButton(context.getResources().getString(R.string.ok), clickListener);

	boolean checkAll = transportTypes.equals("all");

	Button.OnClickListener toggleListener =
	    new Button.OnClickListener() {
		public void onClick(View v) {
		    ToggleButton t = (ToggleButton)v;
		    if (t.getId() == walk.getId()) {
			// User manipulated walking
			tram.setChecked(!walk.isChecked());
			train.setChecked(!walk.isChecked());
			metro.setChecked(!walk.isChecked());
			ferry.setChecked(!walk.isChecked());
			bus.setChecked(!walk.isChecked());
		    } else {
			if (!bus.isChecked() && !tram.isChecked() && !train.isChecked() &&
			    !metro.isChecked() && !ferry.isChecked()) {
			    walk.setChecked(true);
			} else if (walk.isChecked()) {
			    walk.setChecked(false);
			}
		    }
		}
	    };

	bus.setChecked(transportTypes.contains("bus|uline|service") || checkAll);
	bus.setOnClickListener(toggleListener);

	tram.setChecked(transportTypes.contains("tram") || checkAll);
	tram.setOnClickListener(toggleListener);

	metro.setChecked(transportTypes.contains("metro") || checkAll);
	metro.setOnClickListener(toggleListener);

	train.setChecked(transportTypes.contains("train") || checkAll);
	train.setOnClickListener(toggleListener);

	ferry.setChecked(transportTypes.contains("ferry") || checkAll);
	ferry.setOnClickListener(toggleListener);

	walk.setChecked(transportTypes.contains("walk"));
	walk.setOnClickListener(toggleListener);

	// Spinner
	spinner.setSelection(strings.indexOf(optimize));

	return builder.create();
    }
}
