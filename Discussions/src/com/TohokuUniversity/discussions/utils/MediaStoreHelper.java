package com.TohokuUniversity.discussions.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;

public class MediaStoreHelper {

	public static String getPathFromUri(final Context context, final Uri mediaStoreUri) {

		String[] projection = { MediaColumns.DATA };
		Cursor cursor = context.getContentResolver().query(mediaStoreUri, projection, null, null, null);
		String filePath;
		if (cursor.moveToFirst()) {
			int columnIndex = cursor.getColumnIndex(projection[0]);
			filePath = cursor.getString(columnIndex);
		} else {
			filePath = null;
		}
		cursor.close();
		return filePath;
	}

	public static String getTitleFromUri(final Context context, final Uri mediaStoreUri) {

		String[] projection = { MediaColumns.TITLE };
		Cursor cursor = context.getContentResolver().query(mediaStoreUri, projection, null, null, null);
		String mediaTitle;
		if (cursor.moveToFirst()) {
			int columnIndex = cursor.getColumnIndex(projection[0]);
			mediaTitle = cursor.getString(columnIndex);
		} else {
			mediaTitle = null;
		}
		cursor.close();
		return mediaTitle;
	}
}
