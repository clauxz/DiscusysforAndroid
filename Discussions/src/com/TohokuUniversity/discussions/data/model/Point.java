package com.TohokuUniversity.discussions.data.model;

import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Points;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

public class Point implements Value {

	private boolean changesPending;
	private int id;
	private String name;
	private int orderNumber;
	private int personId;
	private String recentlyEnteredMediaUrl;
	private String recentlyEnteredSource;
	private boolean sharedToPublic;
	private int sideCode;
	private int topicId;

	/** Initialize with default values. Notice, that person and topic id required by underlying sql db schema. */
	public Point() {

		changesPending = false;
		id = Integer.MIN_VALUE;
		name = "Your point here";
		orderNumber = Integer.MIN_VALUE;
		personId = Integer.MIN_VALUE;
		recentlyEnteredMediaUrl = "Paste link to media and return";
		recentlyEnteredSource = "Your source here";
		sharedToPublic = true;
		sideCode = 0;
		topicId = Integer.MIN_VALUE;
	}

	public Point(final Bundle bundle) {

		changesPending = bundle.getBoolean(Points.Columns.CHANGES_PENDING);
		id = bundle.getInt(Points.Columns.ID);
		name = bundle.getString(Points.Columns.NAME);
		orderNumber = bundle.getInt(Points.Columns.ORDER_NUMBER, 0);
		personId = bundle.getInt(Points.Columns.PERSON_ID);
		recentlyEnteredMediaUrl = bundle.getString(Points.Columns.RECENTLY_ENTERED_MEDIA_URL);
		recentlyEnteredSource = bundle.getString(Points.Columns.RECENTLY_ENTERED_SOURCE);
		sharedToPublic = bundle.getBoolean(Points.Columns.SHARED_TO_PUBLIC);
		sideCode = bundle.getInt(Points.Columns.SIDE_CODE);
		topicId = bundle.getInt(Points.Columns.TOPIC_ID);
	}

	public Point(final Cursor cursor) {

		if (cursor.isBeforeFirst()) {
			throw new IllegalArgumentException("You have to iterate cursor first. Was at -1 position.");
		}
		if (cursor.isAfterLast()) {
			throw new IllegalArgumentException("You have to iterate cursor first. Was after last position.");
		}
		int changesPendingIndex = cursor.getColumnIndexOrThrow(Points.Columns.CHANGES_PENDING);
		int orderNumIndex = cursor.getColumnIndexOrThrow(Points.Columns.ORDER_NUMBER);
		int idIndex = cursor.getColumnIndexOrThrow(Points.Columns.ID);
		int nameIndex = cursor.getColumnIndexOrThrow(Points.Columns.NAME);
		int personIdIndex = cursor.getColumnIndexOrThrow(Points.Columns.PERSON_ID);
		int recentlyEnteredMediaUrlIndex = cursor
				.getColumnIndexOrThrow(Points.Columns.RECENTLY_ENTERED_MEDIA_URL);
		int recentlyEnteredSourceIndex = cursor.getColumnIndexOrThrow(Points.Columns.RECENTLY_ENTERED_SOURCE);
		int sharedToPublicIndex = cursor.getColumnIndexOrThrow(Points.Columns.SHARED_TO_PUBLIC);
		int sideCodeIndex = cursor.getColumnIndexOrThrow(Points.Columns.SIDE_CODE);
		int topicIdIndex = cursor.getColumnIndexOrThrow(Points.Columns.TOPIC_ID);
		if (cursor.getInt(changesPendingIndex) == 0) {
			changesPending = false;
		} else if (cursor.getInt(changesPendingIndex) == 1) {
			changesPending = true;
		} else {
			throw new IllegalStateException("Point cursor has unknown changes pending value: "
					+ cursor.getInt(changesPendingIndex));
		}
		orderNumber = cursor.getInt(orderNumIndex);
		id = cursor.getInt(idIndex);
		name = cursor.getString(nameIndex);
		personId = cursor.getInt(personIdIndex);
		recentlyEnteredMediaUrl = cursor.getString(recentlyEnteredMediaUrlIndex);
		recentlyEnteredSource = cursor.getString(recentlyEnteredSourceIndex);
		if (cursor.getInt(sharedToPublicIndex) == 0) {
			sharedToPublic = false;
		} else if (cursor.getInt(sharedToPublicIndex) == 1) {
			sharedToPublic = true;
		} else {
			throw new IllegalStateException("Point has unknown shared to public: "
					+ cursor.getInt(sharedToPublicIndex));
		}
		sideCode = cursor.getInt(sideCodeIndex);
		topicId = cursor.getInt(topicIdIndex);
	}

