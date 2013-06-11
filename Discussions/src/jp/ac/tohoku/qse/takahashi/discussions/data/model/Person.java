package jp.ac.tohoku.qse.takahashi.discussions.data.model;

import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;

import android.content.ContentValues;
import android.database.Cursor;

@Deprecated
public class Person implements Value {

	private final int color;
	private final String email;
	private final int id;
	private final String name;
	private final boolean online;
	private final int seatId;
	private final int sessionId;

	public Person(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndexOrThrow(Persons.Columns.ID);
			int nameIndex = cursor.getColumnIndexOrThrow(Persons.Columns.NAME);
			int colorIndex = cursor.getColumnIndexOrThrow(Persons.Columns.COLOR);
			int emailIndex = cursor.getColumnIndexOrThrow(Persons.Columns.EMAIL);
			int onlineIndex = cursor.getColumnIndexOrThrow(Persons.Columns.ONLINE);
			int sessionIdIndex = cursor.getColumnIndexOrThrow(Persons.Columns.SESSION_ID);
			int seatIdIndex = cursor.getColumnIndexOrThrow(Persons.Columns.SEAT_ID);
			color = cursor.getInt(colorIndex);
			if (cursor.getInt(onlineIndex) == 0) {
				online = false;
			} else if (cursor.getInt(onlineIndex) == 1) {
				online = true;
			} else {
				throw new IllegalStateException("Value has unknown online: " + cursor.getInt(onlineIndex));
			}
			id = cursor.getInt(idIndex);
			name = cursor.getString(nameIndex);
			email = cursor.getString(emailIndex);
			seatId = cursor.getInt(seatIdIndex);
			sessionId = cursor.getInt(sessionIdIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}

	public int getColor() {

		return color;
	}

	public String getEmail() {

		return email;
	}

	public int getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public int getSeatId() {

		return seatId;
	}

	public int getSessionId() {

		return sessionId;
	}

	public boolean isOnline() {

		return online;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Persons.Columns.SESSION_ID, sessionId);
		cv.put(Persons.Columns.SEAT_ID, seatId);
		cv.put(Persons.Columns.COLOR, color);
		cv.put(Persons.Columns.EMAIL, email);
		cv.put(Persons.Columns.ID, id);
		cv.put(Persons.Columns.NAME, name);
		cv.put(Persons.Columns.ONLINE, online);
		return cv;
	}

	@Override
	public String toMyString() {

		return toContentValues().toString();
	}
}
