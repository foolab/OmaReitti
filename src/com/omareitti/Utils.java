package com.omareitti;

import java.util.List;
import java.util.UUID;

import com.omareitti.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Utils {

	public static void addHomeScreenShortcut(Context context, String name, String fromAddress, String toAddress, String fromCoords, String toCoords) { 
		Intent shortcutIntent = new Intent();
		
		shortcutIntent.setClassName(context, context.getClass().getName());
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);			
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		if (toAddress != null) shortcutIntent.putExtra("toAddress", toAddress);
		if (fromAddress != null) shortcutIntent.putExtra("fromAddress", fromAddress);

		if (toCoords != null) shortcutIntent.putExtra("toCoords", toCoords);
		if (fromCoords != null) shortcutIntent.putExtra("fromCoords", fromCoords);
		
		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);

		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(context,
						R.drawable.icon));

		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		context.sendBroadcast(intent);		
	}
	

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter(); 
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    
    public static String getUniqueID(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, tmPhone, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        return deviceId;
    }
    /*public static ComponentName getForegroundActivity(Context context) {
    	// get a list of running processes and iterate through them
		ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);

		
		// get the info from the currently running task
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);

		Log.d("current task :", "CURRENT Activity ::"
				+ taskInfo.get(0).topActivity.getClassName());

		ComponentName componentInfo = taskInfo.get(0).topActivity; 
		return componentInfo;
    }*/
}
