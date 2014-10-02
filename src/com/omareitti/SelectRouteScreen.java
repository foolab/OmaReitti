package com.omareitti;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.omareitti.RouteInfoScreen;
import com.omareitti.datatypes.GeoRec;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.omareitti.datatypes.Coords;
import android.view.MenuInflater;

public class SelectRouteScreen extends Activity {
    public ArrayList<Route> routes = null;
    public volatile Handler handler;

    public int screenWidth;
    public String fromCoords = "";
    public String toCoords = "";
    public String fromName = "";
    public String toName = "";
    public String date = "";
    public String time = "";
    public String optimize = "";
    public String timetype = "";
    public String transport_types = "";

    public ListView l1;

    private static class EfficientAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context context;
	private ArrayList<Route> routes;
	private int screenWidth;

	public EfficientAdapter(Context context, ArrayList<Route> routes, int screenWidth) {
	    mInflater = LayoutInflater.from(context);
	    this.context = context;
	    this.routes = routes;
	    this.screenWidth = screenWidth;
	}

	public int getCount() {
	    return routes.size();
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
		convertView = mInflater.inflate(R.layout.routelistitem, null);
		holder = new ViewHolder();
		holder.text = (TextView) convertView.findViewById(R.id.SelectRouteScreenTextTimeStart);
		holder.text1 = (TextView) convertView.findViewById(R.id.SelectRouteScreenTextDuration);
		holder.text2 = (TextView) convertView.findViewById(R.id.SelectRouteScreenTextTimeEnd);
		holder.text3 = (TextView) convertView.findViewById(R.id.SelectRouteScreenTextTimeFirstBus);
		holder.row1 = (TableRow) convertView.findViewById(R.id.SelectRouteScreenTableRowIcons);
		holder.row2 = (TableRow) convertView.findViewById(R.id.SelectRouteScreenTableRowDesc);

		convertView.setTag(holder);
	    } else {
		holder = (ViewHolder) convertView.getTag();
	    }

	    holder.row1.removeAllViewsInLayout();
	    holder.row2.removeAllViewsInLayout();

	    Resources res = context.getResources();
	    Drawable dr = res.getDrawable(R.drawable.man);
	    int iconWidth = dr.getIntrinsicWidth();

	    Route route1 = routes.get(position);

	    String hours = Integer.toString(route1.mDepTime.getHours());
	    hours = hours.length() == 1 ? "0"+hours : hours;
	    String mins = Integer.toString(route1.mDepTime.getMinutes());
	    mins = mins.length() == 1 ? "0"+mins : mins;
	    holder.text.setText(hours+":"+mins);

	    if (route1.mFirstBusTime != null) {
		hours = Integer.toString(route1.mFirstBusTime.getHours());
		hours = hours.length() == 1 ? "0"+hours : hours;
		mins = Integer.toString(route1.mFirstBusTime.getMinutes());
		mins = mins.length() == 1 ? "0"+mins : mins;
		holder.text3.setText(" ("+hours+":"+mins+")");
	    }

	    holder.text1.setText(RouteInfoScreen.getStringDuration(route1.mActualDuration, context));

	    hours = Integer.toString(route1.mArrTime.getHours());
	    hours = hours.length() == 1 ? "0"+hours : hours;
	    mins = Integer.toString(route1.mArrTime.getMinutes());
	    mins = mins.length() == 1 ? "0"+mins : mins;
	    holder.text2.setText(hours+":"+mins);

	    int iconsFit = (int)Math.floor((double)screenWidth/iconWidth);
	    iconsFit = iconsFit < route1.mSteps.size() ? iconsFit-1 : iconsFit;
	    int fitLeft = (int)Math.ceil((double)iconsFit/2);
	    int fitRight = (int)Math.floor((double)iconsFit/2);

	    for (int i = 0; i < route1.mSteps.size(); i++) {
		Route.RouteStep step = route1.mSteps.get(i);

		if (i+1 > fitLeft && i+1 < route1.mSteps.size()-fitRight+1) {
		    if (i+1 == fitLeft+1) {
			ImageView icon = new ImageView(context);
			icon.setImageResource(R.drawable.dots);
			holder.row1.addView(icon);

			TextView desc = new TextView(context);
			desc.setText("");
			desc.setGravity(Gravity.CENTER_HORIZONTAL);
			holder.row2.addView(desc);
		    }

		    continue;
		}

		ImageView icon = new ImageView(context);
		icon.setImageResource(step.getIconId());
		holder.row1.addView(icon);

		TextView desc = new TextView(context);
		desc.setText(step.getBusNumber());
		desc.setGravity(Gravity.CENTER_HORIZONTAL);
		holder.row2.addView(desc);
	    }

