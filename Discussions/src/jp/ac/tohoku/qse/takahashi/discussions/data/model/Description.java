package jp.ac.tohoku.qse.takahashi.discussions.data.model;

import jp.ac.tohoku.qse.takahashi.discussions.data.DataIoException;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Descriptions;


import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

public class Description implements Value {

	private final Integer discussionId;
	private int id;
	private Integer pointId;
	private final String text;

	public Description(final Bundle bundle) {

		super();
		id = bundle.getInt(Descriptions.Columns.ID);
		text = bundle.getString(Descriptions.Columns.TEXT);
		if (bundle.containsKey(Descriptions.Columns.POINT_ID)) {
			pointId = bundle.getInt(Descriptions.Columns.POINT_ID);
		} else {
			pointId = null;
		}
		if (bundle.containsKey(Descriptions.Columns.DISCUSSION_ID)) {
			discussionId = bundle.getInt(Descriptions.Columns.DISCUSSION_ID);
		} else {
			discussionId = null;
		}
		if ((discussionId == null) && (pointId == null)) {
			throw new DataIoException("Both foreign key cant be null");
		}
	}

	public Description(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndexOrThrow(Descriptions.Columns.ID);
			int textIndex = cursor.getColumnIndexOrThrow(Descriptions.Columns.TEXT);
			int discussionIdIndex = cursor.getColumnIndexOrThrow(Descriptions.Columns.DISCUSSION_ID);
			int pointIdIndex = cursor.getColumnIndexOrThrow(Descriptions.Columns.POINT_ID);
			discussionId = cursor.getInt(discussionIdIndex);
			id = cursor.getInt(idIndex);
			text = cursor.getString(textIndex);
			pointId = cursor.getInt(pointIdIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public Description(final int id, final String text, final Integer discussionId, final Integer pointId) {

		super();
		if ((discussionId == null) && (pointId == null)) {
			throw new DataIoException("Both foreign key cant be null");
		}
		this.id = id;
		this.text = text;
		this.discussionId = discussionId;
		this.pointId = pointId;
	}

	public Integer getDiscussionId() {

		return discussionId;
	}

	public int getId() {

		return id;
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

	public void setPointId(final Integer pointId) {

		this.pointId = pointId;
	}

	public Bundle toBundle() {

		Bundle bundle = new Bundle();
		bundle.putInt(Descriptions.Columns.ID, id);
		if (pointId != null) {
			bundle.putInt(Descriptions.Columns.POINT_ID, pointId);
		}
		if (discussionId != null) {
			bundle.putInt(Descriptions.Columns.DISCUSSION_ID, discussionId);
		}
		if ((discussionId == null) && (pointId == null)) {
			throw new DataIoException("Both foreign key cant be null");
		}
		bundle.putString(Descriptions.Columns.TEXT, text);
		return bundle;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Descriptions.Columns.ID, id);
		cv.put(Descriptions.Columns.TEXT, text);
		cv.put(Descriptions.Columns.DISCUSSION_ID, discussionId);
		cv.put(Descriptions.Columns.POINT_ID, pointId);
		return cv;
	}

	@Override
	public String toMyString() {

		return toContentValues().toString();
	}
}
