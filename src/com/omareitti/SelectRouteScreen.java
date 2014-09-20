package com.omareitti;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.omareitti.R;
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

public class SelectRouteScreen extends Activity {

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
			
	        //Log.i("ddd", Integer.toString(screenWidth));
	        //Log.i("ddd", Integer.toString(iconWidth));
			
			Route route1 = routes.get(position);
			
			String hours = Integer.toString(route1.depTime.getHours());
			hours = hours.length() == 1 ? "0"+hours : hours;
			String mins = Integer.toString(route1.depTime.getMinutes());
			mins = mins.length() == 1 ? "0"+mins : mins;
			holder.text.setText(hours+":"+mins);
			
			if (route1.firstBusTime != null) {
				hours = Integer.toString(route1.firstBusTime.getHours());
				hours = hours.length() == 1 ? "0"+hours : hours;
				mins = Integer.toString(route1.firstBusTime.getMinutes());
				mins = mins.length() == 1 ? "0"+mins : mins;			
				holder.text3.setText(" ("+hours+":"+mins+")");			
			}
			
			//holder.text1.setText(route1.getStringDuration());
			holder.text1.setText(RouteInfoScreen.getStringDuration(route1.actual_duration, context));
			
			hours = Integer.toString(route1.arrTime.getHours());
			hours = hours.length() == 1 ? "0"+hours : hours;
			mins = Integer.toString(route1.arrTime.getMinutes());
			mins = mins.length() == 1 ? "0"+mins : mins;
			holder.text2.setText(hours+":"+mins);		
		
			int iconsFit = (int)Math.floor((double)screenWidth/iconWidth);
			iconsFit = iconsFit < route1.steps.size() ? iconsFit-1 : iconsFit; 
			int fitLeft = (int)Math.ceil((double)iconsFit/2);
			int fitRight = (int)Math.floor((double)iconsFit/2);
									
			for (int i = 0; i < route1.steps.size(); i++) {
				Route.RouteStep step = route1.steps.get(i);
				
				if (i+1 > fitLeft && i+1 < route1.steps.size()-fitRight+1) {
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
	
	private static final int ID_DIALOG_SEARCHING = 0;
	private static final int EARLIER_MENU_ID = 0;
	private static final int NOW_MENU_ID = 1;
	private static final int LATER_MENU_ID = 2;
	
	public ProgressDialog dialog;
	
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
	
	//public static String fromLocCoords = "";
	//public static String toLocCoords = "";
	
	//public static String fromLocFinal = "";
	//public static String toLocFinal = "";
	
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		handler = new Handler();
	}
/*
     protected Dialog onCreateDialog(int id) {   
    	if(id == ID_DIALOG_SEARCHING){   
	    	ProgressDialog loadingDialog = new ProgressDialog(this);   
	    	loadingDialog.setMessage("Searching. Please wait...");   
	    	loadingDialog.setIndeterminate(true);   
	    	loadingDialog.setCancelable(true);   
	    	
	    	return loadingDialog;   
    	}   
    	  
    	return super.onCreateDialog(id);   
     }   
*/
	public void doMakeUp() {
        TableRow row = (TableRow) findViewById(R.id.SelectRouteScreenRow1); 
        row.setBackgroundColor(Color.argb(178,255,255,255));
		        
        TextView from = (TextView) findViewById(R.id.SelectRouteScreenTextFrom);
        from.setText(fromName);
        
        TextView to = (TextView) findViewById(R.id.SelectRouteScreenTextTo);
        to.setText(toName);
        
        TableRow row2 = (TableRow) findViewById(R.id.SelectRouteScreenRow3); 
        row2.setBackgroundColor(Color.argb(178,255,255,255));
        
        TextView tt = (TextView) findViewById(R.id.SelectRouteScreenTextTimeType);
        if (timetype.equals("departure")) {
        	tt.setText(getString(R.string.srDeparture));
        } else {
        	tt.setText(getString(R.string.srArrival));
        }
        //tt.setText(timetype.substring(0, 1).toUpperCase() + timetype.substring(1).toLowerCase());
        
        TextView optim = (TextView) findViewById(R.id.SelectRouteScreenTextOptimization);
        String opt = getString(R.string.srOptimNormal);
        if (optimize.equals("default")) opt = getString(R.string.srOptimNormal);
        if (optimize.equals("fastest")) opt = getString(R.string.srOptimFastest);
        if (optimize.equals("least_transfers")) opt = getString(R.string.srOptimLeastTrans);
        if (optimize.equals("least_walking")) opt = getString(R.string.srOptimLeastWalk);
        optim.setText(opt);

        /*
    	Bitmap bity = Bitmap.createBitmap(75,25,Bitmap.Config.ARGB_8888);
    	Canvas canvas = new Canvas(bity);
    	BitmapDrawable bit = new BitmapDrawable(SelectRouteScreen.this.getResources().openRawResource(R.drawable.boat));
    	bit.setBounds(0, 0, 25, 25);
    	
    	Paint paint = new Paint();
    	canvas.drawBitmap(bit.getBitmap(), 0, 0, paint);
    	
    	//ImageView imgv = (ImageView) findViewById(R.id.imageView1);
    	
        TextView tw_icons = (TextView) findViewById(R.id.SelectRouteScreenTextIcons);
    	BitmapDrawable bit1 = new BitmapDrawable(SelectRouteScreen.this.getResources().openRawResource(R.drawable.metro));
    	bit1.setBounds(0, 0, 25, 25);
    	
    	canvas.drawBitmap(bit1.getBitmap(), 25, 0, paint);
    	
    	BitmapDrawable allb = new BitmapDrawable(SelectRouteScreen.this.getResources(), bity); 
    	
    	tw_icons.setCompoundDrawables(allb, null, null, null);
    	*/
    	//imgv.setImageBitmap(bity);
        

    	/*
        if (transport_types.contains("bus") || transport_types.contains("all")) {
        	Drawable dd = getResources().getDrawable(R.drawable.bus); 
        	dd.setBounds(0, 0, 25, 25);
        	tw_icons.setCompoundDrawables(dd, null, null, null);
        }
        if (transport_types.contains("tram") || transport_types.contains("all")) {
        	Drawable dd = getResources().getDrawable(R.drawable.tram); 
        	dd.setBounds(0, 0, 25, 25);
        	tw_icons.setCompoundDrawables(dd, null, null, null);
        }
        if (transport_types.contains("metro") || transport_types.contains("all")) {
        	Drawable dd = getResources().getDrawable(R.drawable.metro); 
        	dd.setBounds(0, 0, 25, 25);
        	tw_icons.setCompoundDrawables(dd, null, null, null);
        }
        if (transport_types.contains("train") || transport_types.contains("all")) {
        	Drawable dd = getResources().getDrawable(R.drawable.train); 
        	dd.setBounds(0, 0, 25, 25);
        	tw_icons.setCompoundDrawables(dd, null, null, null);
        }
        if (transport_types.contains("walk")) {
        	Drawable dd = getResources().getDrawable(R.drawable.man); 
        	dd.setBounds(0, 0, 25, 25);
        	tw_icons.setCompoundDrawables(dd, null, null, null);
        }
        */
		
	}

	private OnItemClickListener routeClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	    {
			//Log.i("CLICK", routes.get(position).jsonString);
		    SharedPreferences settings = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
		    SharedPreferences.Editor editor = settings.edit();
		    editor.putString("route", routes.get(position).jsonString);
		    editor.commit();

            Intent myIntent = new Intent(v.getContext(), RouteInfoScreen.class);
            myIntent.putExtra("from", fromName);
            myIntent.putExtra("to", toName);
            startActivity(myIntent);
	    }
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	   	
        super.onCreate(savedInstanceState);
        
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
        
        History.saveHistory(this, fromName, "", fromCoords);
        History.saveHistory(this, toName, "", toCoords);
        History.saveRoute(this, fromName, toName, fromCoords, toCoords);
		calcRoutes();
    }
	
