package com.omareitti;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

import com.omareitti.IBackgroundServiceAPI;
import com.omareitti.IBackgroundServiceListener;
import com.omareitti.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.omareitti.Route.PathSegment;
import com.omareitti.Route.RouteStep;
import com.omareitti.datatypes.GeoRec;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MapScreen extends MapActivity {
	
	private static final String TAG = MapScreen.class.getSimpleName();
	
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}

	LinearLayout linearLayout;
	MapView mapView;
	
	private Paint mPaint;
	
	public Route route = null;
	private String routeString;
	
	private int currentStep = 0;
	
	private int zoomLevel;
	private String pickPoint;
	
	Bitmap markerBmp;
	
	//private MyLocationOverlay myLocOverlay;
	
	private Boolean isJustLooking = false;
	
	SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		setContentView(R.layout.mapscreen);
		
		SharedPreferences settings = getSharedPreferences(getString(R.string.PREFS_NAME), 0);
		routeString = settings.getString("route", "");

		//Log.i(TAG, "routeString:"+routeString);
		//Log.i(TAG, "getIntent().getExtras():"+getIntent().getExtras());
		
		if (getIntent().getExtras() == null && routeString.equals("")) {
            startActivity(new Intent(MapScreen.this, MainApp.class));
            Log.i(TAG, "intent extras is null, switching to main activity ");
            finish();
            return;
		}
		
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(mListener, mSensor,
                SensorManager.SENSOR_DELAY_GAME);
        
        makeArrow();
       // markerBmp = BitmapFactory.decodeResource(getResources(), R.drawable.map_pin); // pin.png image will require.
        
		isJustLooking = getIntent().getExtras().getBoolean("isJustLooking", false);
		pickPoint = getIntent().getExtras().getString("pickPoint");
		//if (pickPoint == null) pickPoint = "";
		Log.i(TAG, "pickPoint: "+pickPoint);
		
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
		mapView.setSatellite(false);
		mapView.setStreetView(false);
        
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		zoomLevel = Integer.parseInt(prefs.getString("prefMapZoomLevel", "15"));
		
		myMapController = mapView.getController();
	    //myMapController = mapView.getController();
	    myMapController.setZoom(zoomLevel);

	    myMapController.setCenter(new GeoPoint(6016265,24915534)); 
		//myMapController.setCenter(new GeoPoint(60171135, 24943797));
		//myMapController.setZoom(12);
		
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5);
        
		Log.i(TAG, "trying to bind service "+BackgroundService.class.getName());
		Intent servIntent = new Intent(BackgroundService.class.getName());//this, BackgroundService.class);
        startService(servIntent);
        Log.i(TAG, "starting service "+servIntent.toString());
        bindService(servIntent, servceConection, 0);	
        
        //myLocOverlay = new MyLocationOverlay(this, mapView);
		//myLocOverlay.enableMyLocation();
		//mapView.getOverlays().add(myLocOverlay);
        
		if (pickPoint != null) {

			MapOverlay mapOverlay = new MapOverlay();
	        /*List<Overlay> listOfOverlays = mapView.getOverlays();
	        listOfOverlays.clear();*/
			mapView.getOverlays().add(mapOverlay);        
	 
	        mapView.invalidate();				
	        Toast.makeText(this, getString(R.string.msToastAddressSelect), Toast.LENGTH_SHORT).show();
	        takeLocation = true;
	        ((Button)findViewById(R.id.gotoRouteButton)).setVisibility(View.GONE);
			return;
		}
		
        
    	try {
        	
        	if (!routeString.equals("")) route = new Route(routeString);
        	else {
        		Log.i(TAG, "Couldn't get the route from JSONobj "+routeString);
        		Toast.makeText(this, "Can't get current route", Toast.LENGTH_LONG);
        		startActivity(new Intent(MapScreen.this, MainApp.class));
        		finish();
                return;        		
        	}
    	} catch ( Exception e ) {
    		Log.e(TAG, "Couldn't get the route from JSONobj "+routeString, e);
    	}
		
        currentStep = getIntent().getExtras().getInt("currentStep");
        
		mapView.getOverlays().add(new RouteOverlay()); 
	    //myMapController.setCenter(new GeoPoint(60216298, 24881828));
		
		if (route != null && route.steps.size() > currentStep) {
			RouteStep r = route.steps.get(currentStep);
			if (r.path.size() > 0) {
				PathSegment p = r.path.get(0);
				myMapController.setCenter(new GeoPoint((int) (p.coords.x * 1E6), (int) (p.coords.y * 1E6)));
			}
		}
		
		((Button)findViewById(R.id.gotoRouteButton)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				MapScreen.this.onBackPressed();
			}
		});
	}
	
	private static final int LOC_MENU_ID = 333; 
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	Drawable drLocation  = getResources().getDrawable(android.R.drawable.ic_menu_mylocation);	
    	MenuItem tmp = menu.add(0, LOC_MENU_ID, 0, getString(R.string.msMenuCurrentLocation));
    	tmp.setIcon(drLocation);

    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
	        case LOC_MENU_ID:
	        	if (currentPoint == null) { takeLocation = true; return true; }
	        	myMapController.animateTo(currentPoint);
	        	break;
	        default:
	        	return super.onOptionsItemSelected(item);	        	
        }
        return true;
    }
    

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(mListener);
		try {
			if (api != null) api.removeListener(serviceListener);
			unbindService(servceConection);
			Log.i(TAG, "unbind ");		
		} catch(Exception e) {
			Log.e(TAG, "ERROR!!", e);
		}
	}	    
	
	class MapOverlay extends com.google.android.maps.Overlay {
		@Override
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
				long when) {
			super.draw(canvas, mapView, shadow);
			if (currentPoint == null) return false;

			Point screenPts = new Point();
			mapView.getProjection().toPixels(currentPoint, screenPts);

			drawArrow(canvas, screenPts.x, screenPts.y);
			return true;
		}

		private long lastTouchTime;  
		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			if (pickPoint == null) return super.onTouchEvent(event, mapView);

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				 long thisTime = System.currentTimeMillis();
				if (thisTime - lastTouchTime > 250) {
					// Too slow :)
					lastTouchTime = thisTime;
					return false;
				} else {
					// Double tap
					lastTouchTime = -1;
				}
			 				
				GeoPoint p = mapView.getProjection().fromPixels(
						(int) event.getX(), (int) event.getY());

				Geocoder geoCoder = new Geocoder(getBaseContext(),
						Locale.getDefault());
				try {
					/*List<Address> addresses = geoCoder.getFromLocation(
							p.getLatitudeE6() / 1E6, p.getLongitudeE6() / 1E6,
							1);
					String add = "";
					if (addresses.size() > 0) {
						if (addresses.get(0).getMaxAddressLineIndex() > 0)
							add += addresses.get(0).getAddressLine(0);
						/*for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
							add += addresses.get(0).getAddressLine(i);
							if (i != addresses.get(0).getMaxAddressLineIndex() - 1) add += ", ";
						}
						for (int j = 0; j < addresses.size(); j++)
							for (int i = 0; i < addresses.get(j).getMaxAddressLineIndex(); i++)
								Log.i(TAG, "j:"+j+" i:"+i+" "+addresses.get(j).getAddressLine(i));*/
					/*}*/
					
					ArrayList<GeoRec> recs = (new ReittiopasAPI()).getReverseGeocode(""+(p.getLongitudeE6() / 1E6)+","+(p.getLatitudeE6() / 1E6));
					if (recs.size() == 0) return false;
					String name = recs.get(0).name;
					Toast.makeText(getBaseContext(), name, Toast.LENGTH_SHORT).show();
					if (name.length() > 0) {
						//Intent intent = getIntent();
						Intent intent = new Intent(); 
						intent.putExtra("mapAddress", name);
						intent.putExtra("mapCoords", (p.getLongitudeE6() / 1E6f)+","+(p.getLatitudeE6() / 1E6f));
						Log.i(TAG, "mapCoords:"+(p.getLongitudeE6() / 1E6f)+","+(p.getLatitudeE6() / 1E6f));
						setResult(RESULT_OK, intent);
						//mapView.getOverlays().remove(myLocOverlay);
						finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			} else
				return false;
		}
	}
    
	class RouteOverlay extends Overlay {
		@Override
	    public void draw(Canvas canvas, MapView mapv, boolean shadow){
	        super.draw(canvas, mapv, shadow);

	        if (route == null) return;
	        
	        Point p1 = new Point();
	        Point p2 = new Point();
	        
	        for (int i = 0; i < route.steps.size(); i++) {
	        	Path path = new Path();
	        	
	        	RouteStep r = route.steps.get(i);
	        	mPaint.setColor(r.getColor());
	        	
	        	for (int j = 0; j < r.path.size(); j++) {
	        		PathSegment p = r.path.get(j);
	        		
	        		int x = (int) (p.coords.x * 1E6);
	        		int y = (int) (p.coords.y * 1E6);
	        		
	        		GeoPoint gP1 = new GeoPoint(x, y);
	        		mapView.getProjection().toPixels(gP1, p1);
	        		
	        		if (j == 0)  {
	        			path.moveTo(p1.x, p1.y);
	        			//canvas.drawCircle(p1.x, p1.y, 4, mPaint);
	        			path.addCircle(p1.x, p1.y, 5, Path.Direction.CW);
	        		}
	        		else {
	        			path.moveTo(p2.x, p2.y);
	        			path.addCircle(p2.x, p2.y, 3, Path.Direction.CW);
	        			path.lineTo(p1.x, p1.y);
	        			//canvas.drawCircle(p1.x, p1.y, 3, mPaint);
	        		}
	        		
	        		p2.x = p1.x; p2.y = p1.y; 
	        		//Log.i(TAG, "path:"+j+" x:"+p1.x+" y:"+p1.y);
	        	}
	        	canvas.drawPath(path, mPaint);
	        }
	        if (currentPoint != null) {
				Point screenPts = new Point();
				mapView.getProjection().toPixels(currentPoint, screenPts);

				drawArrow(canvas, screenPts.x, screenPts.y);
	        }
	    }
		
		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {
			return super.onTouchEvent(event, mapView);
		}
	}
	
	private MapController myMapController;
	private GeoPoint currentPoint = null;
	private float locationAngle = 0f;
	private Boolean takeLocation = false;
    /** 
     * Service interaction stuff
     */
    private IBackgroundServiceListener serviceListener = new IBackgroundServiceListener.Stub() {
 		
		public void locationDiscovered(double lat, double lon)
				throws RemoteException {
			//Log.i(TAG, "locationDiscovered: "+lat+" "+lon);
			//if (pickPoint != null) {
	        	int ilat = (int) (lat * 1E6);
	        	int ilng = (int) (lon * 1E6);
	        	currentPoint = new GeoPoint(ilat, ilng);
	        	myMapController.animateTo(currentPoint); //setCenter	
	        	locationAngle = 0f;
			//}
		}
		
		public void handleUpdate(String s) throws RemoteException {
			//Log.i(TAG, "handleUpdate: "+s);
		}
		
		public void handleGPSUpdate(double lat, double lon, float angle) throws RemoteException {
			if (!takeLocation && pickPoint != null) return;
			
        	int ilat = (int) (lat * 1E6);
        	int ilng = (int) (lon * 1E6);
        	currentPoint = new GeoPoint(ilat, ilng);
        	
        	if (!isJustLooking)
        		myMapController.animateTo(currentPoint); //setCenter
        	
        	if (takeLocation) takeLocation = false;

        	locationAngle = angle;
        	
            //setContentView(mapView);
		}
		
		public void addressDiscovered(String address) throws RemoteException {
		
		}
	};

	
	private IBackgroundServiceAPI api = null;
	
	private ServiceConnection servceConection = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
			Log.i(TAG, "Service disconnected!");
			api = null;
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			api = IBackgroundServiceAPI.Stub.asInterface(service);

			locationAngle = 0f;
			Log.i(TAG, "Service connected! "+api.toString());
			try {
				api.addListener(serviceListener);
				//if (pickPoint != null) {
					api.requestLastKnownAddress(0);
				//}
			} catch(Exception e) {
				Log.e(TAG, "ERROR!!", e);
			}
		}
	};
	
	private Paint   arrowPaint = new Paint();
    private Path    arrowPath = new Path();
	private void makeArrow() {
		arrowPath.moveTo(0, -30);
		arrowPath.lineTo(-20, 30);
		arrowPath.lineTo(0, 20);
		arrowPath.lineTo(20, 30);
		arrowPath.close();	
	}
	private void drawArrow(Canvas canvas, int cx, int cy) {
		/*int w = canvas.getWidth();
        int h = canvas.getHeight();
        int cx = w / 2;
        int cy = h / 2;*/
		Paint paint = arrowPaint;

        paint.setAntiAlias(true);
        //paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);		

        canvas.save();
        canvas.translate(cx, cy);
        //Log.i(TAG, "ANGLE: "+locationAngle+" "+mValues);
        if (locationAngle == 0 && mValues != null) {
        	paint.setColor(Color.BLUE);
        	canvas.rotate(mValues[0]);
        } else {
        	paint.setColor(Color.GREEN);
        	canvas.rotate(locationAngle);
        }
        
        canvas.drawPath(arrowPath, arrowPaint);
        /*if (mValues != null) {
            canvas.rotate(mValues[0]);
        } 
        canvas.translate(-cx, -cy);*/
        canvas.restore();
	}
	
	private SensorManager mSensorManager;
    private Sensor mSensor;

    private float[] mValues;

    private final SensorEventListener mListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            /*Log.d(TAG,
                    "sensorChanged (" + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ")");*/
            mValues = event.values;
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}
/**
public SampleView(Context context) {
super(context);

// Construct a wedge-shaped path
mPath.moveTo(0, -50);
mPath.lineTo(-20, 60);
mPath.lineTo(0, 50);
mPath.lineTo(20, 60);
mPath.close();
}

@Override protected void onDraw(Canvas canvas) {
Paint paint = mPaint;

canvas.drawColor(Color.WHITE);

paint.setAntiAlias(true);
paint.setColor(Color.BLACK);
paint.setStyle(Paint.Style.FILL);

int w = canvas.getWidth();
int h = canvas.getHeight();
int cx = w / 2;
int cy = h / 2;

canvas.translate(cx, cy);
if (mValues != null) {
    canvas.rotate(-mValues[0]);
}
canvas.drawPath(mPath, mPaint);
}*/