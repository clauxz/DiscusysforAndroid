package com.slobodastudio.discussions.data.provider;

import com.slobodastudio.discussions.ApplicationConstants;
import com.slobodastudio.discussions.data.DataIoException;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Comments;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Descriptions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Discussions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Persons;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.PersonsTopics;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Points;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Seats;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Sessions;
import com.slobodastudio.discussions.data.provider.DiscussionsContract.Topics;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/** Provider that stores {@link DiscussionsContract} data. Data is usually inserted by SyncService , and
 * queried by various {@link Activity} instances. */
public class DiscussionsProvider extends ContentProvider {

	private static final int COMMENTS_DIR = 500;
	private static final int COMMENTS_ITEM = 501;
	private static final int DESCRIPTION_DIR = 600;
	private static final int DESCRIPTION_ITEM = 601;
	private static final int DISCUSSIONS_DIR = 101;
	private static final int DISCUSSIONS_ITEM = 100;
	private static final int DISCUSSIONS_ITEM_TOPICS_DIR = 102;
	private static final boolean LOGV = true && ApplicationConstants.DEV_MODE;
	private static final int PERSONS_DIR = 201;
	private static final int PERSONS_ITEM = 200;
	private static final int PERSONS_ITEM_DISCUSSIONS_DIR = 204;
	private static final int PERSONS_ITEM_POINTS_DIR = 202;
	private static final int PERSONS_ITEM_TOPICS_DIR = 203;
	private static final int POINTS_DIR = 301;
	private static final int POINTS_ITEM = 300;
	private static final int POINTS_PERSONS_DIR = 302;
	private static final int SEATS_DIR = 801;
	private static final int SEATS_ITEM = 800;
	private static final int SESSIONS_DIR = 901;
	private static final int SESSIONS_ITEM = 900;
	private static final UriMatcher sUriMatcher = buildUriMatcher();
	private static final String TAG = DiscussionsProvider.class.getSimpleName();
	private static final int TOPICS_DIR = 401;
	private static final int TOPICS_ITEM = 400;
	private static final int TOPICS_ITEM_POINTS_DIR = 402;
	private DiscussionsDatabase mOpenHelper;

	/** Build a simple {@link SelectionBuilder} to match the requested {@link Uri}. This is usually enough to
	 * support {@link #insert}, {@link #update}, and {@link #delete} operations. */
	private static SelectionBuilder buildSimpleSelection(final Uri uri) {

		final SelectionBuilder builder = new SelectionBuilder();
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case DISCUSSIONS_DIR:
				return builder.table(Discussions.TABLE_NAME);
			case DISCUSSIONS_ITEM: {
				final String valueId = Discussions.getValueId(uri);
				return builder.table(Discussions.TABLE_NAME).where(Discussions.Columns.ID + "=?", valueId);
			}
			case SEATS_DIR:
				return builder.table(Seats.TABLE_NAME);
			case SEATS_ITEM: {
				final String valueId = Seats.getValueId(uri);
				return builder.table(Seats.TABLE_NAME).where(Seats.Columns.ID + "=?", valueId);
			}
			case SESSIONS_DIR:
				return builder.table(Sessions.TABLE_NAME);
			case SESSIONS_ITEM: {
				final String valueId = Sessions.getValueId(uri);
				return builder.table(Sessions.TABLE_NAME).where(Sessions.Columns.ID + "=?", valueId);
			}
			case POINTS_DIR:
				return builder.table(Points.TABLE_NAME);
			case POINTS_ITEM: {
				final String valueId = Points.getValueId(uri);
				// NOTE: this select where is different column by _id, not a Id
				return builder.table(Points.TABLE_NAME).where(BaseColumns._ID + "=?", valueId);
			}
			case PERSONS_DIR:
				return builder.table(Persons.TABLE_NAME);
			case PERSONS_ITEM: {
				final String valueId = Persons.getValueId(uri);
				return builder.table(Persons.TABLE_NAME).where(Persons.Columns.ID + "=?", valueId);
			}
			case TOPICS_DIR:
				return builder.table(Topics.TABLE_NAME);
			case TOPICS_ITEM: {
				final String valueId = Topics.getValueId(uri);
				return builder.table(Topics.TABLE_NAME).where(Topics.Columns.ID + "=?", valueId);
			}
			case COMMENTS_DIR:
				return builder.table(Comments.TABLE_NAME);
			case COMMENTS_ITEM: {
				final String valueId = Comments.getValueId(uri);
				return builder.table(Comments.TABLE_NAME).where(Comments.Columns.ID + "=?", valueId);
			}
			case DESCRIPTION_DIR:
				return builder.table(Descriptions.TABLE_NAME);
			case DESCRIPTION_ITEM: {
				final String valueId = Descriptions.getValueId(uri);
				return builder.table(Descriptions.TABLE_NAME).where(Descriptions.Columns.ID + "=?", valueId);
			}
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
	}

