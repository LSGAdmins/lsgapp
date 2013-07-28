package com.lsg.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lsg.app.lib.TitleCompat;
import com.lsg.app.lib.TitleCompat.HomeCall;
import com.lsg.app.lib.TitleCompat.RefreshCall;
import com.lsg.lib.slidemenu.SlideMenu;

public class SMVBlog extends Fragment implements HomeCall, RefreshCall {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		((MainActivity) getActivity()).getSlideMenu().setFragment(SMVBlog.class);
		FrameLayout parent = (FrameLayout) Functions.webv.getParent();
		if (parent != null)
			parent.removeAllViews();
		return Functions.webv;
	}
	private SlideMenu slidemenu;
	private TitleCompat titlebar;
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		getActivity().setProgressBarVisibility(true);
		if(Functions.webv == null)
		  Functions.init(getActivity());
		super.onCreate(savedInstanceState);
		Functions.webv.getSettings().setJavaScriptEnabled(true);
		Functions.webv.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (getActivity() != null)
					getActivity().setProgress(progress * 1000);
				}
			});
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		slidemenu = ((MainActivity) getActivity()).getSlideMenu();
		slidemenu.setFragment(SMVBlog.class);
//		titlebar = ((MainActivity) getActivity()).getTitlebar();
//		titlebar.addRefresh(this);
		getActivity().setTitle(R.string.smvblog);
//		titlebar.setTitle(getActivity().getTitle());
		super.onActivityCreated(savedInstanceState);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
        case android.R.id.home:
            onHomePress();
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("dest", "ondestroy");
		// Functions.webv.saveState(Functions.webvSave);
		try {
			FrameLayout par = (FrameLayout) Functions.webv.getParent();
			par.removeAllViewsInLayout();
			LinearLayout parpar = (LinearLayout) par.getParent();
			parpar.removeAllViews();
		} catch (Exception e) {
		}
	}
	@Override
	public void onHomePress() {
		slidemenu.show();
	}
	@Override
	public void onRefreshPress() {
		Functions.webv.reload();
	}
}