package com.lsg.app;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TitlePager {
	private LinearLayout titles[];
	private Context context;
	private FrameLayout titlecontainer;
	private ViewPager pager;
	TitlePager(PagerTitles pageradap, final FrameLayout container, Context ctx,  ViewPager pgr) {
		pager = pgr;
		context = ctx;
		titlecontainer = container;
		titles = new LinearLayout[pageradap.getCount()];
		final int pos = pager.getCurrentItem();
		for(int i = 0; i < pageradap.getCount(); i++) {
			TextView tv = new TextView(ctx);
			tv.setText(pageradap.getTitle(i));
			tv.setTextColor(ctx.getResources().getColor(R.color.darkblue));
			tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			titles[i] = new LinearLayout(ctx);
			titles[i].addView(tv);
			titles[i].setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
			container.addView(titles[i]);
		}
		//titlecontainer rendered -> position titles
		container.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						moveViewPagerTitles(pos, 0, true);
						}
					});
		}
	public void moveViewPagerTitles(int position, int offsetPixels) {
		moveViewPagerTitles(position, offsetPixels, false);
	}
	public void moveViewPagerTitles(int position, int offsetPixels, boolean animate) {
		int offsetCP = offsetPixels;
		int size = /*(titlecontainer.getWidth() == 0) ? 320 : */titlecontainer.getWidth();
		FrameLayout.LayoutParams[] lp = new FrameLayout.LayoutParams[titles.length];
		int[][] offsets = new int[titles.length][2];
		int i = 0;
		while(i < titles.length) {
			offsetPixels = offsetCP;
			if(position <= i - 1 && offsetCP > 0) {
				Log.d("position", Integer.valueOf(i).toString());
				offsetPixels = offsetCP - pager.getPageMargin();
				//Log.d("pos", new Integer().toString());
			}
			lp[i] = new FrameLayout.LayoutParams(titles[i].getLayoutParams());
			offsets[i][0] = size * position + offsetPixels + -i * size;
			offsets[i][1] = (size - titles[i].getWidth()) / 2;

			TextView v = (TextView) ((ViewGroup) titles[i]).getChildAt(0);
			v.setTextColor(context.getResources().getColor(R.color.inactivegrey));
			if(offsets[i][0] > offsets[i][1] && offsets[i][0] < 3 * offsets[i][1]) {
				offsets[i][0] = offsets[i][1];
			} else if(offsets[i][0] > 3* offsets[i][1]) {
				offsets[i][0] = offsets[i][1] + (offsets[i][0] - 3 * offsets[i][1]);
			} else if(offsets[i][0] < - offsets[i][1] && offsets[i][0] > - 3 * offsets[i][1] + 4) {
				offsets[i][0] = - offsets[i][1];
			} else if(offsets[i][0] <= -3 * offsets[i][1] + 3) {
				offsets[i][0] = - offsets[i][1] + (offsets[i][0] + 3 * offsets[i][1] - 4);
			} else
				v.setTextColor(context.getResources().getColor(R.color.darkblue));
			if(animate) {
				TranslateAnimation ta = new TranslateAnimation(-(offsets[i][1] - offsets[i][0]), 0, 0, 0);
				ta.setDuration(1000);
				titles[i].startAnimation(ta);
				}
			lp[i].setMargins(offsets[i][1] - offsets[i][0], 0, offsets[i][1] + offsets[i][0], 0);
			titles[i].setLayoutParams(lp[i]);
			i++;
		}
	}
}
