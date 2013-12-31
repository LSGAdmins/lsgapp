package com.lsg.app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SMVBlog extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//((MainActivity) getActivity()).getSlideMenu().setFragment(SMVBlog.class);
		FrameLayout parent = (FrameLayout) Functions.webv.getParent();
		if (parent != null)
			parent.removeAllViews();
		return Functions.webv;
	}
	
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
		// set title
		getActivity().setTitle(R.string.smvblog);
		super.onActivityCreated(savedInstanceState);
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
	
//	@Override
//	public void onRefreshPress() {
//		Functions.webv.reload();
//	}
}