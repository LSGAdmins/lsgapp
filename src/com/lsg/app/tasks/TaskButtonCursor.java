package com.lsg.app.tasks;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

public class TaskButtonCursor implements Cursor {
	private int position = 0;
	private final int count = 3;
	public static final String type = "button";
	public static final String[] columns = new String[] { "_id", "type", "text" };
	private ArrayList<String> items = new ArrayList<String>();
	
	public TaskButtonCursor() {
		items.add("Schulaufgaben");
		items.add("Hausaufgaben");
		items.add("Noten");
	}

	@Override
	public byte[] getBlob(int columnIndex) {
		return null;
	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public int getColumnIndex(String columnName) {
		for(int i = 0; i < columns.length; i++) {
			if(columns[i].equals(columnName))
				return i;
		}
		return 0;
	}

	@Override
	public int getColumnIndexOrThrow(String columnName)
			throws IllegalArgumentException {
		for(int i = 0; i < columns.length; i++) {
			if(columns[i].equals(columnName))
				return i;
		}
		throw new IllegalArgumentException("row not present");
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columns[columnIndex];
	}

	@Override
	public String[] getColumnNames() {
		return columns;
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public double getDouble(int columnIndex) {
		return 0;
	}

	@Override
	public Bundle getExtras() {
		return null;
	}

	@Override
	public float getFloat(int columnIndex) {
		return 0;
	}

	@Override
	public int getInt(int columnIndex) {
		return 0;
	}

	@Override
	public long getLong(int columnIndex) {
		return 0;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public short getShort(int columnIndex) {
		return 0;
	}

	@Override
	public String getString(int columnIndex) {
		if (columnIndex == 1)
			return type;
		else
			return items.get(position);
	}

	@Override
	public int getType(int columnIndex) {
		return FIELD_TYPE_STRING;
	}

	@Override
	public boolean getWantsAllOnMoveCalls() {
		return false;
	}

	@Override
	public boolean isAfterLast() {
		return (position >= count) ? true : false;
	}

	@Override
	public boolean isBeforeFirst() {
		return (position < 0) ? true : false;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public boolean isFirst() {
		return (position == 0) ? true : false;
	}

	@Override
	public boolean isLast() {
		return (position == count - 1) ? true : false;
	}

	@Override
	public boolean isNull(int columnIndex) {
		if(columnIndex != 0)
			return true;
		else
			return false;
	}

	@Override
	public boolean move(int offset) {
		if(position + offset < count) {
			position += offset;
			return true;
		} else
			return false;
	}

	@Override
	public boolean moveToFirst() {
		position = 0;
		return true;
	}

	@Override
	public boolean moveToLast() {
		this.position = this.count - 1;
		return true;
	}

	@Override
	public boolean moveToNext() {
		if(position < count - 1) {
			position++;
			return true;
		} else
			return false;
	}

	@Override
	public boolean moveToPosition(int position) {
		if(position < count) {
			this.position = position;
			return true;
		} else
			return false;
	}
	//below all not needed

	@Override
	public void close() {
		// not needed
	}

	@Override
	public void copyStringToBuffer(int arg0, CharArrayBuffer arg1) {
		//not needed
	}

	@Override
	public void deactivate() {
		//not needed
	}
	
	@Override
	public boolean moveToPrevious() {
		// not needed
		return false;
	}

	@Override
	public void registerContentObserver(ContentObserver observer) {
		// not needed

	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// not needed

	}

	@Override
	public boolean requery() {
		// not needed
		return false;
	}

	@Override
	public Bundle respond(Bundle extras) {
		// not needed
		return null;
	}

	@Override
	public void setNotificationUri(ContentResolver cr, Uri uri) {
		// not needed
	}

	@Override
	public void unregisterContentObserver(ContentObserver observer) {
		// not needed
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// not needed
	}
}
