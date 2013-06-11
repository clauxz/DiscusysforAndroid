package jp.ac.tohoku.qse.takahashi.discussions.data.model;

import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

public class Comment implements Value {

	private int id;
	private final Integer personId;
	private final Integer pointId;
	private final String text;

	public Comment() {

		id = Integer.MIN_VALUE;
		text = "";
		personId = Integer.MIN_VALUE;
		pointId = Integer.MIN_VALUE;
	}

	public Comment(final Bundle commentBundle) {

		id = commentBundle.getInt(Comments.Columns.ID, Integer.MIN_VALUE);
		text = commentBundle.getString(Comments.Columns.TEXT);
		personId = commentBundle.getInt(Comments.Columns.PERSON_ID, Integer.MIN_VALUE);
		pointId = commentBundle.getInt(Comments.Columns.POINT_ID, Integer.MIN_VALUE);
	}

	public Comment(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
			int textIndex = cursor.getColumnIndexOrThrow(Comments.Columns.TEXT);
			int personIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.PERSON_ID);
			int pointIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.POINT_ID);
			personId = cursor.getInt(personIdIndex);
			id = cursor.getInt(idIndex);
			text = cursor.getString(textIndex);
			pointId = cursor.getInt(pointIdIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public int getId() {

		return id;
	}

	public Integer getPersonId() {

		return personId;
	}

	public Integer getPointId() {

		return pointId;
	}

	public String getText() {

		return text;
	}

	public void setId(final int id) {

		this.id = id;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Comments.Columns.ID, id);
		cv.put(Comments.Columns.TEXT, text);
		cv.put(Comments.Columns.PERSON_ID, personId);
		cv.put(Comments.Columns.POINT_ID, pointId);
		return cv;
	}

	@Override
	public String toMyString() {

		return toContentValues().toString();
	}
}
