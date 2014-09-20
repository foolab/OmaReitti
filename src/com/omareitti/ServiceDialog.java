package com.omareitti;

import com.omareitti.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ServiceDialog extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Utils.getForegroundActivity(this);
		setContentView(R.layout.servicedialog);
		
		Log.i("ServiceDialog!!!!", "onCreate:"+getIntent().getExtras());
		
		Bundle b = getIntent().getExtras();
        if (b == null) {
        	finish();
        	return;
        }
        String caption =	b.getString("caption");
        String text =		b.getString("text");
        
        String action = b.getString("action");
        
        showErrorDialog(caption, text, action);
	}
	
    private void showErrorDialog(String title, String message, String action) {
		AlertDialog alertDialog = new AlertDialog.Builder(ServiceDialog.this).create();
	    alertDialog.setTitle(title);
	    alertDialog.setMessage(message);
	    

	    alertDialog.setButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int which) {
		        /*Intent setIntent = new Intent(Intent.ACTION_MAIN); 
		        setIntent.addCategory(Intent.CATEGORY_HOME); 
		        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		        startActivity(setIntent);*/
		        dialog.dismiss();
		        ServiceDialog.this.finish();
	    } });
	    alertDialog.show();	
    }	
}
