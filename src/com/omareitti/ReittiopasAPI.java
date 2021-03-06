package com.omareitti;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.omareitti.datatypes.GeoRec;


import android.util.Log;

public class ReittiopasAPI {
    private static final String TAG = "ReittiopasAPI";

    private static final String main_url =
	"http://api.reittiopas.fi/hsl/1_1_2/?request=";

    private static final String auth =
	"&user=difogic&pass=routeapi$44&epsg_out=4326&epsg_in=4326&show=5"; 

    static DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    static DateFormat timeFormat = new SimpleDateFormat("HHmm");

    // Walking speed. Optional, default 70 m/min, range 1-500.
    public static int walkingSpeed = 70;

    public static String formatDate(Date dt) {
	return dateFormat.format(dt);
    }

    public static String formatTime(Date dt) {
	return timeFormat.format(dt);
    }

    public static ArrayList<GeoRec> getGeocode(String search) {
	String url = main_url+"geocode"+auth+"&key="+search.replace(" ", "+").toLowerCase();
	return Route.getGeocodeCoords(queryUrl(url));
    }

    public static ArrayList<GeoRec> getReverseGeocode(String data) {
	String url = main_url+"reverse_geocode"+auth+"&coordinate="+data.replace(" ", "+");
	return Route.getGeocodeCoords(queryUrl(url));
    }

    public ArrayList<Route> getRoute(String from, String to, String date,
				     String time, String optimize,
				     String timetype, String transport_types) {

	if (from == null || to == null)
	    return null;

        Date dt = new Date();

	if (date.equals(""))
	    date = dateFormat.format(dt);

	if (time.equals(""))
	    time = timeFormat.format(dt);

	if (optimize.equals(""))
	    optimize = "default";

	if (timetype.equals(""))
	    timetype = "departure"; // "arrival" or "departure"

	if (transport_types.equals(""))
	    transport_types = "all";

	transport_types = transport_types.replace("|", "%7C");

	String url = main_url+"route"+auth+"&from="+from.toLowerCase()
	    +"&to="+to.toLowerCase()
	    +"&date="+date
	    +"&time="+time
	    +"&optimize="+optimize
	    +"&timetype="+timetype
	    +"&transport_types="+transport_types
	    +"&walk_speed="+walkingSpeed
	    +"&show=5"
	    +"&detail=full";

	String content = queryUrl(url);
	ArrayList<Route> routes = Route.parseRoute(content, from, to);
	return routes;
    }

    public static String queryUrl(String url) {
	HttpClient httpclient = new DefaultHttpClient();
	HttpGet httpget = new HttpGet(url);
	HttpResponse response;

	try {
	    response = httpclient.execute(httpget);
	    Log.i(TAG, "Url:" + url + "");
	    Log.i(TAG, "Status:[" + response.getStatusLine().toString() + "]");
	    HttpEntity entity = response.getEntity();

	    if (entity != null) {
		InputStream instream = entity.getContent();

		BufferedReader r = new BufferedReader(new InputStreamReader(instream));
		StringBuilder total = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
		    total.append(line);
		}

		instream.close();

		String result = total.toString();
		return result;
	    }
	} catch (ClientProtocolException e) {
	    Log.e(TAG, "There was a protocol based error", e);
	} catch (Exception e) {
	    Log.e(TAG, "There was some error", e);
	}

	return null;
    }
}