	    return convertView;
	}

	static class ViewHolder {
	    TextView text;
	    TextView text1;
	    TextView text2;
	    TextView text3;
	    TableRow row1;
	    TableRow row2;
	}
    }

    @Override
    protected void onStart() {
	// TODO Auto-generated method stub
	super.onStart();

	handler = new Handler();
    }

    public void doMakeUp() {
        TextView from = (TextView) findViewById(R.id.SelectRouteScreenTextFrom);
        from.setText(fromName);

        TextView to = (TextView) findViewById(R.id.SelectRouteScreenTextTo);
        to.setText(toName);

        TextView tt = (TextView) findViewById(R.id.SelectRouteScreenTextTimeType);
        if (timetype.equals("departure"))
	    tt.setText(getString(R.string.srDeparture));
	else
	    tt.setText(getString(R.string.srArrival));

        TextView optim = (TextView) findViewById(R.id.SelectRouteScreenTextOptimization);
        String opt = getString(R.string.srOptimNormal);

        if (optimize.equals("default"))
	    opt = getString(R.string.srOptimNormal);

        if (optimize.equals("fastest"))
	    opt = getString(R.string.srOptimFastest);

        if (optimize.equals("least_transfers"))
	    opt = getString(R.string.srOptimLeastTrans);

        if (optimize.equals("least_walking"))
	    opt = getString(R.string.srOptimLeastWalk);

        optim.setText(opt);
    }

    private OnItemClickListener routeClickListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Intent myIntent = new Intent(v.getContext(), RouteInfoScreen.class);
		myIntent.putExtra("from", fromName);
		myIntent.putExtra("to", toName);
		myIntent.putExtra("route", routes.get(position).jsonString);
		startActivity(myIntent);
	    }
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();

        fromCoords 		= b.getString("fromCoords");
        toCoords   		= b.getString("toCoords");
        fromName  		= b.getString("fromName");
        toName     		= b.getString("toName");
        date       		= b.getString("date");
        time       		= b.getString("time");
        optimize   		= b.getString("optimize");
        timetype   		= b.getString("timetype");
        transport_types = b.getString("transport_types");

        History.saveHistory(this, fromName, "", new Coords(fromCoords));
        History.saveHistory(this, toName, "", new Coords(toCoords));
        History.saveRoute(this, fromName, toName, new Coords(fromCoords), new Coords(toCoords));

	calcRoutes();
    }

    public void calcRoutes() {
	final ProgressDialog dialog =
	    ProgressDialog.show(SelectRouteScreen.this, "",
				getString(R.string.srDlgSearching), true);

        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();

        setContentView(R.layout.selectroutescreen);

        l1 = (ListView) findViewById(R.id.SelectRouteScreenListView);
        l1.setOnItemClickListener(routeClickListener);

        routes = null;

	new Thread(new Runnable() {
		public void run() {
		    try {
			ReittiopasAPI api = new ReittiopasAPI();
			routes =
			    api.getRoute(fromCoords, toCoords, date, time,
					 optimize, timetype, transport_types);
		    } catch ( Exception e ) {
			Log.e("ERROR", "No network", e);
		    }

		    handler.post(new Runnable() {
			    public void run() {
				// TODO Auto-generated method stub
				if (routes != null) {
				    l1.setAdapter(new EfficientAdapter(SelectRouteScreen.this, routes, screenWidth));
				    doMakeUp();
				} else {
				    AlertDialog alertDialog =
					new AlertDialog.Builder(SelectRouteScreen.this).create();
				    alertDialog.setTitle(getString(R.string.networkErrorTitle));
				    alertDialog.setMessage(getString(R.string.networkErrorText));
				    alertDialog.setButton(getString(R.string.ok),
							  new DialogInterface.OnClickListener() {
							      public void onClick(DialogInterface dialog, int which) {
						SelectRouteScreen.this.finish();
							      } });
				    alertDialog.show();
				}

				dialog.dismiss();
			    }
			});
		}
	    }).start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

	if (routes != null) {
	    setContentView(R.layout.selectroutescreen);
	    doMakeUp();

	    Display display = getWindowManager().getDefaultDisplay();
	    screenWidth = display.getWidth();

	    l1 = (ListView) findViewById(R.id.SelectRouteScreenListView);
	    l1.setOnItemClickListener(routeClickListener);
	    l1.setAdapter(new EfficientAdapter(SelectRouteScreen.this, routes, screenWidth));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.selectroute_actions, menu);

    	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Date dt = new Date();

	switch (item.getItemId()) {
	case R.id.action_prev:
	    if (timetype.equals("departure")) {
		long diff = (routes.get(routes.size() - 1).mDepTime).getTime() -
		    (routes.get(0).mDepTime).getTime();
		dt = new Date((routes.get(0).mDepTime).getTime() - diff);
	    } else {
		dt = routes.get(routes.size()-1).mArrTime;
	    }
	    break;

	case R.id.action_now:
	    // Nothing
	    break;

	case R.id.action_next:
	    if (timetype.equals("departure")) {
		dt = routes.get(routes.size()-1).mDepTime;
	    } else {
		// web interface in this case adds 15 mins (900000 msec)
		dt = new Date((routes.get(0).mArrTime).getTime() + 900000);
	    }
	    break;

	case android.R.id.home:
            onBackPressed();
	    return true;

	default:
	    return super.onOptionsItemSelected(item);
	}

    	date = ReittiopasAPI.formatDate(dt);
    	time = ReittiopasAPI.formatTime(dt);

        calcRoutes();

	return true;
    }
}
