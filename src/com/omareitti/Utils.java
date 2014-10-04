package com.omareitti;

import java.util.UUID;

import com.omareitti.datatypes.Coords;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class Utils {
    public static void addHomeScreenShortcut(Context context, String name,
					     String fromAddress, String toAddress,
					     Coords fromCoords,
					     Coords toCoords) {

	Intent shortcutIntent = new Intent();
	shortcutIntent.setClassName(context, context.getClass().getName());
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

	if (toAddress != null)
	    shortcutIntent.putExtra("toAddress", toAddress);

	if (fromAddress != null)
	    shortcutIntent.putExtra("fromAddress", fromAddress);

	if (toCoords != null)
	    shortcutIntent.putExtra("toCoords", toCoords.toString());
	if (fromCoords != null)
	    shortcutIntent.putExtra("fromCoords", fromCoords.toString());

	Intent intent = new Intent();
	intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);

	intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
			Intent.ShortcutIconResource.fromContext(context, R.drawable.icon));

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
}
