package com.omareitti;

interface IBackgroundServiceListener {
	void handleUpdate(String s);
	void handleGPSUpdate(double lat, double lon, float angle);
	
	void locationDiscovered(double lat, double lon);
	void addressDiscovered(String address);
}