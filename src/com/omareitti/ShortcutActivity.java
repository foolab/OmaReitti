package com.omareitti;

import java.util.ArrayList;

import com.omareitti.R;
import com.omareitti.History.HistoryItem;
import com.omareitti.History.RouteHistoryItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TableLayout;

public class ShortcutActivity extends Activity {

    ArrayList<HistoryItem> history;
    ArrayList<RouteHistoryItem> routes;
    private int lastSelectedHistory = -1;
    ListView myPlaces, myRoutes;
    
    History.HistoryAdapter historyAdapter;
    History.RoutesAdapter routesAdapter;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainapp);
		
		TableLayout t1 = (TableLayout)findViewById(R.id.tableLayout1);
		TableLayout t2 = (TableLayout)findViewById(R.id.tableLayout2);
		TableLayout t3 = (TableLayout)findViewById(R.id.tableLayout3);
		
		t1.setVisibility(View.GONE);
		t2.setVisibility(View.GONE);
		t3.setVisibility(View.GONE);
		
        TabHost tabs = (TabHost)findViewById(R.id.TabHost01);

        tabs.setup();

        TabHost.TabSpec spec1 = tabs.newTabSpec("tag1");

        spec1.setContent(R.id.myPlacesList);
        spec1.setIndicator(getString(R.string.maTabPlaces), getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
        
        tabs.addTab(spec1);

        TabHost.TabSpec spec2 = tabs.newTabSpec("tag2");
        spec2.setContent(R.id.myRoutesList);
        spec2.setIndicator(getString(R.string.maTabRoutes), getResources().getDrawable(android.R.drawable.ic_menu_myplaces));

        tabs.addTab(spec2);

        myPlaces = (ListView)findViewById(R.id.myPlacesList);
        myRoutes = (ListView)findViewById(R.id.myRoutesList);

        history = History.getHistory(this);
        routes = History.getRoutes(this);
        historyAdapter = new History.HistoryAdapter(this);
        routesAdapter = new History.RoutesAdapter(this);
        myPlaces.setAdapter(historyAdapter);
        myRoutes.setAdapter(routesAdapter);
        Utils.setListViewHeightBasedOnChildren((ListView)findViewById(R.id.myPlacesList));
        Utils.setListViewHeightBasedOnChildren((ListView)findViewById(R.id.myRoutesList));
        
        myPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
					HistoryItem h = history.get(arg2);
					
					String str = h.address; 
			    	if (!h.name.equals("")) str = h.name;
			    	
					createShortcut(str, null, h.address);
				}
		});
        
        myRoutes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				RouteHistoryItem r = routes.get(arg2);
				String n1 = r.start.substring(0, 5); if (r.start.length() > 0) n1 +=".";
				String n2 = r.end;//.substring(0, 5); if (r.end.length() > 0) n2 +=".";
				Utils.addHomeScreenShortcut(ShortcutActivity.this, n1+"-"+n2, r.start, r.end, r.coords, r.coords2);
			}
		});		
	}

	public void createShortcut(String name, String fromAddress, String toAddress) {
        // The meat of our shortcut
        Intent shortcutIntent = new Intent();//"com.omareitti.MainApp");
        shortcutIntent.setClassName(getPackageName(), MainApp.class.getName());
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
		if (toAddress != null) shortcutIntent.putExtra("toAddress", toAddress);
		if (fromAddress != null) shortcutIntent.putExtra("fromAddress", fromAddress);
		
        // The result we are passing back from this activity
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));
        

        setResult(RESULT_OK, intent);
 
        finish(); // Must call finish for result to be returned immediately				
	}
}
