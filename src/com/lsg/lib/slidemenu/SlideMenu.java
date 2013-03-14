package com.lsg.lib.slidemenu;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lsg.app.Functions;
import com.lsg.app.R;
import com.lsg.app.widget.CustomFrameLayout;

public class SlideMenu implements OnTouchListener {

	private static boolean menuShown = false;
	public static boolean menuToHide = false;
	private static View menu;
	private static LinearLayout contentContainer;
	private static FrameLayout decorView;
	private static int menuSize;
	private static int statusBarHeight = 0;
	private Activity act;
	private int slideRightMargin;
	private static Class<? extends Fragment> fragment;

	public SlideMenu(Activity act) {
		this.act = act;
		contentContainer = ((LinearLayout) act.findViewById(
				android.R.id.content).getParent());

		decorView = (FrameLayout) contentContainer.getParent();

		(act.findViewById(android.R.id.content)).setBackgroundDrawable(decorView
				.getBackground());
		
		LayoutInflater inflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (!act.getResources().getBoolean(R.bool.isTablet)) {
			menu = inflater.inflate(R.layout.slidemenu_layout, null);
			menuLayoutParams = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.MATCH_PARENT, 3);
			menuLayoutParams.setMargins(20000, 20000, 0, 0);
			menu.setLayoutParams(menuLayoutParams);
			menu.getViewTreeObserver().addOnGlobalLayoutListener(
					new ViewTreeObserver.OnGlobalLayoutListener() {
						@SuppressWarnings("deprecation")
						@Override
						public void onGlobalLayout() {
							menu.getViewTreeObserver()
									.removeGlobalOnLayoutListener(this);
							if (menuToHide) {
								hide();
								menuToHide = false;
							}
						}
					});
			menuSize = Functions.dpToPx(250, act);
			Display display = act.getWindowManager().getDefaultDisplay(); 
			slideRightMargin = display.getWidth() - menuSize;  // deprecated
		}

		decorView.removeAllViews();
		FrameLayout.LayoutParams parentLays = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		CustomFrameLayout parent = new CustomFrameLayout(act);
		parent.setLayoutParams(parentLays);
		decorView.addView(parent);

		FrameLayout.LayoutParams contentLays = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		contentContainer.setLayoutParams(contentLays);
		// menu added before content, to have in back
		if (!act.getResources().getBoolean(R.bool.isTablet))
			parent.addView(menu);
		parent.addView(contentContainer);