	/** Build and return a {@link UriMatcher} that catches all {@link Uri} variations supported by this
	 * {@link ContentProvider}. */
	private static UriMatcher buildUriMatcher() {

		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = DiscussionsContract.CONTENT_AUTHORITY;
		// discussion
		matcher.addURI(authority, Discussions.A_TABLE_PREFIX, DISCUSSIONS_DIR);
		matcher.addURI(authority, Discussions.A_TABLE_PREFIX + "/*", DISCUSSIONS_ITEM);
		matcher.addURI(authority, Discussions.A_TABLE_PREFIX + "/*/" + Topics.A_TABLE_PREFIX,
				DISCUSSIONS_ITEM_TOPICS_DIR);
		// point
		matcher.addURI(authority, Points.A_TABLE_PREFIX, POINTS_DIR);
		matcher.addURI(authority, Points.A_TABLE_PREFIX + "/*", POINTS_ITEM);
		matcher.addURI(authority, Points.A_TABLE_PREFIX + "," + Persons.A_TABLE_PREFIX, POINTS_PERSONS_DIR);
		// person
		matcher.addURI(authority, Persons.A_TABLE_PREFIX, PERSONS_DIR);
		matcher.addURI(authority, Persons.A_TABLE_PREFIX + "/*", PERSONS_ITEM);
		matcher.addURI(authority, Persons.A_TABLE_PREFIX + "/*/" + Points.A_TABLE_PREFIX,
				PERSONS_ITEM_POINTS_DIR);
		matcher.addURI(authority, Persons.A_TABLE_PREFIX + "/*/" + Topics.A_TABLE_PREFIX,
				PERSONS_ITEM_TOPICS_DIR);
		matcher.addURI(authority, Persons.A_TABLE_PREFIX + "/*/" + Discussions.A_TABLE_PREFIX,
				PERSONS_ITEM_DISCUSSIONS_DIR);
		// topic
		matcher.addURI(authority, Topics.A_TABLE_PREFIX, TOPICS_DIR);
		matcher.addURI(authority, Topics.A_TABLE_PREFIX + "/*", TOPICS_ITEM);
		matcher.addURI(authority, Topics.A_TABLE_PREFIX + "/*/" + Points.A_TABLE_PREFIX,
				TOPICS_ITEM_POINTS_DIR);
		// comment
		matcher.addURI(authority, Comments.A_TABLE_PREFIX, COMMENTS_DIR);
		matcher.addURI(authority, Comments.A_TABLE_PREFIX + "/*", COMMENTS_ITEM);
		// description
		matcher.addURI(authority, Descriptions.A_TABLE_PREFIX, DESCRIPTION_DIR);
		matcher.addURI(authority, Descriptions.A_TABLE_PREFIX + "/*", DESCRIPTION_ITEM);
		// seat
		matcher.addURI(authority, Seats.A_TABLE_PREFIX, SEATS_DIR);
		matcher.addURI(authority, Seats.A_TABLE_PREFIX + "/*", SEATS_ITEM);
		// seat
		matcher.addURI(authority, Sessions.A_TABLE_PREFIX, SESSIONS_DIR);
		matcher.addURI(authority, Sessions.A_TABLE_PREFIX + "/*", SESSIONS_ITEM);
		return matcher;
	}

