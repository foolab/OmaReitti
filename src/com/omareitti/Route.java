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
		public RouteStep () {
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
			if (iconId > -1) return iconId;
			
			switch (type) {
				case 1: case 3: case 4: case 5: case 8: case 21: case 22: case 23: case 24: case 25: case 36: case 39:
					iconId = R.drawable.bus; break;
				case 2: 
					iconId = R.drawable.tram; break;
				case 6: 
					iconId = R.drawable.metro; break;
				case 7: 
					iconId = R.drawable.boat; break;
				case 12: 
					iconId = R.drawable.train; break;
					
				case 0: default:
					iconId = R.drawable.man; break;			
			}
			
			return iconId;
		}
		
		public int getTransportName() {
			switch (type) {
				case 1: case 3: case 4: case 5: case 8: case 21: case 22: case 23: case 24: case 25: case 36: case 39:
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
				case 1: case 3: case 4: case 5: case 8: case 21: case 22: case 23: case 24: case 25: case 36: case 39:
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
		/*
		public String getStringDuration() {
			int hours = (int) Math.floor(duration/3600);
			int mins = (int) Math.ceil((duration-hours*3600)/60);
			
			if (hours == 0) {
				return ""+mins+" min";	
			} else {
				return ""+hours+" h "+mins+" min";
			}		
		}
		*/
		public String getBusNumber() {
			if (busNumber != "") return busNumber;			
			//if (desc != null) return desc;
			
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
	/*
	public String getStringDuration() {
		int hours = (int) Math.floor(duration/3600);
		int mins = (int) Math.ceil((duration-hours*3600)/60);
		
		if (hours == 0) {
			return ""+mins+" min";	
		} else {
			return ""+hours+" h "+mins+" min";
		}		
	}
	*/
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
/*
 * [{"locType":"address","locTypeId":900,"name":"Matinraitti","matchedName":"Matinraitti 5","lang":"fi","city":"Espoo","coords":"24.7485443901,60.1582536605","details":{"houseNumber":5}}] 
 * [{"locType":"poi","locTypeId":1,"name":"Kamppi","matchedName":"Kamppi","lang":"fi","city":"Helsinki","coords":"24.9305257301,60.1681327014","details":{"poiType":"city area"}},{"locType":"poi","locTypeId":2,"name":"Kamppi metroasema","matchedName":"Kamppi metroasema","lang":"fi","city":"Helsinki","coords":"24.9304072679,60.1684028131","details":{"poiType":"station"}},{"locType":"poi","locTypeId":2,"name":"Kampin terminaali","matchedName":"Kamppi terminal","lang":"en","city":"Helsinki","coords":"24.9320442059,60.1689568049","details":{"poiType":"station"}},{"locType":"poi","locTypeId":9,"name":"City Car Club - Kamppi, Annankatu","matchedName":"Kamppi, Annankatu - City Car Club","lang":"slangi","city":"Helsinki","coords":"24.9365843166,60.1670757517","details":{"poiType":"city car club"}},{"locType":"poi","locTypeId":9,"name":"City Car Club - Kamppi, Eerikinkatu","matchedName":"Kamppi, Eerikinkatu - City Car Club","lang":"slangi","city":"Helsinki","coords":"24.9285808156,60.1649598526","details":{"poiType":"city car club"}},{"locType":"poi","locTypeId":9,"name":"City Car Club - Kamppi, Fredrikinkatu","matchedName":"Kamppi, Fredrikinkatu - City Car Club","lang":"slangi","city":"Helsinki","coords":"24.9327898839,60.1672999741","details":{"poiType":"city car club"}},{"locType":"poi","locTypeId":9,"name":"City Car Club - Kamppi, Kalevankatu","matchedName":"Kamppi, Kalevankatu - City Car Club","lang":"slangi","city":"Helsinki","coords":"24.9341551717,60.1658990786","details":{"poiType":"city car club"}},{"locType":"poi","locTypeId":9,"name":"City Car Club - Kamppi, Kansakoulukuja","matchedName":"Kamppi, Kansakoulukuja - City Car Club","lang":"slangi","city":"Helsinki","coords":"24.9337375894,60.167688248","details":{"poiType":"city car club"}},{"locType":"poi","locTypeId":9,"name":"City Car Club - Kamppi, Lapinlahdenkatu","matchedName":"Kamppi, Lapinlahdenkatu - City Car Club","lang":"slangi","city":"Helsinki","coords":"24.9310216204,60.1665584176","details":{"poiType":"city car club"}},{"locType":"poi","locTypeId":9,"name":"City Car Club - Kamppi, Ruoholahdenkatu","matchedName":"Kamppi, Ruoholahdenkatu - City Car Club","lang":"slangi","city":"Helsinki","coords":"24.9227896737,60.1639859986","details":{"poiType":"city car club"}},{"locType":"address","locTypeId":900,"name":"Kamppiaistie","matchedName":"Kamppiaistie","lang":"fi","city":"Helsinki","coords":"24.921474301,60.2437375222","details":{"houseNumber":1}},{"locType":"stop","locTypeId":10,"name":"Kamppi","matchedName":"Kamppi","lang":"fi","city":"Helsinki","coords":"24.9302487203,60.1691668866","details":{"code":"1040143","shortCode":"1237","changeCost":0.3,"lines":["1070T 1:Suutarila"]}},{"locType":"stop","locTypeId":10,"name":"Kamppi","matchedName":"Kamppi","lang":"fi","city":"Helsinki","coords":"24.931502109,60.1688977825","details":{"code":"1040601","shortCode":"0013","changeCost":0.3,"lines":["1300M 1:Mellunm\u00e4ki","1300M21:It\u00e4keskus","1300V 1:Vuosaari"]}},{"locType":"stop","locTypeId":10,"name":"Kamppi","matchedName":"Kamppi","lang":"fi","city":"Helsinki","coords":"24.931502109,60.1688977825","details":{"code":"1040602","shortCode":"0013","changeCost":0.3,"lines":["1300M 2:Ruoholahti","1300M22:Ruoholahti","1300V 2:Ruoholahti"]}},{"locType":"stop","locTypeId":10,"name":"Kamppi","matchedName":"Kamppi","lang":"fi","city":"Helsinki","coords":"24.9302487203,60.1691668866","details":{"code":"1040243","shortCode":"1237","changeCost":0.3,"lines":["2205  1:Lepp\u00e4vaara, lait.23"]}},{"locType":"stop","locTypeId":10,"name":"Kamppi(M)","matchedName":"Kamppi(M)","lang":"fi","city":"Helsinki","coords":"24.9286120549,60.1686218468","details":{"code":"1040115","shortCode":"1220","changeCost":0.6,"lines":["1039  2:Kamppi","1039A 2:Kamppi","1041  2:Kamppi","1045  2:Kamppi","1047  2:Kamppi","1047 12:Kamppi","1070T 2:Kamppi"]}},{"locType":"stop","locTypeId":10
	
 */
	public static ArrayList<GeoRec> getGeocodeCoords(String json) {
		ArrayList<GeoRec> res = new ArrayList<GeoRec>();
		try {
			// Reittiopas couldn't find any locations
			if (json.equals("") || json.substring(0, 3).equals("<h1")) return res;
			
			JSONArray list = new JSONArray(json);

			// find the lowest locTypeId
			int minLocTypeID = 1000;
			for (int i=0; i<list.length(); i++) {
				JSONObject geo_rec = list.getJSONObject(i);
				minLocTypeID = Integer.parseInt(geo_rec.getString("locTypeId")) < minLocTypeID ? 
						Integer.parseInt(geo_rec.getString("locTypeId")) : minLocTypeID;				 
			}
			
			for (int i=0; i<list.length(); i++) {
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
						if (house != null || !house.equals("null") ) rec.name += " "+house;
					} catch (Exception e) {
					}
				}
				//Log.i("ROUTEEEE", "rec.name: "+rec.name+" "+geo_rec.getString("matchedName"));
				
				rec.lang = geo_rec.getString("lang");
				
				
				rec.coords = geo_rec.getString("coords");
				rec.locType = geo_rec.getString("locType");
				rec.locTypeId = geo_rec.getString("locTypeId");
				
				rec.json = geo_rec.toString(); // DELETE THIS AFTER ALL
				//Log.i("ROUTE!!!!!", rec.toString());
				res.add(rec);
			}
		} catch (Exception e) {
			Log.e("HelsinkiTravel", "Caught!", e);
			return null;
		};
		return res;
	}
	
	
	public Route() {}
	
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
		
		for(int i=0;i<legs.length();i++) {
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
			
			for(int j=0;j<locs.length();j++) {
				JSONObject loc = locs.getJSONObject(j);
				JSONObject coord = loc.getJSONObject("coord");
			
				PathSegment p = new PathSegment();
				p.coords = new Coords(coord.getDouble("y"), coord.getDouble("x"));
				
				p.arrTime = parseReittiOpasDate(loc.getString("arrTime"));
				p.depTime = parseReittiOpasDate(loc.getString("depTime"));
								
				try {
					p.name = loc.getString("name");
				} catch (Exception e) {
					//Log.e("HelsinkiTravel", "That's ok");
				};	
				
				if (p.name != null && !p.name.equals("nul")) {
					if (s.firstLoc.equals("")) s.firstLoc = p.name;
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
		
		//Log.i("date", year+" "+month+" "+day+" "+hour+" "+minute);
		
		return new Date( Integer.parseInt(year)-1900, Integer.parseInt(month)-1, Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(minute));		
	}
	
	public void test() {
		Log.i("ROUTE-TEST", "Duration: " + this.duration);
		Log.i("ROUTE-TEST", "Length: " + this.length);
		Log.i("ROUTE-TEST", "Steps: " + this.steps.size());
		for(int i=0; i<this.steps.size(); i++) {
			RouteStep s = this.steps.get(i);
			Log.i("ROUTE-TEST", "\tDuration:"+s.duration);
			Log.i("ROUTE-TEST", "\tLength:"+s.length);
			
			Log.i("ROUTE-TEST", "\tfirstLoc:"+s.firstLoc);
			Log.i("ROUTE-TEST", "\tlastLoc:"+s.lastLoc);
			Log.i("ROUTE-TEST", "\ttype:"+s.type);
			Log.i("ROUTE-TEST", "\tIcon:"+s.getIconId());
			
			Log.i("ROUTE-TEST", "\tPath length:"+s.path.size());
			
			for(int j=0; j<s.path.size(); j++) {
				PathSegment p = s.path.get(j);
				Log.i("ROUTE-TEST", "\t\tPath name:"+p.name);
				Log.i("ROUTE-TEST", "\t\tCoords:"+p.coords.x+" "+p.coords.y);
				Log.i("ROUTE-TEST", "\t\tarrTime:"+p.arrTime.toLocaleString());
				Log.i("ROUTE-TEST", "\t\tdepTime:"+p.depTime.toLocaleString());
			}
		}
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

/*
[
[
   {
      "length":12321.299,
      "duration":960,
      "legs":[
         {
            "length":347,
            "duration":238.92,
            "type":"walk",
            "locs":[
               {
                  "coord":{
                     "x":24.7361910358,
                     "y":60.1606744939
                  },
                  "arrTime":"201103080033",
                  "depTime":"201103080033",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7362807305,
                     "y":60.1616254306
                  },
                  "arrTime":"201103080034",
                  "depTime":"201103080034",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7363367946,
                     "y":60.1616358902
                  },
                  "arrTime":"201103080035",
                  "depTime":"201103080035",
                  "name":"Markkinakatu"
               },
               {
                  "coord":{
                     "x":24.7355168722,
                     "y":60.1626627845
                  },
                  "arrTime":"201103080036",
                  "depTime":"201103080036",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7362802203,
                     "y":60.1630525852
                  },
                  "arrTime":"201103080037",
                  "depTime":"201103080037",
                  "name":"Piispansilta"
               }
            ]
         },
         {
            "length":11816,
            "duration":660,
            "type":"5",
            "code":"2143  2",
            "locs":[
               {
                  "coord":{
                     "x":24.7362802203,
                     "y":60.1630525852
                  },
                  "arrTime":"201103080037",
                  "depTime":"201103080037",
                  "name":"Piispansilta"
               },
               {
                  "coord":{
                     "x":24.7555826217,
                     "y":60.1659778169
                  },
                  "arrTime":"201103080038",
                  "depTime":"201103080038",
                  "name":"Matinsolmu"
               },
               {
                  "coord":{
                     "x":24.772475979,
                     "y":60.1674243176
                  },
                  "arrTime":"201103080039",
                  "depTime":"201103080039",
                  "name":"Haukilahdensolmu"
               },
               {
                  "coord":{
                     "x":24.8044677066,
                     "y":60.1689215434
                  },
                  "arrTime":"201103080040",
                  "depTime":"201103080040",
                  "name":"Westendinasema"
               },
               {
                  "coord":{
                     "x":24.8259550043,
                     "y":60.1687894394
                  },
                  "arrTime":"201103080041",
                  "depTime":"201103080041",
                  "name":"Karhusaari"
               },
               {
                  "coord":{
                     "x":24.8422306971,
                     "y":60.1656262732
                  },
                  "arrTime":"201103080043",
                  "depTime":"201103080043",
                  "name":"Hanasaari"
               },
               {
                  "coord":{
                     "x":24.9134665043,
                     "y":60.1642300842
                  },
                  "arrTime":"201103080047",
                  "depTime":"201103080047",
                  "name":"L\u00e4nsiv\u00e4yl\u00e4"
               },
               {
                  "coord":{
                     "x":24.9267577712,
                     "y":60.1661035862
                  },
                  "arrTime":"201103080048",
                  "depTime":"201103080048",
                  "name":"Lapinrinne"
               },
               {
                  "coord":{
                     "x":24.9301475091,
                     "y":60.1687726522
                  },
                  "arrTime":"201103080048",
                  "depTime":"201103080048",
                  "name":"Kamppi, tulolaituri"
               }
            ]
         },
         {
            "length":156,
            "duration":108.9,
            "type":"walk",
            "locs":[
               {
                  "coord":{
                     "x":24.9301475091,
                     "y":60.1687726522
                  },
                  "arrTime":"201103080048",
                  "depTime":"201103080048",
                  "name":"Kamppi, tulolaituri"
               },
               {
                  "coord":{
                     "x":24.9293864314,
                     "y":60.1685499985
                  },
                  "arrTime":"201103080048",
                  "depTime":"201103080048",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.929467381,
                     "y":60.1684183813
                  },
                  "arrTime":"201103080048",
                  "depTime":"201103080048",
                  "name":"Runeberginkatu"
               },
               {
                  "coord":{
                     "x":24.9305257301,
                     "y":60.1681327014
                  },
                  "arrTime":"201103080049",
                  "depTime":"201103080049",
                  "name":null
               }
            ]
         }
      ]
   }
],
[
   {
      "length":13121.839,
      "duration":1500,
      "legs":[
         {
            "length":807,
            "duration":635.52,
            "type":"walk",
            "locs":[
               {
                  "coord":{
                     "x":24.7361910358,
                     "y":60.1606744939
                  },
                  "arrTime":"201103080037",
                  "depTime":"201103080037",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7373687958,
                     "y":60.160270321
                  },
                  "arrTime":"201103080038",
                  "depTime":"201103080038",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7372029909,
                     "y":60.1600235109
                  },
                  "arrTime":"201103080039",
                  "depTime":"201103080039",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7388370603,
                     "y":60.1594911254
                  },
                  "arrTime":"201103080041",
                  "depTime":"201103080041",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7387811198,
                     "y":60.1594860516
                  },
                  "arrTime":"201103080041",
                  "depTime":"201103080041",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7397372291,
                     "y":60.1588380527
                  },
                  "arrTime":"201103080042",
                  "depTime":"201103080042",
                  "name":"Kala-Maijan polku"
               },
               {
                  "coord":{
                     "x":24.7405180562,
                     "y":60.1582448807
                  },
                  "arrTime":"201103080043",
                  "depTime":"201103080043",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7406795912,
                     "y":60.1580608719
                  },
                  "arrTime":"201103080043",
                  "depTime":"201103080043",
                  "name":"Kala-Maijan polku"
               },
               {
                  "coord":{
                     "x":24.7414393651,
                     "y":60.157413957
                  },
                  "arrTime":"201103080045",
                  "depTime":"201103080045",
                  "name":"Anjankuja"
               },
               {
                  "coord":{
                     "x":24.7428256472,
                     "y":60.1577203502
                  },
                  "arrTime":"201103080046",
                  "depTime":"201103080046",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7432892153,
                     "y":60.1577545528
                  },
                  "arrTime":"201103080046",
                  "depTime":"201103080046",
                  "name":"Matinkyl\u00e4ntie"
               },
               {
                  "coord":{
                     "x":24.744491805,
                     "y":60.1566249307
                  },
                  "arrTime":"201103080048",
                  "depTime":"201103080048",
                  "name":"Akselinpolku"
               }
            ]
         },
         {
            "length":12157,
            "duration":780,
            "type":"5",
            "code":"2132  2",
            "locs":[
               {
                  "coord":{
                     "x":24.744491805,
                     "y":60.1566249307
                  },
                  "arrTime":"201103080048",
                  "depTime":"201103080048",
                  "name":"Akselinpolku"
               },
               {
                  "coord":{
                     "x":24.7489377666,
                     "y":60.1597144968
                  },
                  "arrTime":"201103080049",
                  "depTime":"201103080049",
                  "name":"Matinniitty"
               },
               {
                  "coord":{
                     "x":24.7522695298,
                     "y":60.163662944
                  },
                  "arrTime":"201103080049",
                  "depTime":"201103080049",
                  "name":"Matinsyrj\u00e4"
               },
               {
                  "coord":{
                     "x":24.772475979,
                     "y":60.1674243176
                  },
                  "arrTime":"201103080051",
                  "depTime":"201103080051",
                  "name":"Haukilahdensolmu"
               },
               {
                  "coord":{
                     "x":24.8044677066,
                     "y":60.1689215434
                  },
                  "arrTime":"201103080052",
                  "depTime":"201103080052",
                  "name":"Westendinasema"
               },
               {
                  "coord":{
                     "x":24.8259550043,
                     "y":60.1687894394
                  },
                  "arrTime":"201103080054",
                  "depTime":"201103080054",
                  "name":"Karhusaari"
               },
               {
                  "coord":{
                     "x":24.8422306971,
                     "y":60.1656262732
                  },
                  "arrTime":"201103080055",
                  "depTime":"201103080055",
                  "name":"Hanasaari"
               },
               {
                  "coord":{
                     "x":24.9134665043,
                     "y":60.1642300842
                  },
                  "arrTime":"201103080059",
                  "depTime":"201103080059",
                  "name":"L\u00e4nsiv\u00e4yl\u00e4"
               },
               {
                  "coord":{
                     "x":24.9267577712,
                     "y":60.1661035862
                  },
                  "arrTime":"201103080100",
                  "depTime":"201103080100",
                  "name":"Lapinrinne"
               },
               {
                  "coord":{
                     "x":24.9301475091,
                     "y":60.1687726522
                  },
                  "arrTime":"201103080101",
                  "depTime":"201103080101",
                  "name":"Kamppi, tulolaituri"
               }
            ]
         },
         {
            "length":156,
            "duration":108.9,
            "type":"walk",
            "locs":[
               {
                  "coord":{
                     "x":24.9301475091,
                     "y":60.1687726522
                  },
                  "arrTime":"201103080101",
                  "depTime":"201103080101",
                  "name":"Kamppi, tulolaituri"
               },
               {
                  "coord":{
                     "x":24.9293864314,
                     "y":60.1685499985
                  },
                  "arrTime":"201103080101",
                  "depTime":"201103080101",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.929467381,
                     "y":60.1684183813
                  },
                  "arrTime":"201103080101",
                  "depTime":"201103080101",
                  "name":"Runeberginkatu"
               },
               {
                  "coord":{
                     "x":24.9305257301,
                     "y":60.1681327014
                  },
                  "arrTime":"201103080102",
                  "depTime":"201103080102",
                  "name":null
               }
            ]
         }
      ]
   }
],
[
   {
      "length":12710.391,
      "duration":1080,
      "legs":[
         {
            "length":347,
            "duration":238.92,
            "type":"walk",
            "locs":[
               {
                  "coord":{
                     "x":24.7361910358,
                     "y":60.1606744939
                  },
                  "arrTime":"201103080052",
                  "depTime":"201103080052",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7362807305,
                     "y":60.1616254306
                  },
                  "arrTime":"201103080053",
                  "depTime":"201103080053",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7363367946,
                     "y":60.1616358902
                  },
                  "arrTime":"201103080054",
                  "depTime":"201103080054",
                  "name":"Markkinakatu"
               },
               {
                  "coord":{
                     "x":24.7355168722,
                     "y":60.1626627845
                  },
                  "arrTime":"201103080055",
                  "depTime":"201103080055",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.7362802203,
                     "y":60.1630525852
                  },
                  "arrTime":"201103080056",
                  "depTime":"201103080056",
                  "name":"Piispansilta"
               }
            ]
         },
         {
            "length":12206,
            "duration":780,
            "type":"5",
            "code":"2154T 2",
            "locs":[
               {
                  "coord":{
                     "x":24.7362802203,
                     "y":60.1630525852
                  },
                  "arrTime":"201103080056",
                  "depTime":"201103080056",
                  "name":"Piispansilta"
               },
               {
                  "coord":{
                     "x":24.7555826217,
                     "y":60.1659778169
                  },
                  "arrTime":"201103080057",
                  "depTime":"201103080057",
                  "name":"Matinsolmu"
               },
               {
                  "coord":{
                     "x":24.772475979,
                     "y":60.1674243176
                  },
                  "arrTime":"201103080057",
                  "depTime":"201103080057",
                  "name":"Haukilahdensolmu"
               },
               {
                  "coord":{
                     "x":24.8044677066,
                     "y":60.1689215434
                  },
                  "arrTime":"201103080059",
                  "depTime":"201103080059",
                  "name":"Westendinasema"
               },
               {
                  "coord":{
                     "x":24.8259550043,
                     "y":60.1687894394
                  },
                  "arrTime":"201103080100",
                  "depTime":"201103080100",
                  "name":"Karhusaari"
               },
               {
                  "coord":{
                     "x":24.8422306971,
                     "y":60.1656262732
                  },
                  "arrTime":"201103080101",
                  "depTime":"201103080101",
                  "name":"Hanasaari"
               },
               {
                  "coord":{
                     "x":24.8603953863,
                     "y":60.1618115562
                  },
                  "arrTime":"201103080103",
                  "depTime":"201103080103",
                  "name":"Katajaharju"
               },
               {
                  "coord":{
                     "x":24.8701734318,
                     "y":60.1610745409
                  },
                  "arrTime":"201103080103",
                  "depTime":"201103080103",
                  "name":"Lahnalahdentie"
               },
               {
                  "coord":{
                     "x":24.8762690753,
                     "y":60.1607112558
                  },
                  "arrTime":"201103080104",
                  "depTime":"201103080104",
                  "name":"Lahnalahden puisto"
               },
               {
                  "coord":{
                     "x":24.8840610166,
                     "y":60.1604800039
                  },
                  "arrTime":"201103080104",
                  "depTime":"201103080104",
                  "name":"Koillisv\u00e4yl\u00e4"
               },
               {
                  "coord":{
                     "x":24.8912987534,
                     "y":60.1610688341
                  },
                  "arrTime":"201103080105",
                  "depTime":"201103080105",
                  "name":"Lauttasaaren silta"
               },
               {
                  "coord":{
                     "x":24.9058315134,
                     "y":60.1636451749
                  },
                  "arrTime":"201103080106",
                  "depTime":"201103080106",
                  "name":"Salmisaari"
               },
               {
                  "coord":{
                     "x":24.9134665043,
                     "y":60.1642300842
                  },
                  "arrTime":"201103080107",
                  "depTime":"201103080107",
                  "name":"L\u00e4nsiv\u00e4yl\u00e4"
               },
               {
                  "coord":{
                     "x":24.9267577712,
                     "y":60.1661035862
                  },
                  "arrTime":"201103080108",
                  "depTime":"201103080108",
                  "name":"Lapinrinne"
               },
               {
                  "coord":{
                     "x":24.9301475091,
                     "y":60.1687726522
                  },
                  "arrTime":"201103080109",
                  "depTime":"201103080109",
                  "name":"Kamppi, tulolaituri"
               }
            ]
         },
         {
            "length":156,
            "duration":108.9,
            "type":"walk",
            "locs":[
               {
                  "coord":{
                     "x":24.9301475091,
                     "y":60.1687726522
                  },
                  "arrTime":"201103080109",
                  "depTime":"201103080109",
                  "name":"Kamppi, tulolaituri"
               },
               {
                  "coord":{
                     "x":24.9293864314,
                     "y":60.1685499985
                  },
                  "arrTime":"201103080109",
                  "depTime":"201103080109",
                  "name":null
               },
               {
                  "coord":{
                     "x":24.929467381,
                     "y":60.1684183813
                  },
                  "arrTime":"201103080109",
                  "depTime":"201103080109",
                  "name":"Runeberginkatu"
               },
               {
                  "coord":{
                     "x":24.9305257301,
                     "y":60.1681327014
                  },
                  "arrTime":"201103080110",
                  "depTime":"201103080110",
                  "name":null
               }
            ]
         }
      ]
   }
]
]
*/