package com.lsg.app.drawer;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lsg.app.Functions;
import com.lsg.app.R;

public class DrawerAdapter extends ArrayAdapter<DrawerAdapter.MenuDesc> {
	Activity act;
	DrawerAdapter.MenuDesc[] items;
	int selected;

	class MenuItem {
		public TextView label;
		public TextView title;
		public ImageView icon;
	}

	public static class MenuDesc {
		public boolean useSlideMenu = true;
		public int type = Functions.TYPE_PAGE;
		public int icon;
		public String label;
		public String title;
		public Class<? extends Activity> openActivity;
		public Class<? extends Fragment> openFragment;
		public Class<? extends Activity> containerActivity;
		public Intent openIntent;
		public boolean selected;
	}

	public DrawerAdapter(Activity act, DrawerAdapter.MenuDesc[] items) {
		super(act, R.id.menu_label, items);
		this.act = act;
		this.items = items;
	}

	@Override
	public int getItemViewType(int position) {
		return items[position].type;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		if (rowView == null
				|| (items[position].type == Functions.TYPE_INFO && rowView
						.findViewById(R.id.title) == null)
				|| (items[position].type == Functions.TYPE_PAGE && rowView
						.findViewById(R.id.title) != null)) {
			LayoutInflater inflater = act.getLayoutInflater();
			MenuItem viewHolder = new MenuItem();
			switch (getItemViewType(position)) {
			case Functions.TYPE_PAGE:
				rowView = inflater.inflate(R.layout.menu_listitem, null);
				viewHolder.title = null;
				break;
			case Functions.TYPE_INFO:
				rowView = inflater.inflate(R.layout.menu_info, null);
				viewHolder.title = (TextView) rowView.findViewById(R.id.title);
				break;
			}
			viewHolder.icon = (ImageView) rowView.findViewById(R.id.menu_icon);
			viewHolder.label = (TextView) rowView.findViewById(R.id.menu_label);
			rowView.setTag(viewHolder);
		}

		MenuItem holder = (MenuItem) rowView.getTag();
		String s = items[position].label;
		holder.label.setText(s);
		holder.icon.setImageResource(items[position].icon);

		if (items[position].selected) {
			rowView.setBackgroundColor(act.getResources().getColor(
					R.color.activeblack));
			selected = position;
		}
		else
			rowView.setBackgroundDrawable(null);

		if (holder.title != null) {
			if (items[position].title != null) {
				holder.title.setText(items[position].title);
				holder.title.setVisibility(View.VISIBLE);
			} else
				holder.title.setVisibility(View.GONE);
		}

		return rowView;
	}
	public void clearSelection() {
		items[selected].selected = false;
	}
}
