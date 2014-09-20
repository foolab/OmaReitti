package com.omareitti;

import com.omareitti.IBackgroundServiceListener;

interface IBackgroundServiceAPI {

	int requestLastKnownAddress(int getAddress);
	
	boolean isGPSOn();

	void setRoute(String route, boolean remindDep, boolean remindArr);
	void cancelRoute(int notify);
	
	void addListener(IBackgroundServiceListener l);
	void removeListener(IBackgroundServiceListener l); 
}