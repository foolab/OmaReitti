package com.omareitti;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.omareitti.R;
import com.omareitti.datatypes.Coords;
import com.omareitti.datatypes.GeoRec;

import android.graphics.Color;
import android.util.Log;

public class Route {
    public ArrayList<RouteStep> steps;

    public double length = 0;
    public double duration = 0;
    public double actual_duration = 0;

    public Date depTime = null;
    public Date arrTime = null;
    public Date firstBusTime = null;

    public class PathSegment {
	public Coords coords;

	public Date arrTime;
	public Date depTime;

	public String name = null;
    }

    public class RouteStep {
	public RouteStep() {
	}

	public int iconId = -1;
	public String desc;
	public String busNumber = "";

	public Date arrTime = null;
	public Date depTime = null;

	public String firstLoc = "";
	public String lastLoc = "";

	public double length = 0;
	public double duration = 0;
	public int type = 0;

	public ArrayList<PathSegment> path;

	public boolean hasRemindedArr = false;

	public int getIconId() {
	    if (iconId > -1)
		return iconId;

	    switch (type) {
	    case 1:
	    case 3:
	    case 4:
	    case 5:
	    case 8:
	    case 21:
	    case 22:
	    case 23:
	    case 24:
	    case 25:
	    case 36:
	    case 39:
		iconId = R.drawable.bus;
		break;

	    case 2:
		iconId = R.drawable.tram;
		break;

	    case 6:
		iconId = R.drawable.metro;
		break;

	    case 7:
		iconId = R.drawable.boat;
		break;

	    case 12:
		iconId = R.drawable.train;
		break;

	    case 0:
	    default:
		iconId = R.drawable.man;
		break;
	    }

	    return iconId;
	}

	public int getTransportName() {
	    switch (type) {
	    case 1:
	    case 3:
	    case 4:
	    case 5:
	    case 8:
	    case 21:
	    case 22:
	    case 23:
	    case 24:
	    case 25:
	    case 36:
	    case 39:
		return R.string.tr_bus;

	    case 2:
		return R.string.tr_tram;

	    case 6:
		return R.string.tr_metro;

	    case 7:
		return R.string.tr_boat;

	    case 12:
		return R.string.tr_train;

	    case 0:
	    default:
		return R.string.tr_walk;
	    }
	}

	public int getColor() {
	    switch (type) {
	    case 1:
	    case 3:
	    case 4:
	    case 5:
	    case 8:
	    case 21:
	    case 22:
	    case 23:
	    case 24:
	    case 25:
	    case 36:
	    case 39:
		return Color.rgb(28, 135, 2301);

	    case 2:
		return Color.rgb(180, 213, 113);

	    case 6:
		return Color.rgb(252, 128, 45);

	    case 7:
		return Color.rgb(90, 214, 254);

	    case 12:
		return Color.rgb(211, 40, 84);

	    case 0:
	    default:
		return Color.rgb(100, 100, 100);
	    }
	}

	public String getBusNumber() {
	    if (busNumber != "")
		return busNumber;

	    if (desc == null ||
		desc.substring(0,4).equals("1300") ||
		desc.substring(0,4).equals("1019")) {
		// No desc, e.g. walking or
		// subway (1300) or
		// Suomenlinna ferry (1019)
		busNumber = "";
	    }
	    else if (desc.substring(0,2).equals("11")) {
		// Helsinki night busses
		busNumber = desc.substring(2,5);
	    }
	    else if (desc.substring(0,1).equals("3")) {
		// Local trains
		busNumber = desc.substring(4,6).trim();
	    }
	    else {
		int number = Integer.parseInt(desc.substring(1,4));
		String letter = desc.substring(4,6);
		busNumber = (""+number+letter).trim();
	    }

	    return busNumber;
	}
    }

    public static ArrayList<Route> parseRoute(String json) {
	ArrayList<Route> routes = new ArrayList<Route>();

	try {
	    JSONArray list = new JSONArray(json);

	    for(int i=0;i<list.length();i++)
                routes.add(new Route(list.getJSONArray(i)));
	} catch (Exception e) {
	    Log.e("HelsinkiTravel", "Caught!", e);
	    return null;
	};

	return routes;
    }

    public static ArrayList<GeoRec> getGeocodeCoords(String json) {
	ArrayList<GeoRec> res = new ArrayList<GeoRec>();

	if (json == null) {
	    return null;
	}

	try {
	    // Reittiopas couldn't find any locations
	    if (json.equals("") || json.substring(0, 3).equals("<h1"))
		return res;

	    JSONArray list = new JSONArray(json);

	    // find the lowest locTypeId
	    int minLocTypeID = 1000;
	    for (int i=0; i<list.length(); i++) {
		JSONObject geo_rec = list.getJSONObject(i);
		minLocTypeID = Integer.parseInt(geo_rec.getString("locTypeId")) < minLocTypeID ?
		    Integer.parseInt(geo_rec.getString("locTypeId")) : minLocTypeID;
	    }

	    for (int i = 0; i < list.length(); i++) {
		JSONObject geo_rec = list.getJSONObject(i);

		// filter out unwanted location entries
		if (minLocTypeID <= 2) {
		    if (Integer.parseInt(geo_rec.getString("locTypeId")) > 2) {
			continue;
		    }
		} else {
		    if (Integer.parseInt(geo_rec.getString("locTypeId")) != minLocTypeID) {
			continue;
		    }
		}

		GeoRec rec = new GeoRec();
		rec.city = geo_rec.getString("city");
		rec.name = geo_rec.getString("matchedName");

		if (rec.name == null || rec.name.equals("null") ) {
		    rec.name = geo_rec.getString("name");
		} else {
		    try {
			JSONObject details = geo_rec.getJSONObject("details");

			String house = details.getString("houseNumber");
			if (house != null || !house.equals("null"))
			    rec.name += " "+house;
		    } catch (Exception e) {
		    }
		}

		rec.lang = geo_rec.getString("lang");
		String[] p = geo_rec.getString("coords").split(",");
		rec.coords = new Coords(Double.parseDouble(p[0]), Double.parseDouble(p[1]));
		rec.locType = geo_rec.getString("locType");
		rec.locTypeId = geo_rec.getString("locTypeId");

		//rec.json = geo_rec.toString();

		res.add(rec);
	    }
	} catch (Exception e) {
	    Log.e("HelsinkiTravel", "Caught!", e);
	    return null;
	};

	return res;
    }

