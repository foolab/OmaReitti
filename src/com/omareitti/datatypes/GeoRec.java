package com.omareitti.datatypes;

public class GeoRec {
	public String name;
	public String coords;	
	public String lang;
	public String locType, locTypeId, city;
	
	public String json;
	
	public String toString() {
		return ""+name+" "+coords+" "+lang+" "+locType+" "+locTypeId+" "+city;
	}
}