	/** Apply the given set of {@link ContentProviderOperation}, executing inside a {@link SQLiteDatabase}
	 * transaction. All changes will be rolled back if any single one fails. */
	@Override
	public ContentProviderResult[] applyBatch(final ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {

		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			final int numOperations = operations.size();
			final ContentProviderResult[] results = new ContentProviderResult[numOperations];
			for (int i = 0; i < numOperations; i++) {
				results[i] = operations.get(i).apply(this, results, i);
			}
			db.setTransactionSuccessful();
			return results;
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {

		if (LOGV) {
			Log.v(TAG, "delete(uri=" + uri + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int rowsCount = builder.where(selection, selectionArgs).delete(db);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsCount;
	}

	@Override
	public String getType(final Uri uri) {

		final int match = sUriMatcher.match(uri);
		switch (match) {
			case DISCUSSIONS_DIR:
				return Discussions.CONTENT_DIR_TYPE;
			case DISCUSSIONS_ITEM:
				return Discussions.CONTENT_ITEM_TYPE;
			case DISCUSSIONS_ITEM_TOPICS_DIR:
				return Topics.CONTENT_DIR_TYPE;
			case POINTS_DIR:
				return Points.CONTENT_DIR_TYPE;
			case POINTS_PERSONS_DIR:
				return Points.CONTENT_DIR_TYPE;
			case POINTS_ITEM:
				return Points.CONTENT_ITEM_TYPE;
			case PERSONS_DIR:
				return Persons.CONTENT_DIR_TYPE;
			case PERSONS_ITEM:
				return Persons.CONTENT_ITEM_TYPE;
			case PERSONS_ITEM_POINTS_DIR:
				return Points.CONTENT_DIR_TYPE;
			case PERSONS_ITEM_TOPICS_DIR:
				return Topics.CONTENT_DIR_TYPE;
			case PERSONS_ITEM_DISCUSSIONS_DIR:
				return Discussions.CONTENT_DIR_TYPE;
			case TOPICS_DIR:
				return Topics.CONTENT_DIR_TYPE;
			case TOPICS_ITEM:
				return Topics.CONTENT_ITEM_TYPE;
			case TOPICS_ITEM_POINTS_DIR:
				return Points.CONTENT_DIR_TYPE;
			case COMMENTS_DIR:
				return Comments.CONTENT_DIR_TYPE;
			case COMMENTS_ITEM:
				return Comments.CONTENT_ITEM_TYPE;
			case DESCRIPTION_DIR:
				return Descriptions.CONTENT_DIR_TYPE;
			case DESCRIPTION_ITEM:
				return Descriptions.CONTENT_ITEM_TYPE;
			case SEATS_DIR:
				return Seats.CONTENT_DIR_TYPE;
			case SEATS_ITEM:
				return Seats.CONTENT_ITEM_TYPE;
			case SESSIONS_DIR:
				return Sessions.CONTENT_DIR_TYPE;
			case SESSIONS_ITEM:
				return Sessions.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {

		if (LOGV) {
			Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final int match = sUriMatcher.match(uri);
		final long insertedId;
		final Uri insertedUri;
		try {
			switch (match) {
				case DISCUSSIONS_DIR:
					insertedId = db.insertOrThrow(Discussions.TABLE_NAME, null, values);
					insertedUri = Discussions.buildTableUri(insertedId);
					// notify subscribers from persons table
					getContext().getContentResolver().notifyChange(Persons.CONTENT_URI, null);
					break;
				case POINTS_DIR:
					if (values.containsKey(Points.Columns.GROUP_ID_SERVER)) {
						values.put(Points.Columns.GROUP_ID, values
								.getAsInteger(Points.Columns.GROUP_ID_SERVER));
						values.remove(Points.Columns.GROUP_ID_SERVER);
					}
					insertedId = db.insertOrThrow(Points.TABLE_NAME, null, values);
					insertedUri = Points.buildTableUri(insertedId);
					break;
				case PERSONS_DIR:
					insertedId = db.insertOrThrow(Persons.TABLE_NAME, null, values);
					insertedUri = Persons.buildTableUri(insertedId);
					break;
				case PERSONS_ITEM_TOPICS_DIR:
					insertedId = db.insertOrThrow(PersonsTopics.TABLE_NAME, null, values);
					insertedUri = Persons.buildTableUri(insertedId);
					break;
				case TOPICS_DIR:
					insertedId = db.insertOrThrow(Topics.TABLE_NAME, null, values);
					insertedUri = Topics.buildTableUri(insertedId);
					break;
				case COMMENTS_DIR:
					insertedId = db.insertOrThrow(Comments.TABLE_NAME, null, values);
					insertedUri = Comments.buildTableUri(insertedId);
					break;
				case DESCRIPTION_DIR:
					insertedId = db.insertOrThrow(Descriptions.TABLE_NAME, null, values);
					insertedUri = Descriptions.buildTableUri(insertedId);
					break;
				case SEATS_DIR:
					insertedId = db.insertOrThrow(Seats.TABLE_NAME, null, values);
					insertedUri = Seats.buildTableUri(insertedId);
					break;
				case SESSIONS_DIR:
					insertedId = db.insertOrThrow(Sessions.TABLE_NAME, null, values);
					insertedUri = Sessions.buildTableUri(insertedId);
					break;
				default:
					throw new IllegalArgumentException("Unknown uri: " + uri);
			}
		} catch (SQLiteException e) {
			throw new DataIoException("Unable to insert uri: " + uri + ", value: " + values.toString(), e);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return insertedUri;
	}

	@Override
	public boolean onCreate() {

		final Context context = getContext();
		mOpenHelper = new DiscussionsDatabase(context);
		return true;
	}

	@Override
	public ParcelFileDescriptor openFile(final Uri uri, final String mode) {

		throw new UnsupportedOperationException("With uri: " + uri + ", mode: " + mode);
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection,
			final String[] selectionArgs, final String sortOrder) {

		if (LOGV) {
			Log.v(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ", selection="
					+ selection + ", selectArg=" + Arrays.toString(selectionArgs) + ", sortOrder="
					+ sortOrder + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		SelectionBuilder builder = new SelectionBuilder();
		Uri notificationUri;
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case DISCUSSIONS_ITEM_TOPICS_DIR: {
				final String valueId = Discussions.getValueId(uri);
				builder.table(Topics.TABLE_NAME).where(Topics.Columns.DISCUSSION_ID + "=?", valueId);
				notificationUri = Topics.CONTENT_URI;
				break;
			}
			case PERSONS_ITEM_POINTS_DIR: {
				final String valueId = Persons.getValueId(uri);
				builder.table(Points.TABLE_NAME).where(Points.Columns.PERSON_ID + "=?", valueId);
				notificationUri = Points.CONTENT_URI;
				break;
			}
			case POINTS_PERSONS_DIR: {
				builder.table(Points.TABLE_NAME + "," + Persons.TABLE_NAME);
				builder.mapToTable(BaseColumns._ID, Points.TABLE_NAME).mapToTable(Points.Columns.ID,
						Points.TABLE_NAME);
				builder.where(Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID + "!=? AND "
						+ Points.Columns.PERSON_ID + "=" + Persons.Qualified.PERSON_ID, selectionArgs);
				Cursor c = builder.query(db, new String[] { BaseColumns._ID, Points.Columns.ID,
						Points.Columns.NAME, Persons.Columns.COLOR }, Points.Qualified.POINT_ID, null,
						sortOrder, null);
				notificationUri = Points.CONTENT_URI;
				c.setNotificationUri(getContext().getContentResolver(), notificationUri);
				return c;
			}
			case PERSONS_ITEM_TOPICS_DIR: {
				final String valueId = Persons.getValueId(uri);
				builder.table(PersonsTopics.TABLE_NAME + "," + Topics.TABLE_NAME).mapToTable(BaseColumns._ID,
						Topics.TABLE_NAME).mapToTable(Topics.Columns.ID, Topics.TABLE_NAME).where(
						PersonsTopics.Columns.PERSON_ID + "=? AND " + PersonsTopics.Columns.TOPIC_ID + "="
								+ Topics.Columns.ID, valueId);
				notificationUri = Discussions.CONTENT_URI;
				Cursor c = builder.query(db, new String[] { BaseColumns._ID, Topics.Columns.ID,
						Topics.Columns.NAME, Topics.Columns.DISCUSSION_ID }, sortOrder);
				c.setNotificationUri(getContext().getContentResolver(), uri);
				return c;
			}
			case PERSONS_ITEM_DISCUSSIONS_DIR: {
				final String valueId = Persons.getValueId(uri);
				builder.table(
						PersonsTopics.TABLE_NAME + "," + Topics.TABLE_NAME + "," + Discussions.TABLE_NAME)
						.mapToTable(BaseColumns._ID, Discussions.TABLE_NAME).mapToTable(
								Discussions.Columns.ID, Discussions.TABLE_NAME).where(
								PersonsTopics.Columns.PERSON_ID + "=? AND " + PersonsTopics.Columns.TOPIC_ID
										+ "=" + Topics.Qualified.TOPIC_ID + " AND "
										+ Topics.Columns.DISCUSSION_ID + "="
										+ Discussions.Qualified.DISCUSSION_ID, valueId);
				notificationUri = Discussions.CONTENT_URI;
				Cursor c = builder.query(db, new String[] { BaseColumns._ID, Discussions.Columns.ID,
						Discussions.Columns.SUBJECT }, Discussions.Qualified.DISCUSSION_ID, null, sortOrder,
						null);
				c.setNotificationUri(getContext().getContentResolver(), notificationUri);
				return c;
			}
			case TOPICS_ITEM_POINTS_DIR: {
				final String valueId = Topics.getValueId(uri);
				builder.table(Points.TABLE_NAME).where(Points.Columns.TOPIC_ID + "=?", valueId);
				notificationUri = Points.CONTENT_URI;
				break;
			}
			case COMMENTS_DIR: {
				builder.table(Comments.TABLE_NAME + "," + Persons.TABLE_NAME);
				builder.mapToTable(BaseColumns._ID, Comments.TABLE_NAME).mapToTable(Comments.Columns.ID,
						Comments.TABLE_NAME);
				if (selectionArgs != null) {
					builder.where(Comments.Columns.POINT_ID + "=? AND " + Comments.Columns.PERSON_ID + "="
							+ Persons.Qualified.PERSON_ID, selectionArgs);
				} else {
					builder.where(selection, (String[]) null);
				}
				Cursor c = builder.query(db, new String[] { BaseColumns._ID, Comments.Columns.ID,
						Comments.Columns.TEXT, Persons.Columns.NAME, Persons.Columns.COLOR },
						Comments.Qualified.COMMENT_ID, null, sortOrder, null);
				notificationUri = Comments.CONTENT_URI;
				c.setNotificationUri(getContext().getContentResolver(), notificationUri);
				return c;
			}
			default:
				notificationUri = uri;
				builder = buildSimpleSelection(uri);
				builder.where(selection, selectionArgs);
		}
		Cursor c = builder.query(db, projection, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), notificationUri);
		return c;
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection,
			final String[] selectionArgs) {

		if (LOGV) {
			Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
		}
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final SelectionBuilder builder = buildSimpleSelection(uri);
		int rowCount = builder.where(selection, selectionArgs).update(db, values);
		getContext().getContentResolver().notifyChange(uri, null);
		return rowCount;
	}
}
