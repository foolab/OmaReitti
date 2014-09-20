package com.omareitti;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.omareitti.IBackgroundServiceAPI;
import com.omareitti.IBackgroundServiceListener;
import com.omareitti.R;
import com.omareitti.Route;
import com.omareitti.Route.PathSegment;
import com.omareitti.Route.RouteStep;
import com.omareitti.datatypes.Coords;
import com.omareitti.datatypes.GeoRec;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BackgroundService extends Service implements LocationListener {

	private Boolean discoverAddress = false;
	
	private String lastKnownAddress = "";
	
	private ReittiopasAPI api;

	private int currentState = 0;
	public static int STATE_DO_NOTHING	= 0;
	public static int STATE_START_ROUTE	= 1;
	public static int STATE_IM_INBUS	= 2;
	public static int STATE_LAST_STOP	= 3;
	public static int STATE_LAZY_MODE	= 4;
	
	public boolean isGPSProviderOn		= false;
	public boolean isNetworkProviderOn	= false;
	public boolean isWIFIProviderOn		= false;
	
	public int minTimeGPS = 2 * 1000;
	public int minTimeNetwork = 1 * 1000;
	
	public long timerInverval = 1000L;
	
	public Route route;
	
	public int reminderTime; // = 5 * 60 * 1000;
	
	Calendar currentTime;
	Calendar depTime;
	Calendar arrTime;
	
	private boolean needRemindDep = false;
	private boolean needRemindArr = false;
	private boolean hasRemindedDep = false;
	//private boolean hasRemindedArr = false;
	
	private boolean isGPSOn = false;
	
	private boolean isRouteSet = false;
	private boolean isNotRouteInSettings = false;
	
	private String prevRouteString = "";
	
	SharedPreferences prefs;
	private boolean allowCoords;
	
	public void getRouteFromSettings() {
		Log.i(TAG, "SERVICE getRouteFromSettings");
        SharedPreferences settings = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
        try {
        	String routeString = settings.getString("route", "");

        	if (!routeString.equals("")) route = new Route(routeString);
        	
        	needRemindDep = settings.getBoolean("needRemindDep", true);
        	needRemindArr = settings.getBoolean("needRemindArr", true);
        	prevRouteString = routeString;
        	isNotRouteInSettings = false;
        	return;
        } catch ( Exception e ) {
        }
        isNotRouteInSettings = true;
	}
	
	public void deleteRoute() {
		Log.i(TAG, "SERVICE deleteRoute");
		SharedPreferences settings = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove("route");
		editor.remove("routeStarted");
		editor.remove("isRouteSet");
		editor.commit();
		locationManager.removeUpdates(this);
		currentState = STATE_DO_NOTHING;
		route = null;
		prevRouteString = "";
	}
	
	public void startRoute() {
		Log.i(TAG, "SERVICE deleteRoute");
		allowCoords = prefs.getString("prefAllowCoords", "dgdsfg").equals("Yes") ? true : false;
		Log.i("DEBUG!!", prefs.getString("prefAllowCoords", "Yes"));
		SharedPreferences settings = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("routeStarted", true);
		editor.commit();
	}	
	
	private Boolean isSameRoute = false;
	/** The actual API */
	public void setServiceRoute(String r) {
		Log.i("FROM SERVICE", r);
		try {
			Log.i(TAG, "is not new? "+prevRouteString.equals(r));
        	if (route != null && prevRouteString.equals(r)) {
        		// This is the same route
        		isSameRoute = true;
        	} else {
        		hasRemindedDep = false;
        		isSameRoute = false;
        		this.route = new Route(r);
        	}
        	
        	prevRouteString = r;
			
			depTime = Calendar.getInstance();
			arrTime = Calendar.getInstance();
			//Date depDate = new Date();
			//depDate.setTime(depDate.getTime() - reminderTime);
			depTime.setTimeInMillis(route.depTime.getTime() - reminderTime);
			arrTime.setTime(route.arrTime);
			
			Log.i(TAG, "DATES: "+arrTime+" "+depTime+" "+route.depTime);
			/*if (needRemindArr) { //TODO DOESN"T WORK ANYWAY
				isGPSOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
				showDialog("GPS Not Enabled", "Please enable GPS in if you want to be reminded before arrival", "gps_settings");
			}*/
			isRouteSet = true;
			handle.post(new Runnable() {
				public void run() {
					changeState(STATE_START_ROUTE);
				}
			});
			
		} catch (Exception e) {
			Log.e(TAG, "setServiceRoute", e);
		}
	}	
	
	public Location getServiceLastKnownLocation() {
		Location l1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location l2 = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location l3 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		if (l1 == null && l2 == null && l3 != null) return l3;
		
		Log.i(TAG, "getServiceLastKnownLocation:\n"+String.valueOf(l1)+"\n"+String.valueOf(l2)+"\n"+String.valueOf(l2)+"\n");
		
		if ((l1 != null || l2 != null )) {
			if (isBetterLocation(l1, l2)) return l1;
		}
		return l2;
	}

	public void locationDiscovered(Location l) {
		if (l == null) return;
		synchronized (listeners) {
			for (IBackgroundServiceListener listener : listeners) {
				try {
					listener.locationDiscovered(l.getLatitude(), l.getLongitude());
				} catch (RemoteException e) {
					Log.e(TAG, "in locationDiscovered", e);
					Log.e(TAG, "listener is "+listener);
					//try { listeners.remove(listener); } catch (Exception e2) {};
				}
			}
		}		
	}
	
	public void addressDiscovered(String s) {
		synchronized (listeners) {
			for (IBackgroundServiceListener listener : listeners) {
				try {
					listener.addressDiscovered(s);
				} catch (RemoteException e) {
					Log.e(TAG, "in getting address", e);
					Log.e(TAG, "listener is "+listener);
					//try { listeners.remove(listener); } catch (Exception e2) {};
				}
			}
		}		
	}
	
	public void getAddressFromReittiopas(final Location l) {
		if (l == null) return;
		Log.i(TAG, "getAddressFromReittiopas "+l);
		new Thread(new Runnable() {
			public void run() {
				Coords coords = new Coords(l.getLatitude(), l.getLongitude());
				Log.i(TAG, "getAddressFromReittiopas: sending http request "+coords.toString());
				ArrayList<GeoRec> recs = api.getReverseGeocode(coords.toString());
				if (recs.size() > 0) {
					GeoRec rec = recs.get(0);
					
					//Log.i(TAG, "GOT GEOREC!!: "+rec.name);
					lastKnownAddress = rec.name;
					addressDiscovered(rec.name);
				}
			}
		}).run();		
	}
	
	/** GPS stuff */
	private LocationManager locationManager;
	
	double lat, lng;
	
	float angle = 0;
//	@Override
	public void onLocationChanged(Location loc) {
		// TODO Auto-generated method stub
		//! if (isBetterLocation(loc, currentBestLocation))
		previousLocation = currentLocation;
		currentLocation = loc;
		if (currentLocation.getProvider() == LocationManager.GPS_PROVIDER) {
			angle = currentLocation.getBearing();
		}/*
		if (angle == 0 && previousLocation != null) {
			angle = previousLocation.bearingTo(currentLocation);
		}	
		if (currentState != STATE_IM_INBUS) angle = 0;*/
		//}
		//Log.i(TAG, "CURRENT STATE:"+currentState);
		
    	lat = (int) (currentLocation.getLatitude()*1E6);
    	lng = (int) (currentLocation.getLongitude()*1E6);	
    	Log.i(TAG, "LOCATION!!!!! "+currentLocation.getLatitude()+" "+currentLocation.getLongitude());
		synchronized (listeners) {
			for (IBackgroundServiceListener listener : listeners) {
				try {
					//if (listener != null) { listeners.remove(listener); continue; }

					listener.handleGPSUpdate(currentLocation.getLatitude(), currentLocation.getLongitude(), angle);
				} catch (RemoteException e) {
					//Log.e(TAG, "in getting coords", e);
					Log.e(TAG, "listener is "+listener);
					//try { listeners.remove(listener); } catch (Exception e2) {};
				}
			}
		}
		if (discoverAddress) {
			getAddressFromReittiopas(loc);
		}
	}
	
	private Location currentLocation = null;
	private Location previousLocation = null;
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	/** Taken from http://developer.android.com/guide/topics/location/obtaining-user-location.html 
	  * Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    } else if (location == null) return false;

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	public void onProviderDisabled(String pr) {
		Log.v(TAG, "ProviderEnabled: "+pr);
	}

	public void onProviderEnabled(String pr) {
		Log.v(TAG, "ProviderEnabled: "+pr);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		
		switch (status) {
			case LocationProvider.OUT_OF_SERVICE:
				Log.v(TAG, "Status Changed: Out of Service");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.v(TAG, "Status Changed: Temporarily Unavailable");
				break;
			case LocationProvider.AVAILABLE:
				Log.v(TAG, "Status Changed: Available");
				break;
		}
	}
	
	
	/** The exposed API implementation */
	private List<IBackgroundServiceListener> listeners = new ArrayList<IBackgroundServiceListener>();

	private IBackgroundServiceAPI.Stub apiEndpoint = new IBackgroundServiceAPI.Stub() {
		
		public void setRoute(String route, boolean remindDep, boolean remindArr) {
			Log.i(TAG, "setRoute: "+remindDep+" "+remindArr+"\n"+route);
			needRemindDep = remindDep;
			needRemindArr = remindArr;
			setServiceRoute(route);
		} 
		
		public void cancelRoute(int notify) {
			deleteRoute();
			cancelNotification(notify > 0);
		}
		
		public void addListener(IBackgroundServiceListener l) {
			synchronized (listeners) {
			      listeners.add(l);
			}
		}
		
		public void removeListener(IBackgroundServiceListener l) {
			synchronized (listeners) {
			      listeners.remove(l);
			}
		}

		public int requestLastKnownAddress(int getAddress) {
			Log.i(TAG, "SERVICE requestLastKnownAddress");
			Location l1 = getServiceLastKnownLocation();
			Log.i(TAG, "requestLastKnownAddress:\n"+String.valueOf(l1));
			previousLocation = currentLocation;
			currentLocation = l1;
			if (l1 == null) {
				addressDiscovered("");
				return 0;
			}
			
			locationDiscovered(l1);
			if (getAddress > 0)
				getAddressFromReittiopas(l1);
			return 1;
		}
		
		public boolean isGPSOn() {
			boolean gpson = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			Log.i(TAG,"GPS status: "+gpson);
			return gpson;
		}
	};
	
	/** The rest of the stuff */
	private static final String TAG = BackgroundService.class.getSimpleName();
	private Timer timer;

	private void changeState(int state) {
			currentState = state;
			if (state == STATE_START_ROUTE) {
			   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeGPS,  0, this);	
			   locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeNetwork,  0, this);		
			   currentStep = 0;
			   if (!isSameRoute) {
				   createNotification(getString(R.string.bsNotifRouteStartTicker), getString(R.string.appName), getString(R.string.bsNotifRouteStartText), false, false, Toast.LENGTH_LONG);
				   isRouteStartedShown = false;
			   }
			} else if (state == STATE_IM_INBUS) {
			   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,  0, this);	
			   locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,  0,this);
			   
			} else if (state == STATE_LAST_STOP) {
			   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeGPS,  0, this);	
			   locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeNetwork,  0, this);
			   //hasRemindedArr = false;
			} else if (state == STATE_LAZY_MODE) {
			   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60*60,  0, this);	
			   locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60*60,  0, this);
			   //hasRemindedArr = false;
			} else if (state == STATE_DO_NOTHING) {
				// done in deleteRoute, locationManager.removeUpdates(this);	
				deleteRoute();
				createNotification(getString(R.string.bsNotifRouteFinishTicker), getString(R.string.appName), getString(R.string.bsNotifRouteFinishText), false, false, Toast.LENGTH_LONG);
				cancelNotification();
			} 
	}
	
	/*private void showDialog(String caption, String text) { // LEAVE THIS OFF FOR BETTER TIMES
		showDialog(caption, text, "");
	}
	private void showDialog(String caption, String text, String action) {
        Intent myIntent = new Intent(BackgroundService.this, ServiceDialog.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        myIntent.putExtra("caption", caption);
        myIntent.putExtra("text", text);
        myIntent.putExtra("action", action);
        startActivity(myIntent);			
	}*/

	private int currentStep = 0;
	float minDist = 9999999;
	int minDistIdx = -1;
	
	float prevMinDist, prevMinDistIdx;
	
	private Boolean isRouteStartedShown = false;
	private String busName = "";
	private void processRoute() {
		if (route == null || currentLocation == null) {
			if (route == null && !isRouteSet && !isNotRouteInSettings) {
				getRouteFromSettings();
			}
			Log.i(TAG, "processRoute error: "+String.valueOf(route)+" "+String.valueOf(currentLocation));
			return;
		}
		prevMinDist = minDist;
		prevMinDistIdx = minDistIdx;
		
		minDist = 9999999;
		minDistIdx = -1;
		
		int lastCurrentStep = currentStep;
		//Log.i(TAG, "lastCurrentStep: "+lastCurrentStep+" "+currentStep);
		PathSegment lastP = null;
		//RouteStep lastRouteStep = null;
		String lastName = "";
		
		for (int j=0; j<route.steps.size(); j++) {
			RouteStep s = route.steps.get(j);
			
			currentTime = Calendar.getInstance();
			long now =  currentTime.getTimeInMillis();
			
			for (int i=0; i < s.path.size(); i++) {
				PathSegment p = s.path.get(i);
				
				Location dest = new Location("");
				dest.setLatitude(p.coords.x);
				dest.setLongitude(p.coords.y);
				float dist = currentLocation.distanceTo(dest); // Approximate distance in meters
				
				if (dist < minDist) {
					minDist = dist;
					minDistIdx = i;
					currentStep = j;
					lastP = p;
					busName = s.getBusNumber();
					//lastRouteStep = s;
				}
				
				if (s.getTransportName() == R.string.tr_metro
						|| s.getTransportName() == R.string.tr_train
						&& i == (s.path.size() - 1) && i > 0 
						&& !s.hasRemindedArr) {
					
					long time1 = s.path.get(i - 1).depTime.getTime();
					long time2 = s.path.get(i).arrTime.getTime();
					if (now > time1 + (time2 - time1)/2 && now < time2 ) {
						//showDialog(getString(R.string.appName), getString(R.string.bsDialogNextStopTimetable)); 
						createNotification(String.format(getString(R.string.bsNotifExitTicker), getString( s.getTransportName() )), 
								getString(R.string.appName), 
								getString(R.string.bsDialogNextStopTimetable), true, true, Toast.LENGTH_LONG);					
						s.hasRemindedArr = true;
					}
				}
			}
			if (needRemindArr && minDistIdx == s.path.size() - 1 && prevMinDist < minDist && !s.hasRemindedArr) {
				//! This is the next to last stop and probably were getting away from it
				if (s.getTransportName() != R.string.tr_walk 			// currentState == STATE_IM_INBUS 
						&& s.getTransportName() != R.string.tr_metro
						&& s.getTransportName() != R.string.tr_train) {
					
					//showDialog(getString(R.string.appName), getString(R.string.bsDialogNextStop)); 
					createNotification(String.format(getString(R.string.bsNotifExitTicker), getString( s.getTransportName() )), 
							getString(R.string.appName), 
							getString(R.string.bsNotifExitText), true, true, Toast.LENGTH_LONG);
					s.hasRemindedArr = true;
				}
			}
		}

		if (lastP != null) {
			//Log.i(TAG, "current route step: "+lastP.name+" "+lastP.coords.toString()+" "+lastP.depTime+" "+lastP.arrTime);
			String name = lastP.name;

			if (minDistIdx != prevMinDistIdx && lastP.name != null && !lastP.name.equals("null")) {
				Log.i(TAG, "current route step: "+lastP.name+" "+lastP.coords.toString()+" "+lastP.depTime+" "+lastP.arrTime);
				Log.i(TAG, "createNotification: "+lastCurrentStep+" "+currentStep);
				createNotification(getString(R.string.bsNotifPositionTicker), 
						getString(R.string.appName), 
							String.format(getString(R.string.bsNotifPositionText), lastP.name), false, false, Toast.LENGTH_SHORT);
			}
		}		
		//currentStep = lastCurrentStep;
		if ((minDistIdx == prevMinDistIdx && prevMinDist < minDist) // Probably were leaving the last route leg
				|| 
				(minDistIdx < prevMinDistIdx)) // Probably we are moving to the next step
		{  
			Log.i(TAG, "Probably we are leaving the last route leg: "+minDistIdx+" "+minDist);
			nextRouteStep();
			
		}
	}
	
	private void nextRouteStep() {
		/*if (currentStep + 1 < route.steps.size()) currentStep++;
		else routeDone();*/
		changeState(STATE_LAST_STOP);
		if (currentStep + 1 < route.steps.size()) {}
		else routeDone();
	}
	
	private void routeDone() {
		//showDialog(getString(R.string.appName), getString(R.string.bsDialogRouteDone));
		createNotification(getString(R.string.bsDialogRouteDone), 
				getString(R.string.appName), 
				getString(R.string.bsDialogRouteDone), false, false, Toast.LENGTH_LONG);
		
		changeState(STATE_LAZY_MODE);
	}
	
	public float BUS_SPEED = 4.5f; // m/s
	
	public float prevSpeed = 0;
	
	public long counter = 0; 
	private TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			//Log.i(TAG, "Timer task doing work: "+String.valueOf(route)+"\n"+String.valueOf(currentBestLocation)+
			//		((currentBestLocation != null) ? currentBestLocation.getSpeed() : "")+" state:"+currentState );
			if (route != null && currentLocation != null) {
				/*Location l1 = getServiceLastKnownLocation(); // I'm not sute if we need this, but let it be for the time being
				if (isBetterLocation(l1, currentLocation)) { previousLocation = currentLocation; currentLocation = l1; }
				
				float speed = currentLocation.getSpeed();
				if (speed == 0 && previousLocation != null) { // DOESN"T WORK AT ALL
					float dist = currentLocation.distanceTo(previousLocation);
					float timediff = currentLocation.getTime() - previousLocation.getTime();
					speed = dist / timediff;
					Log.i(TAG, "Calculated speed:"+speed+" dist:"+dist+" time:"+timediff);
				}*/
				float speed = currentLocation.getSpeed();
				
				if (currentLocation != null) { //  && currentLocation.getProvider() == LocationManager.GPS_PROVIDER
					if (prevSpeed > 0) {
						if (speed >= BUS_SPEED && prevSpeed < BUS_SPEED && currentState != STATE_IM_INBUS) {
							changeState(STATE_IM_INBUS);
							//showDialog("MESSAGE FROM SERVICE", "Bus speed is:"+ currentBestLocation.getSpeed());
							//Remove this later
							/*createNotification(getString(R.string.bsNotifPositionTicker), getString(R.string.appName), 
									"Bus speed is:"+ currentBestLocation.getSpeed(), false, false);*/
						} else {
							// if (this is last leg) changeState(STATE_LAZY_MODE);
							//showDialog("MESSAGE FROM SERVICE", "Bus speed is:"+ currentBestLocation.getSpeed());
						}
					} 
					prevSpeed = speed;//currentLocation.getSpeed();
				}
				if (currentState != STATE_DO_NOTHING && route != null) {
					currentTime = Calendar.getInstance();
					if (needRemindDep) {
						if (currentTime.compareTo(depTime) > 0) {
							long diff = route.depTime.getTime() - currentTime.getTimeInMillis(); // depTime.getTimeInMillis();
							if (!hasRemindedDep) {
								String str = String.format(getString(R.string.bsNotifDepartureText1), Math.round(diff/(1000*60)));
								//String str = getString(R.string.bsNotifDepartureText1)+" "+Math.round(diff/(1000*60))+" "+getString(R.string.bsNotifDepartureText2);
								if (diff <= 0) str = String.format(getString(R.string.bsNotifDepartureText2), Math.round(-diff/(1000*60)));
								//if (diff <= 0) str = getString(R.string.bsNotifDepartureText3)+" "+Math.round(-diff/(1000*60))+" "+getString(R.string.bsNotifDepartureText4);
								//showDialog(getString(R.string.appName), str); //Math.round(diff/(1000*60))+" minutes");
								createNotification(getString(R.string.bsNotifDepartureTicker), getString(R.string.appName), str, true, (diff > reminderTime - 1500), Toast.LENGTH_LONG);
								hasRemindedDep = true;
							}
							if (diff < 0 && !isRouteStartedShown) {
								createNotification(getString(R.string.bsNotifRouteStartTicker), getString(R.string.appName), getString(R.string.bsNotifRouteStartText), false, false, Toast.LENGTH_SHORT);
								startRoute();
								isRouteStartedShown = true;
							}
						} 
					}
					if (currentTime.getTimeInMillis() -  arrTime.getTimeInMillis() > reminderTime) {
						//showDialog("MESSAGE FROM SERVICE", "You arrived at "+arrTime);
						changeState(STATE_DO_NOTHING);
					}
					
					processRoute();
				}
				Log.i("DEBUG!", ""+allowCoords);
				if (counter % SEND_DATA_RATE == 0 && allowCoords) sendDataToServer();
			}
			
			/*synchronized (listeners) {
				for (IBackgroundServiceListener listener : listeners) {
					try {
						listener.handleUpdate("");
					} catch (RemoteException e) {
					}
				}
			}*/

			counter++;
		}
	};

	public static int SEND_DATA_RATE = 5;
	
	public void sendDataToServer() {
		if (currentLocation == null) return;
			api.sendCoords(userId, currentLocation.getLatitude()+","+currentLocation.getLongitude()+","+currentLocation.getBearing(), busName);
	}
	
	public BackgroundService() {
		super();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return apiEndpoint;
	}

	private Handler handle;
	private String userId;
	
	@Override
	public void onCreate() {
		super.onCreate();

		timer = new Timer(getString(R.string.bsTimer));
		timer.schedule(updateTask, 0, timerInverval);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		reminderTime = Integer.parseInt(prefs.getString("prefDepNotifInterval", "5")) * 60 * 1000;
		
		// get a hangle on the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		currentState = STATE_DO_NOTHING;
		
		api = new ReittiopasAPI();
		
		Log.i(TAG, "SERVICE CREATED");
		handle = new Handler();
		
		alarmSound = Uri.parse("android.resource://"+getPackageName()+"/" +R.raw.alarm_clock);
		
		SharedPreferences settings = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
		userId = settings.getString("userId", "");
		Log.i(TAG, "User ID: "+userId);
		if (userId.equals("")) {
			SharedPreferences.Editor editor = settings.edit();
			userId = api.getUniqueID();
			editor.putString("userId", userId);
			Log.i(TAG, "User ID from API: "+userId);
			editor.commit();
		}
	}
	
	@Override
	public void onDestroy() {
		locationManager.removeUpdates(this);
		
		super.onDestroy();
		timer.cancel();
	    timer = null;
	    
	    Log.i(TAG, "SERVICE DESTROYED");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	public static final int NOTIFICATION_ID = 12345;
	private Notification notification = null;
	private PendingIntent contentIntent;
	private Intent notificationIntent;
	private int toastLength = Toast.LENGTH_SHORT;
	String lastText = "";
	
	private AsyncPlayer aPlayer = new AsyncPlayer("aPlayer");
	private Uri alarmSound; 
	
	public void createNotification(String ticker, String title, String text,  boolean vibrate, boolean sound, int tlength) {
		String ns = Context.NOTIFICATION_SERVICE;
		final NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);	
		
		long when = System.currentTimeMillis();

		if (notification == null) {
			notificationIntent = new Intent(this, MapScreen.class);
			notificationIntent.putExtra("currentStep", currentStep);
			//notificationIntent.setFlags(Intent.FLAG_FROM_BACKGROUND);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
						
		}
		
		notification = new Notification(R.drawable.notification, ticker, when);
		contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.defaults = 0;//Notification.DEFAULT_ALL; 
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		
		if (vibrate) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			//notification.defaults |= Notification.DEFAULT_LIGHTS;			
		}
		
		if (sound) {
			/*notification.defaults |= Notification.DEFAULT_SOUND;
			notification.sound = Uri.parse("android.resource://"+this.getPackageName()+"/" +R.raw.alarm_clock); 
			Log.i(TAG, "SOUUUUUUUUUUND: "+notification.sound);*/
			//MediaPlayer mNotify =  
			//MediaPlayer.create(this, R.raw.alarm_clock).start();
			aPlayer.play(this, alarmSound, false, AudioManager.STREAM_SYSTEM);
		}
		//notification.defaults |= Notification.FLAG_INSISTENT;// Add this to the flags field to repeat the audio until the user responds.
		notification.ledARGB = 0xff00ff00;
		notification.ledOnMS = 300;
		notification.ledOffMS = 1000;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		
		notification.setLatestEventInfo(this, title, text, contentIntent);
		/*Looper.prepare();
		Looper.loop();*/
		lastText = text;
		toastLength = tlength;
		mNotificationManager.notify(NOTIFICATION_ID, notification);
		handle.post(new Runnable() {
			public void run() {
				try {
					Toast.makeText(BackgroundService.this, lastText, toastLength).show();
				} catch(Exception e) {};
			}
		});
	}
	
	public void cancelNotification() {
		cancelNotification(true);
	}
	
	public void cancelNotification(final Boolean cancel) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

		
		mNotificationManager.cancel(NOTIFICATION_ID);
		
		handle.post(new Runnable() {
			public void run() {
				if (cancel) Toast.makeText(BackgroundService.this, getString(R.string.bsToastRouteCancel), Toast.LENGTH_SHORT).show();
			}
		});
		
	}
}
