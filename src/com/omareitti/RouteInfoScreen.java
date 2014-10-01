package com.omareitti;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;

public class RouteInfoScreen extends Activity {
    private static final String TAG = MainApp.class.getSimpleName();

    private String fromLoc;
    private String toLoc;
    private Route route;
    private String routeString;
    private ListView l1;

    public static String getStringDuration(double duration, Context context) {
	String dur = "";
	Resources res = context.getResources();
	int durHours = (int) Math.floor(duration/3600);
	int durMins = (int) Math.ceil((duration-durHours*3600)/60);
	if (durHours == 0) {
	    dur = String.format(res.getString(R.string.minuteAbbr), durMins);
	} else {
	    dur = String.format(res.getString(R.string.hourAbbr), durHours)+" "+String.format(res.getString(R.string.minuteAbbr), durMins);
	}
	return dur;
    }

    private static class EfficientAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context context;
	private Route route;

	public EfficientAdapter(Context context, Route route) {
	    mInflater = LayoutInflater.from(context);
	    this.context = context;
	    this.route = route;
	}

	public int getCount() {
	    return route.mSteps.size();
	}

	public Object getItem(int position) {
	    return position;
	}

	public long getItemId(int position) {
	    return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
	    ViewHolder holder;
	    if (convertView == null) {
		convertView = mInflater.inflate(R.layout.routeinfolistitem, null);
		holder = new ViewHolder();
		holder.text = (TextView) convertView.findViewById(R.id.TextRouteDep);
		holder.text1 = (TextView) convertView.findViewById(R.id.TextRouteDur);
		holder.image = (ImageView) convertView.findViewById(R.id.RouteInfoIcon);
		holder.text2 = (TextView) convertView.findViewById(R.id.TextRouteLen);
		holder.text3 = (TextView) convertView.findViewById(R.id.TextRouteArr);
		holder.text4 = (TextView) convertView.findViewById(R.id.TextRouteBus);
		holder.text5 = (TextView) convertView.findViewById(R.id.TextRouteAddress);
		holder.row1 = (TableRow) convertView.findViewById(R.id.RouteInfoRow1);
		holder.row2 = (TableRow) convertView.findViewById(R.id.RouteInfoRow2);
		convertView.setTag(holder);
	    } else {
		holder = (ViewHolder) convertView.getTag();
	    }

	    Route.RouteStep step = route.mSteps.get(position);

	    String hours = Integer.toString(step.depTime.getHours());
	    hours = hours.length() == 1 ? "0"+hours : hours;
	    String mins = Integer.toString(step.depTime.getMinutes());
	    mins = mins.length() == 1 ? "0"+mins : mins;
	    holder.text.setText(hours+":"+mins);

	    holder.text1.setText(getStringDuration(step.duration, context));
	    holder.image.setImageResource(step.getIconId());

	    Resources res = context.getResources();
	    holder.text2.setText(String.format(res.getString(R.string.kmDouble), (step.length/100f)/10f));
	    hours = Integer.toString(step.arrTime.getHours());
	    hours = hours.length() == 1 ? "0"+hours : hours;
	    mins = Integer.toString(step.arrTime.getMinutes());
	    mins = mins.length() == 1 ? "0"+mins : mins;
	    holder.text3.setText(hours+":"+mins);

	    if (step.firstLoc != null && !step.firstLoc.equals("null")) holder.text5.setText(step.firstLoc);
	    else if (step.lastLoc != null && !step.lastLoc.equals("null")) holder.text5.setText(step.lastLoc);
	    else holder.text5.setText("");

	    holder.text4.setText(parent.getContext().getString(step.getTransportName())+" "+step.getBusNumber());

	    return convertView;
	}

	static class ViewHolder {
	    TextView text;
	    TextView text1;
	    ImageView image;
	    TextView text2;
	    TextView text3;
	    TextView text4;
	    TextView text5;
	    TableRow row1;
	    TableRow row2;
	}
    }

    private OnItemClickListener routeClickListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
		Intent myIntent = new Intent(v.getContext(), RouteMap.class);
		myIntent.putExtra("currentStep", position);
		myIntent.putExtra("route", routeString);
		myIntent.putExtra("from", fromLoc);
		myIntent.putExtra("to", toLoc);

		startActivity(myIntent);
	    }
	};

    public void doMakeUp() {
        TableRow row = (TableRow) findViewById(R.id.TopAddressRow);
        row.setBackgroundColor(Color.argb(178,255,255,255));

        TextView from = (TextView) findViewById(R.id.RouteInfoScreenTextFrom);
        from.setText(fromLoc);

        TextView to = (TextView) findViewById(R.id.RouteInfoScreenTextTo); 
        to.setText(toLoc);

        TableRow row1 = (TableRow) findViewById(R.id.TopInfoRow);
        row1.setBackgroundColor(Color.argb(178,255,255,255));

        TextView dur = (TextView) findViewById(R.id.RouteInfoScreenTextDuration);
        dur.setText(getStringDuration(route.mActualDuration, this));

        TextView dist = (TextView) findViewById(R.id.RouteInfoScreenTextDistance);
        dist.setText(String.format(getString(R.string.kmDouble), (route.mLength/100f)/10f));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	getActionBar().setDisplayHomeAsUpEnabled(true);

	setContentView(R.layout.routeinfoscreen);

	if (getIntent().getExtras() == null) {
            startActivity(new Intent(RouteInfoScreen.this, MainApp.class));
            return;
	}

        fromLoc = getIntent().getExtras().getString("from");
        toLoc = getIntent().getExtras().getString("to");
	routeString = getIntent().getExtras().getString("route");

	if (routeString != null && !routeString.equals("")) {
	    try {
		route = new Route(routeString, fromLoc, toLoc);
	    } catch (Exception e) {
		Log.e(TAG, "Failed to parse route " + e);
		startActivity(new Intent(RouteInfoScreen.this, MainApp.class));
		finish();
		return;
	    }
	}
	else {
	    startActivity(new Intent(RouteInfoScreen.this, MainApp.class));
	    finish();
	    return;
	}


        l1 = (ListView) findViewById(R.id.RouteInfoScreenListView);
        l1.setAdapter(new EfficientAdapter(RouteInfoScreen.this, route));
        l1.setOnItemClickListener(routeClickListener);
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
    }

    @Override
    protected void onResume() {
	super.onResume();
        doMakeUp();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        doMakeUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.routeinfo_actions, menu);

    	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case R.id.action_show_map:
            Intent myIntent = new Intent(RouteInfoScreen.this, RouteMap.class);
	    myIntent.putExtra("currentStep", -1);
	    myIntent.putExtra("route", routeString);
	    myIntent.putExtra("from", fromLoc);
	    myIntent.putExtra("to", toLoc);

	    startActivity(myIntent);

	    return true;

	case android.R.id.home:
            onBackPressed();
	    return true;

	default:
	    break;
	}

	return super.onOptionsItemSelected(item);
    }
}
