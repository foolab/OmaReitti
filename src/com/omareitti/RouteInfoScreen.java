package com.omareitti;

import java.util.ArrayList;

import com.omareitti.IBackgroundServiceAPI;
import com.omareitti.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RouteInfoScreen extends Activity {
	
	private static final String TAG = MainApp.class.getSimpleName();
	public String fromLoc = "";
	public String toLoc = "";
	
	public Route route = null;
	private String routeString;
	
	public ListView l1;
	
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
				return route.steps.size();
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
				
				Route.RouteStep step = route.steps.get(position);

				String hours = Integer.toString(step.depTime.getHours());
				hours = hours.length() == 1 ? "0"+hours : hours;
				String mins = Integer.toString(step.depTime.getMinutes());
				mins = mins.length() == 1 ? "0"+mins : mins;
				holder.text.setText(hours+":"+mins);
				
				//holder.text1.setText(step.getStringDuration());
				holder.text1.setText(getStringDuration(step.duration, context));				
				holder.image.setImageResource(step.getIconId());
				//holder.text2.setText(Double.toString(Math.round(step.length/100)/10)+" km");
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
				/*if (step.getBusNumber().equals("")) {
					holder.text4.setVisibility(View.GONE);
					holder.row1.setPadding(0, 3, 0, 3);
					holder.row2.setVisibility(View.GONE);
				} else {
					holder.text4.setText(step.getTransportName()+" "step.getBusNumber());
				}*/				
			
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
			if (!isRouteSet)
				Toast.makeText(RouteInfoScreen.this, getString(R.string.riToastNotSelected), Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(v.getContext(), MapScreen.class);
            myIntent.putExtra("currentStep", position);
            myIntent.putExtra("isJustLooking", true);
            
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
        //dur.setText(route.getStringDuration());
        dur.setText(getStringDuration(route.actual_duration, this));
        
        TextView dist = (TextView) findViewById(R.id.RouteInfoScreenTextDistance); 
        dist.setText(String.format(getString(R.string.kmDouble), (route.length/100f)/10f));
        //Double.toString(Math.round(route.length/100)/10)+" km");
    }
    
    public void getRoute() {
        SharedPreferences settings = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        try {
        	routeString = settings.getString("route", "");
        	routeStarted = settings.getBoolean("routeStarted", false);
        	isRouteSet = settings.getBoolean("isRouteSet", false);
        
        	if (routeStarted) isRouteSet = routeStarted;
        	if (!routeString.equals("")) {
        		route = new Route(routeString);
        	}
        	else {
        		startActivity(new Intent(RouteInfoScreen.this, MainApp.class));
        		finish();
                return;        		
        	}
        	cbDeparture.setChecked(settings.getBoolean("needRemindDep", true));
        	cbArrival.setChecked(settings.getBoolean("needRemindArr", true));
        } catch ( Exception e ) {
        	Log.e("ERROR", "Couldn't get the route from JSONobj");
        }        
    }
    
    CheckBox cbDeparture, cbArrival;
    
    private String lastAction = "";
    private Boolean routeStarted = false;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.routeinfoscreen);
		
		cbDeparture = (CheckBox) findViewById(R.id.checkBoxDeparture);
		cbArrival = (CheckBox) findViewById(R.id.checkBoxArrival);
		
		if (getIntent().getExtras() == null) {
            startActivity(new Intent(RouteInfoScreen.this, MainApp.class));
            return;
		}
		
        fromLoc = getIntent().getExtras().getString("from");
        toLoc = getIntent().getExtras().getString("to");  
        
        Button showMap = (Button) findViewById(R.id.RouteInfoScreenShowMap);
        showMap.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				lastAction = "shop_map";
				if (!setRoute()) return;
	            /*Intent myIntent = new Intent(v.getContext(), MapScreen.class);
	            myIntent.putExtra("currentStep", 0);
	            startActivity(myIntent);			*/
				executeLastAction();
	        }
		});
        
        isRouteSet = false;
        
        getRoute();
        
        l1 = (ListView) findViewById(R.id.RouteInfoScreenListView);        
        l1.setAdapter(new EfficientAdapter(RouteInfoScreen.this, route));
        l1.setOnItemClickListener(routeClickListener);
		
        Button b = (Button) findViewById(R.id.SelectRouteButton);
        b.setOnClickListener(selectButtonListener);
		Log.i(TAG, "trying to bind service "+BackgroundService.class.getName());
		Intent servIntent = new Intent(BackgroundService.class.getName());//this, BackgroundService.class);
        startService(servIntent);
        Log.i(TAG, "starting service "+servIntent.toString());
        bindService(servIntent, servceConection, 0);
        
        if (isRouteSet) setButtonToCancel();
	}
	
	private View.OnClickListener selectButtonListener = new View.OnClickListener() {

		public void onClick(View v) {
			lastAction = "exit";
			if (!setRoute()) return;
	        /*Intent setIntent = new Intent(Intent.ACTION_MAIN); 
	        setIntent.addCategory(Intent.CATEGORY_HOME); 
	        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        startActivity(setIntent); */
			executeLastAction();
		}
	};
	
	private View.OnClickListener cancelButtonListener = new View.OnClickListener() {

		public void onClick(View v) {
			cancelRoute();
		}
	};
	
	private void cancelRoute() {
		isRouteSet = false;
		Button b = (Button) findViewById(R.id.SelectRouteButton);
		b.setText(getString(R.string.riButtonSelect));
        b.setOnClickListener(selectButtonListener);
        cbDeparture.setVisibility(View.VISIBLE);
        cbArrival.setVisibility(View.VISIBLE);        
		try {
			if (api != null) api.cancelRoute(1);
			((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(BackgroundService.NOTIFICATION_ID);
		} catch(Exception e) {
			Log.e(TAG, "ERROR!!", e);
		}		
	}
	
	@Override
	public void onBackPressed() {
		cancelRoute();
		super.onBackPressed();
	}

	private boolean isRouteSet = false;
	
	private boolean setRoute() {
		try {
			if (api != null) {
				api.setRoute(routeString, cbDeparture.isChecked(), cbArrival.isChecked());
				SharedPreferences settings = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("route", routeString);
				editor.putBoolean("needRemindDep", cbDeparture.isChecked());
				editor.putBoolean("needRemindArr", cbArrival.isChecked());
				editor.putBoolean("isRouteSet", true);
				editor.commit();
				
				isGPSon = api.isGPSOn();
				if (cbDeparture.isChecked() && !isGPSon) {
					showEnableGPSDialog();
					return false;
				}
				//Log.i(TAG, "Route set, routeString:"+);
			}
		} catch(Exception e) {
			Log.e(TAG, "ERROR!!", e);
		}
		return true;
	}
	
	private IBackgroundServiceAPI api = null;
	private boolean isGPSon = false;
	
	private ServiceConnection servceConection = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "Service disconnected!");
			api = null;
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			api = IBackgroundServiceAPI.Stub.asInterface(service);
			Log.i(TAG, "Service connected! "+api.toString());
		}
	};	

	public void showEnableGPSDialog() {
		AlertDialog alertDialog = new AlertDialog.Builder(RouteInfoScreen.this).create();
	    alertDialog.setTitle(getString(R.string.riDlgNoGPSTitle));
	    alertDialog.setMessage(getString(R.string.riDlgNoGPSText));
	    
	    alertDialog.setButton(getString(R.string.riDlgNoGPSButton), new DialogInterface.OnClickListener() {  
	    	public void onClick(DialogInterface dialog, int which) {
	    		startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
	    } });
	    
	    alertDialog.setButton2(getString(R.string.cancel), new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
	    		executeLastAction();
	    		dialog.dismiss();
	    } });			    
	    alertDialog.show();			
	}

	public void setButtonToCancel() {
		isRouteSet = true;
		Button b = (Button) findViewById(R.id.SelectRouteButton);
		b.setText(getString(R.string.riButtonCancelRoute));
        b.setOnClickListener(cancelButtonListener);
        cbDeparture.setVisibility(View.GONE);
        cbArrival.setVisibility(View.GONE);
	}
	
	public void executeLastAction() {
		setButtonToCancel();		
		if ( lastAction.equals("exit") ) {
	        Intent setIntent = new Intent(Intent.ACTION_MAIN); 
	        setIntent.addCategory(Intent.CATEGORY_HOME); 
	        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        startActivity(setIntent); 
		} else {
            Intent myIntent = new Intent(RouteInfoScreen.this, MapScreen.class);
            myIntent.putExtra("currentStep", 0);
            startActivity(myIntent);		    			
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		executeLastAction();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			unbindService(servceConection);
			Log.i(TAG, "unbind ");		
		} catch(Exception e) {
			Log.e(TAG, "ERROR!!", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
        doMakeUp();
        
        getRoute();
        Log.i(TAG, "onResume:"+isRouteSet);
        if (isRouteSet) setButtonToCancel();
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);	    
	    
        doMakeUp();
        
        getRoute();
        
        Log.i(TAG, "onConfigurationChanged:"+isRouteSet);
        if (isRouteSet) setButtonToCancel();
    }	
}
