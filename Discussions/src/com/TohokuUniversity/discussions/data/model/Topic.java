package com.TohokuUniversity.discussions.data.model;

import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Topics;

import android.content.ContentValues;
import android.database.Cursor;

public class Topic implements Value {

	private final int discussionId;
	private final int id;
	private final String name;

	public Topic(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			// FIXME: show related persons here
			int discussionIdIndex = cursor.getColumnIndexOrThrow(Topics.Columns.DISCUSSION_ID);
			int idIndex = cursor.getColumnIndexOrThrow(Topics.Columns.ID);
			int nameIndex = cursor.getColumnIndexOrThrow(Topics.Columns.NAME);
			discussionId = cursor.getInt(discussionIdIndex);
			id = cursor.getInt(idIndex);
			name = cursor.getString(nameIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public Topic(final int discussionId, final int id, final String name) {

		super();
		this.discussionId = discussionId;
		this.id = id;
		this.name = name;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Topics.Columns.DISCUSSION_ID, discussionId);
		cv.put(Topics.Columns.ID, id);
		cv.put(Topics.Columns.NAME, name);
		return cv;
	}

	@Override
	public String toMyString() {

		StringBuilder sb = new StringBuilder();
		sb.append(Topics.Columns.DISCUSSION_ID).append(':').append(discussionId).append('\n');
		sb.append(Topics.Columns.ID).append(':').append(id).append('\n');
		sb.append(Topics.Columns.NAME).append(':').append(name).append('\n');
		return sb.toString();
	}
}
