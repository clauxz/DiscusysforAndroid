package com.TohokuUniversity.discussions.data.model;

import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Discussions;

import android.content.ContentValues;
import android.database.Cursor;

public class Discussion implements Value {

	private final int id;
	private final String subject;

	public Discussion(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndexOrThrow(Discussions.Columns.ID);
			int subjectIndex = cursor.getColumnIndexOrThrow(Discussions.Columns.SUBJECT);
			id = cursor.getInt(idIndex);
			subject = cursor.getString(subjectIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public Discussion(final int id, final String subject) {

		super();
		this.id = id;
		this.subject = subject;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Discussions.Columns.ID, id);
		cv.put(Discussions.Columns.SUBJECT, subject);
		return cv;
	}

	@Override
	public String toMyString() {

		return toContentValues().toString();
	}
}