	public void calcRoutes() {
//		showDialog(ID_DIALOG_SEARCHING);
		dialog = ProgressDialog.show(SelectRouteScreen.this, "", getString(R.string.srDlgSearching), true);
		
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
					routes = api.getRoute(fromCoords, toCoords, date, time, optimize, timetype, transport_types);						
				} catch ( Exception e ) {
					Log.e("ERROR", "No network", e);
				}
				handler.post(new Runnable() {						
	//				@Override
					public void run() {
						// TODO Auto-generated method stub
						if (routes != null) {
							l1.setAdapter(new EfficientAdapter(SelectRouteScreen.this, routes, screenWidth));
					    	doMakeUp();
						} else {
							//if (routes == null) {
							//dismissDialog(ID_DIALOG_SEARCHING);
							AlertDialog alertDialog = new AlertDialog.Builder(SelectRouteScreen.this).create();
						    alertDialog.setTitle(getString(R.string.networkErrorTitle));
						    alertDialog.setMessage(getString(R.string.networkErrorText));
						    alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						    	public void onClick(DialogInterface dialog, int which) {
						    		SelectRouteScreen.this.finish();
						    } }); 
						    alertDialog.show();	
							//}
						}
						//dismissDialog(ID_DIALOG_SEARCHING);
						dialog.dismiss();
					}
				});
			}
		}).start();
	}
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
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
    	
    	Drawable drRew  = getResources().getDrawable(android.R.drawable.ic_media_rew);	
    	Drawable drForw = getResources().getDrawable(android.R.drawable.ic_media_ff);	
    	Drawable drNow = getResources().getDrawable(android.R.drawable.ic_menu_set_as);	
    	
    	MenuItem tmp = menu.add(0, EARLIER_MENU_ID, 0, getString(R.string.srMenuEarlier));
    	tmp.setIcon(drRew);
    	tmp = menu.add(0, NOW_MENU_ID, 1, getString(R.string.srMenuNow));
    	tmp.setIcon(drNow);
    	tmp = menu.add(0, LATER_MENU_ID, 2, getString(R.string.srMenuLater));
    	tmp.setIcon(drForw);
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	Date dt = new Date();  
        switch (item.getItemId()) {
	        case EARLIER_MENU_ID:
	        	if (timetype.equals("departure")) {
	        		long diff = (routes.get(routes.size()-1).depTime).getTime() - (routes.get(0).depTime).getTime();
	        		Log.i("DEBUG", Long.toString(diff));
	        		dt = new Date((routes.get(0).depTime).getTime() - diff);
	        	} else {
	        		dt = routes.get(routes.size()-1).arrTime;
	        	}
	        	break;
	        case NOW_MENU_ID:
	        	dt = new Date();
	            break;
	        case LATER_MENU_ID:
	        	if (timetype.equals("departure")) {
	        		dt = routes.get(routes.size()-1).depTime;
	        	} else {
	        		// web interface in this case adds 15 mins (900000 msec)
	        		dt = new Date((routes.get(0).arrTime).getTime() + 900000);
	        	}
	        	break;
	        default:
	            return super.onOptionsItemSelected(item);	        
        }
        
    	date = ReittiopasAPI.formatDate(dt);
    	time = ReittiopasAPI.formatTime(dt);        
        
        calcRoutes();
        
        return true;
    }

}
