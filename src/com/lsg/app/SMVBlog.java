package com.lsg.app;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class SMVBlog extends Activity {
	private SlideMenu slidemenu;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("smv", "oncreate");
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		Functions.setTheme(false, true, this);
		
		WebView webview = new WebView(this);
		setContentView(webview);
		webview.getSettings().setJavaScriptEnabled(true);
		final Activity activity = this;
		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				activity.setProgress(progress*1000);
				}
			});
		webview.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Toast.makeText(activity, getString(R.string.oops) + " " + description, Toast.LENGTH_SHORT).show();
				}
			});
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String postData = "log=" + prefs.getString("username", "")
				+ "&pwd=" + prefs.getString("password", "") + "&redirect_to=http://www.lsg.musin.de/smv/aktuelles/";
		if(Functions.getSDK() >= 5) {
			AdvancedWrapper advWrapper = new AdvancedWrapper();
			advWrapper.postUrl(webview, "http://www.lsg.musin.de/smv/login/?action=login", EncodingUtils.getBytes(postData, "BASE64"));
			advWrapper = null;
		}
		else
			webview.loadUrl("http://www.lsg.musin.de/smv/login/?action=login");
		Log.d("SMVBlog", "load");
		slidemenu = new SlideMenu(this, 3);
		slidemenu.checkEnabled();
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig){        
	    super.onConfigurationChanged(newConfig);
	    Log.d("config", "change: " + newConfig.toString());
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        case android.R.id.home:
            slidemenu.show();
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}