		Fragment menuFrag = new SlideMenuFragment();
		FragmentTransaction fragmentTransaction = ((FragmentActivity) act)
				.getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.slideMenuContainer, menuFrag);
		fragmentTransaction.commit();

		if (!act.getResources().getBoolean(R.bool.isTablet)) {
			parent.setOnTouchListener(this);
			parent.setOnTouchIntercept(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					motionStartX = event.getX();
					// check if menu is opened, and the user is dragging to
					// close
					// the menu
					if (event.getAction() == MotionEvent.ACTION_DOWN
							&& event.getX() > menuSize
							&& menuShown) {
						// prepare for slidein
						contentContainerLayoutParams = (FrameLayout.LayoutParams) contentContainer
								.getLayoutParams();
						contentContainerLayoutParams.setMargins(0, 0, 0, 0);
						contentContainer
								.setLayoutParams(contentContainerLayoutParams);
						contentContainer.scrollTo(-menuSize, 0);
						return true;
					} else
						return false;
				}
			});
		}
		checkShown();
	}

	public void checkShown() {
		if (menuShown)
			this.show(false);
	}

	public boolean handleBack() {
		if (menuShown) {
			hide();
			return true;
		}
		return false;
	}

	public void setFragment(Class<? extends Fragment> fragment) {
		SlideMenu.fragment = fragment;
		
	}

	public void show() {
		if (statusBarHeight == 0) {
			Rect rectgle = new Rect();
			Window window = act.getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
			statusBarHeight = rectgle.top;
		}
		this.show(true, 0);
	}

	public void show(boolean animate) {
		show(animate, 0);
	}

	// LayoutParams to move content & menu around
	private FrameLayout.LayoutParams contentContainerLayoutParams;
	private FrameLayout.LayoutParams menuLayoutParams;
	// store motion events
	private float motionStartX;
	private float lastX;
	private float previousX;
	private int maxDiff = 0;
	private int lastDiff;

	public void show(boolean animate, int offset) {
		if (act.getResources().getBoolean(R.bool.isTablet))
			return;
		if (offset == 0)
			offset = menuSize;

		// move content & ActionBar out to right
		contentContainerLayoutParams = (FrameLayout.LayoutParams) contentContainer
				.getLayoutParams();
		contentContainerLayoutParams.setMargins(menuSize, 0, -menuSize, 0);
		contentContainerLayoutParams.gravity = Gravity.TOP;
		contentContainer.setLayoutParams(contentContainerLayoutParams);

		// set menu to left side
		menuLayoutParams = new FrameLayout.LayoutParams(-1, -1, 3);
		menuLayoutParams.setMargins(0, statusBarHeight, slideRightMargin, 0);
		menu.setLayoutParams(menuLayoutParams);

		if (animate) {
			// slide out content
			TranslateAnimation contentSlideOut = new TranslateAnimation(
					-offset, 0, 0, 0);
			contentSlideOut.setDuration(Math.abs(offset) * 500 / menuSize);
			contentContainer.startAnimation(contentSlideOut);

			// slide in menu
			TranslateAnimation menuSlideIn = new TranslateAnimation(
					-(offset / 2), 0, 0, 0);
			menuSlideIn.setDuration(Math.abs(offset) * 500 / menuSize);
			menu.startAnimation(menuSlideIn);
		}
		menuShown = true;
	}

	public void hide() {
		hide(0);
	}

	public void hide(int offset) {
		if (act.getResources().getBoolean(R.bool.isTablet))
			return;
		// slide out menu to left
		TranslateAnimation menuSlideOut = new TranslateAnimation(
				-((offset) / 2), -((menuSize) / 2), 0, 0);
		menuSlideOut.setDuration(Math.abs(menuSize - offset) * 500 / menuSize);
		menu.startAnimation(menuSlideOut);

		// slide in content from right
		TranslateAnimation contentSlideIn = new TranslateAnimation(menuSize
				- offset, 0, 0, 0);
		contentSlideIn
				.setDuration(Math.abs(menuSize - offset) * 500 / menuSize);
		contentContainer.startAnimation(contentSlideIn);

		contentContainerLayoutParams = (FrameLayout.LayoutParams) contentContainer
				.getLayoutParams();
		contentContainerLayoutParams.setMargins(0, 0, 0, 0);
		contentContainer.setLayoutParams(contentContainerLayoutParams);

		menuShown = false;

		menuSlideOut.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// not needed here
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// not needed here
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// move menu out of visible scope
				menuLayoutParams = (FrameLayout.LayoutParams) menu
						.getLayoutParams();
				menuLayoutParams.setMargins(20000, 20000, 0, 0);
				menu.setLayoutParams(menuLayoutParams);
			}
		});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			previousX = lastX;
			lastX = event.getX();
			int positionDiff = Float.valueOf(motionStartX - lastX).intValue();
			if (positionDiff < 0)
				positionDiff = 0;
			if (lastDiff == positionDiff || lastDiff - 1 == positionDiff)
				break;
			lastDiff = positionDiff;
			if (lastDiff < maxDiff)
				maxDiff = lastDiff;
			contentContainer.scrollTo(-menuSize + positionDiff, 0);
			menu.scrollTo(positionDiff / 2, 0);
			break;
		case MotionEvent.ACTION_UP:
			contentContainer.scrollTo(0, 0);
			menu.scrollTo(0, 0);
			if (previousX < event.getX() && lastDiff > Functions.dpToPx(5, act)) {
				show(true, lastDiff);
			} else
				hide(lastDiff);
			break;
		}
		return true;
	}

	public Class<? extends Fragment> getFragment() {
		return fragment;
	}

	public void tabletHide() {
		if (act.getResources().getBoolean(R.bool.isTablet)) {
			act.findViewById(R.id.slideMenuContainer).setVisibility(View.GONE);
		}
	}

	public void tabletShow() {
		if (act.getResources().getBoolean(R.bool.isTablet)) {
			act.findViewById(R.id.slideMenuContainer).setVisibility(
					View.VISIBLE);
		}
	}
}