    public String jsonString;

    public Route(String json) throws JSONException {
	this(new JSONObject(json));
    }

    public Route(JSONArray a) throws JSONException {
	this(a.getJSONObject(0));
    }

    public Route(JSONObject obj) throws JSONException {
	this.duration = obj.getDouble("duration");
	this.length = obj.getDouble("length");

	JSONArray legs = obj.getJSONArray("legs");

	steps = new ArrayList<Route.RouteStep>();

	for(int i = 0; i < legs.length(); i++) {
	    RouteStep s = new RouteStep();

	    JSONObject leg = legs.getJSONObject(i);

	    s.length = leg.getDouble("length");
	    s.duration = leg.getDouble("duration");
	    this.actual_duration += Math.ceil(s.duration/60)*60;

	    try {
		s.desc = leg.getString("code");
	    } catch (Exception e) {
		//Log.e("HelsinkiTravel", "That's ok");
	    };

	    try {
		s.type = leg.getInt("type");
	    } catch (Exception e) {
		//Log.e("HelsinkiTravel", "That's ok");
	    };

	    JSONArray locs = leg.getJSONArray("locs");

	    s.path = new ArrayList<Route.PathSegment>();

	    for(int j = 0; j < locs.length(); j++) {
		JSONObject loc = locs.getJSONObject(j);
		JSONObject coord = loc.getJSONObject("coord");

		PathSegment p = new PathSegment();
		p.coords = new Coords(coord.getDouble("x"), coord.getDouble("y"));
		p.arrTime = parseReittiOpasDate(loc.getString("arrTime"));
		p.depTime = parseReittiOpasDate(loc.getString("depTime"));

		try {
		    p.name = loc.getString("name");
		} catch (Exception e) {
		    //Log.e("HelsinkiTravel", "That's ok");
		};

		if (p.name != null && !p.name.equals("nul")) {
		    if (s.firstLoc.equals(""))
			s.firstLoc = p.name;

		    s.lastLoc = p.name;
		}

		s.path.add(p);

		if (s.depTime == null && p.depTime != null) {
		    s.depTime = parseReittiOpasDate(loc.getString("depTime"));
		}

		if (p.arrTime != null) {
		    s.arrTime = parseReittiOpasDate(loc.getString("arrTime"));
		}

		if (this.depTime == null && p.depTime != null) {
		    this.depTime = parseReittiOpasDate(loc.getString("depTime"));
		}

		if (p.arrTime != null) {
		    this.arrTime = parseReittiOpasDate(loc.getString("arrTime"));
		}

		if (this.firstBusTime == null && p.depTime != null && s.type != 0) {
		    this.firstBusTime = parseReittiOpasDate(loc.getString("depTime"));
		}
	    }

	    steps.add(s);
	}

	this.jsonString = obj.toString();
    }

    private Date parseReittiOpasDate(String s) {
	String year = s.substring(0, 4);
	String month = s.substring(4, 6);
	String day = s.substring(6, 8);
	String hour = s.substring(8, 10);
	String minute = s.substring(10, 12);

	return new Date(Integer.parseInt(year)-1900,
			Integer.parseInt(month)-1,
			Integer.parseInt(day),
			Integer.parseInt(hour),
			Integer.parseInt(minute));
    }
}

/**
 *  1	length	Number	Length of the route in meters.
 2	duration	Number	Duration of the route in seconds.
 3	legs	Array
 Array of legs of the route.
 3.1	length	Number	Length of the leg in meters.
 3.2	duration	Number	Duration of the leg in seconds.
 3.3	type	String/Number	
 Type of the leg:
	
 walk
 transport type id (see parameter mode_cost above for explanation of the ids)
 3.4
	
 locs	Array	Array of locations on the leg (limited detail only lists start and end locations).
 3.5	shape	List	Shape (list of coordinates) of the leg (only in full detail).
 3.4.1	coord	Coordinate	Coordinate of the location.
 3.4.2	arrTime	Number	Arrival time to the location, format YYYYMMDDHHMM.
 3.4.3	depTime	Number	Departure time from the location, format YYYYMMDDHHMM.
 3.4.4	name	String	Name of the location.
*/

/**
 * 
 1 = Helsinki internal bus lines
 2 = trams
 3 = Espoo internal bus lines
 4 = Vantaa internal bus lines
 5 = regional bus lines
 6 = metro
 7 = ferry
 8 = U-lines
 12 = commuter trains
 21 = Helsinki service lines
 22 = Helsinki night buses
 23 = Espoo service lines
 24 = Vantaa service lines
 25 = region night buses
 36 = Kirkkonummi internal bus lines
 39 = Kerava internal bus lines
*/
