package com.lsg.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class lsgapp extends ListActivity {
	Download down;
	final Handler handler = new Handler();
    class UpdateCheck extends Thread {
    	Handler handler;
    	public UpdateCheck(Handler h) {
    		handler = h;
    	}
    	public void run() {
    		Looper.prepare();
        	String get = "";
    		try {
    			String data = "";
            	URL url = new URL("http://linux.lsg.musin.de/cp/checkUpdate.php?version=" + getString(R.string.versioncode));
            	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            	// If you invoke the method setDoOutput(true) on the URLConnection, it will always use the POST method.
            	conn.setDoOutput(true);
            	conn.setRequestMethod("POST");
            	OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            	wr.write(data);
            	wr.flush();
            	wr.close();
            	//get response
            	InputStream response = conn.getInputStream();
            	BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            	String line;
            	while ((line = reader.readLine()) != null) {
            		get += line;
            		}
            	try {
            		JSONObject jObject = new JSONObject(get);
            		if(!jObject.getBoolean("act")) {
            			Log.d("asdf", "notact");
            			final String actVersion = jObject.getString("actversion");
            			final String changelog  = jObject.getString("changelog");
            			Runnable notify = new Runnable() {
            				public void run() {
            					AlertDialog.Builder builder = new AlertDialog.Builder(lsgapp.this);
            					builder.setMessage(getString(R.string.update_available) + '\n' + actVersion + ": "+ '\n' + changelog)
            					       .setCancelable(false)
            					       .setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
            					           public void onClick(DialogInterface dialog, int id) {
            					       		Toast.makeText(lsgapp.this, getString(R.string.downloading), Toast.LENGTH_LONG).show();
            					        	   if(Build.VERSION.SDK_INT >= 9) {
            					        		   down.download();
            					        	   }
            					        	   else {
            					        		   Intent intent = new Intent(Intent.ACTION_VIEW);
            					        		   intent.setData(Uri.parse(Functions.UPDATE_URL));
            					        		   startActivity(intent);
            					        	   }
            					           }
            					       })
            					       .setNegativeButton(getString(R.string.no_update), new DialogInterface.OnClickListener() {
            					           public void onClick(DialogInterface dialog, int id) {
            					                dialog.cancel();
            					           }
            					       });
            					AlertDialog alert = builder.create();
            					alert.show();
            					}
            			};
            			handler.post(notify);
            		}
            		} catch(JSONException e) {Log.d("json", e.getMessage()); Log.d("asdf", e.getMessage());}
            }
            catch(Exception e) {
    	    	Log.d("except", e.getMessage());
            }
    	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Functions.setTheme(false, false, this);
		getWindow().setBackgroundDrawableResource(R.layout.background);
        super.onCreate(savedInstanceState);
        String[] actions = {getString(R.string.vplan), getString(R.string.events), getString(R.string.smvblog)/*,
        		getString(R.string.updatecheck)*/};
        setListAdapter(new ArrayAdapter<String>(this, R.layout.main_listitem, actions));
        Functions.styleListView(getListView(), this);
        
        if(Build.VERSION.SDK_INT >= 9)
 		   down = new Download(lsgapp.this);
        
        UpdateCheck uCheck = new UpdateCheck(handler);
        uCheck.start();
    }
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String listtext = (String) ((TextView) v).getText();
		if(listtext.equals(getString(R.string.vplan))) {
			Intent intent = new Intent(this, VPlan.class);
			startActivity(intent);
		}
		if(listtext.equals(getString(R.string.events))) {
			Intent intent = new Intent(this, Events.class);
			startActivity(intent);
		}
		if(listtext.equals(getString(R.string.smvblog))) {
			Intent intent = new Intent(this, SMVBlog.class);
			startActivity(intent);
		}
		if(listtext.equals(getString(R.string.updatecheck))) {
			Intent intent = new Intent(this, UpdateCheck.class);
			startActivity(intent);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.lsgapp, menu);
	    return true;
	}
	@Override
	public void onPause() {
		super.onPause();
		if(Build.VERSION.SDK_INT >= 9)
			unregisterReceiver(down.downloadReceiver);
	}
	@Override
	public void onResume() {
		super.onResume();
		if(Build.VERSION.SDK_INT >= 9) {
			IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
			registerReceiver(down.downloadReceiver, intentFilter);
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.help:
	    	Intent help = new Intent(this, HelpAbout.class);
	    	help.putExtra(Functions.helpabout, Functions.help);
	    	startActivity(help);
	        return true;
	    case R.id.about:
	    	Intent about = new Intent(this, HelpAbout.class);
	    	about.putExtra(Functions.helpabout, Functions.about);
	    	startActivity(about);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}