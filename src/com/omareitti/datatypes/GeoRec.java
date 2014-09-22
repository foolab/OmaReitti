package com.omareitti.datatypes;

import com.omareitti.datatypes.Coords;

public class GeoRec {
    public String name;
    public Coords coords;
    public String lang;
    public String locType, locTypeId, city;
    public String json;

    public String toString() {
	return ""+name+" "+coords+" "+lang+" "+locType+" "+locTypeId+" "+city;
    }
}