	public int getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public int getOrderNumber() {

		return orderNumber;
	}

	public int getPersonId() {

		return personId;
	}

	public String getRecentlyEnteredMediaUrl() {

		return recentlyEnteredMediaUrl;
	}

	public String getRecentlyEnteredSource() {

		return recentlyEnteredSource;
	}

	public int getSideCode() {

		return sideCode;
	}

	public int getTopicId() {

		return topicId;
	}

	public boolean isChangesPending() {

		return changesPending;
	}

	public boolean isSharedToPublic() {

		return sharedToPublic;
	}

	public void setChangesPending(final boolean changesPending) {

		this.changesPending = changesPending;
	}

	public void setId(final int id) {

		this.id = id;
	}

	public void setName(final String name) {

		this.name = name;
	}

	public void setOrderNumber(final int orderNumber) {

		this.orderNumber = orderNumber;
	}

	public void setPersonId(final int personId) {

		this.personId = personId;
	}

	public void setRecentlyEnteredMediaUrl(final String recentlyEnteredMediaUrl) {

		this.recentlyEnteredMediaUrl = recentlyEnteredMediaUrl;
	}

	public void setRecentlyEnteredSource(final String recentlyEnteredSource) {

		this.recentlyEnteredSource = recentlyEnteredSource;
	}

	public void setSharedToPublic(final boolean sharedToPublic) {

		this.sharedToPublic = sharedToPublic;
	}

	public void setSideCode(final int sideCode) {

		this.sideCode = sideCode;
	}

	public void setTopicId(final int topicId) {

		this.topicId = topicId;
	}

	public Bundle toBundle() {

		Bundle bundle = new Bundle();
		bundle.putBoolean(Points.Columns.CHANGES_PENDING, changesPending);
		bundle.putInt(Points.Columns.ID, id);
		bundle.putString(Points.Columns.NAME, name);
		bundle.putInt(Points.Columns.ORDER_NUMBER, orderNumber);
		bundle.putInt(Points.Columns.PERSON_ID, personId);
		bundle.putString(Points.Columns.RECENTLY_ENTERED_MEDIA_URL, recentlyEnteredMediaUrl);
		bundle.putString(Points.Columns.RECENTLY_ENTERED_SOURCE, recentlyEnteredSource);
		bundle.putBoolean(Points.Columns.SHARED_TO_PUBLIC, sharedToPublic);
		bundle.putInt(Points.Columns.SIDE_CODE, sideCode);
		bundle.putInt(Points.Columns.TOPIC_ID, topicId);
		return bundle;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Points.Columns.CHANGES_PENDING, changesPending);
		cv.put(Points.Columns.ID, id);
		cv.put(Points.Columns.NAME, name);
		cv.put(Points.Columns.ORDER_NUMBER, orderNumber);
		cv.put(Points.Columns.PERSON_ID, personId);
		cv.put(Points.Columns.RECENTLY_ENTERED_MEDIA_URL, recentlyEnteredMediaUrl);
		cv.put(Points.Columns.RECENTLY_ENTERED_SOURCE, recentlyEnteredSource);
		cv.put(Points.Columns.SHARED_TO_PUBLIC, sharedToPublic);
		cv.put(Points.Columns.SIDE_CODE, sideCode);
		cv.put(Points.Columns.TOPIC_ID, topicId);
		return cv;
	}

	@Override
	public String toMyString() {

		return toBundle().toString();
	}
}
