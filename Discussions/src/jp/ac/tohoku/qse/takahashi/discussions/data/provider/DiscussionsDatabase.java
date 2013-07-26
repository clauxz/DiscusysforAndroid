package jp.ac.tohoku.qse.takahashi.discussions.data.provider;

import java.lang.ref.Reference;

import jp.ac.tohoku.qse.takahashi.discussions.data.model.Attachment;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Attachments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Descriptions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Discussions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.PersonsTopics;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Seats;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sessions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sources;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Topics;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/** Helper for managing {@link SQLiteDatabase} that stores data for {@link DiscussionsProvider}. */
public class DiscussionsDatabase extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "discussions.db";
	// NOTE: carefully update onUpgrade() when bumping database versions to make
	// sure user data is saved.
	private static final int DATABASE_VERSION = 61;
	private static final String TAG = DiscussionsDatabase.class.getSimpleName();

	/** @param context
	 *            to use to open or create the database */
	public DiscussionsDatabase(final Context context) {

		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {

		// @formatter:off
		db.execSQL("CREATE TABLE " + Sessions.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Sessions.Columns.ID + " INTEGER NOT NULL,"
				+ Sessions.Columns.NAME + " TEXT NOT NULL,"
				+ Sessions.Columns.ESTIMATED_TIME_SLOT + " INTEGER NOT NULL,"
				+ Sessions.Columns.ESTIMATED_DATA_TIME + " TEXT NOT NULL,"
				+ Sessions.Columns.ESTIMATED_END_DATE_TIME + " TEXT NOT NULL,"
				+ " UNIQUE (" + Sessions.Columns.ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Seats.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Seats.Columns.ID + " INTEGER NOT NULL,"
				+ Seats.Columns.NAME + " TEXT NOT NULL,"
				+ Seats.Columns.COLOR + " INTEGER NOT NULL,"
				+ " UNIQUE (" + Seats.Columns.ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Discussions.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Discussions.Columns.ID + " INTEGER NOT NULL,"
				+ Discussions.Columns.SUBJECT + " TEXT NOT NULL,"
				+ Discussions.Columns.HTML_BACKGROUND + " TEXT NOT NULL,"
				+ " UNIQUE (" + Discussions.Columns.ID + ") ON CONFLICT REPLACE)");
		
		db.execSQL("CREATE TABLE " + Persons.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Persons.Columns.ID + " INTEGER NOT NULL,"
				+ Persons.Columns.NAME + " TEXT NOT NULL,"
				+ Persons.Columns.EMAIL + " TEXT NOT NULL,"
				+ Persons.Columns.COLOR + " INTEGER NOT NULL,"
				+ Persons.Columns.ONLINE + " INTEGER NOT NULL,"
				+ Persons.Columns.SEAT_ID + " INTEGER "  + References.SEAT_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
				+ Persons.Columns.SESSION_ID + " INTEGER "  + References.SESSION_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
				+ Persons.Columns.ONLINE_DEVICE_TYPE + " INTEGER,"
				+ " UNIQUE (" + Persons.Columns.ID + ") ON CONFLICT REPLACE)");
		
		/*
		String topicSQL="CREATE TABLE " + Topics.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Topics.Columns.ID + " INTEGER NOT NULL,"
				+ Topics.Columns.NAME + " TEXT NOT NULL,"
				+ Topics.Columns.RUNNING + " INTEGER NOT NULL,"
				+ Topics.Columns.CUMULATIVE_DURATION + " INTEGER NOT NULL,"
				+ Topics.Columns.ANNOTATION + " BLOB,"
				+ Topics.Columns.DISCUSSION_ID + " INTEGER NOT NULL " + References.DISCUSSION_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
				+ " UNIQUE (" + Topics.Columns.ID + ") ON CONFLICT REPLACE)";
		Log.i("Disc CREATE TOPIC",topicSQL);
		db.execSQL(topicSQL);
		//*/
		//*
		db.execSQL("CREATE TABLE " + Topics.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Topics.Columns.ID + " INTEGER NOT NULL,"
				+ Topics.Columns.NAME + " TEXT NOT NULL,"
				+ Topics.Columns.RUNNING + " INTEGER NOT NULL,"
				+ Topics.Columns.CUMULATIVE_DURATION + " INTEGER NOT NULL,"
				+ Topics.Columns.ANNOTATION + " BLOB,"
				+ Topics.Columns.DISCUSSION_ID + " INTEGER NOT NULL " + References.DISCUSSION_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
				+ " UNIQUE (" + Topics.Columns.ID + ") ON CONFLICT REPLACE)");
		//*/
		
		String commentSQL="CREATE TABLE " + Comments.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Comments.Columns.ID + " INTEGER NOT NULL,"
				+ Comments.Columns.TEXT + " TEXT NOT NULL,"
				+ Comments.Columns.PERSON_ID + " INTEGER, " 
				+ Comments.Columns.POINT_ID + " INTEGER, " 
				//+ Comments.Columns.ISNEW + " INTEGER, "   //new column, new old comment
				+ " UNIQUE (" + Comments.Columns.ID + ") ON CONFLICT REPLACE)";
		
		Log.i("Disc CREATE COMMENT",commentSQL);
		db.execSQL(commentSQL);
		/*
		db.execSQL("CREATE TABLE " + Comments.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Comments.Columns.ID + " INTEGER NOT NULL,"
				+ Comments.Columns.TEXT + " TEXT NOT NULL,"
				+ Comments.Columns.PERSON_ID + " INTEGER, " 
				+ Comments.Columns.POINT_ID + " INTEGER, " 
				+ " UNIQUE (" + Comments.Columns.ID + ") ON CONFLICT REPLACE)");
		//*/
		
		// table contains information comments which was readed
		String commentReadEntrySQL="CREATE TABLE "+CommentsPersonReadEntry.TABLE_NAME+" ("
				+BaseColumns._ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+CommentsPersonReadEntry.Columns.ID+" INTEGER NOT NULL,"
				
				+CommentsPersonReadEntry.Columns.COMMENT_ID+" INTEGER "+References.COMMENT_ID+" ON UPDATE CASCADE ON DELETE CASCADE,"
				+CommentsPersonReadEntry.Columns.PERSON_ID+" INTEGER "+References.PERSON_ID+" ON UPDATE CASCADE ON DELETE CASCADE,"
				//+" FOREIGN KEY ( "+CommentsPersonReadEntry.Columns.COMMENT_ID+" ) REFERENCES "+Comments.TABLE_NAME+" ( "+Comments.Columns.ID+" ) ,"
				//+" FOREIGN KEY ( "+CommentsPersonReadEntry.Columns.PERSON_ID+") PREFRENCES "+Persons.TABLE_NAME+" ( "+Persons.Columns.ID+" ) ,"
				+" UNIQUE ("+CommentsPersonReadEntry.Columns.ID+") ON CONFLICT REPLACE)";
		Log.i("Disc CREATE CommentsPersonEntry",commentReadEntrySQL);
		db.execSQL(commentReadEntrySQL);
		
		
				
		db.execSQL("CREATE TABLE " + Descriptions.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Descriptions.Columns.ID + " INTEGER NOT NULL,"
				+ Descriptions.Columns.TEXT + " TEXT NOT NULL,"
				+ Descriptions.Columns.DISCUSSION_ID + " INTEGER " + References.DISCUSSION_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
				+ Descriptions.Columns.POINT_ID + " INTEGER, " 
				+ " UNIQUE (" + Descriptions.Columns.ID + ") ON CONFLICT REPLACE)");
		
		String pointSQL="CREATE TABLE " + Points.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Points.Columns.ID + " INTEGER NOT NULL,"
				+ Points.Columns.PERSON_ID + " INTEGER NOT NULL " + References.PERSON_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
				+ Points.Columns.TOPIC_ID + " INTEGER NOT NULL " + References.TOPIC_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
			    + Points.Columns.NAME + " TEXT NOT NULL,"
				+ Points.Columns.SHARED_TO_PUBLIC + " INTEGER NOT NULL,"
				+ Points.Columns.CHANGES_PENDING + " INTEGER NOT NULL,"
				+ Points.Columns.ORDER_NUMBER + " INTEGER NOT NULL,"
				+ Points.Columns.SIDE_CODE + " INTEGER NOT NULL,"
				+ Points.Columns.RECENTLY_ENTERED_MEDIA_URL + " TEXT NOT NULL,"
				+ Points.Columns.RECENTLY_ENTERED_SOURCE + " TEXT NOT NULL,"
				+ Points.Columns.ISNEW + " INTEGER, "
				+ " UNIQUE (" + Points.Columns.ID + ") ON CONFLICT REPLACE)";
		Log.i("Disc CREATE POINT",pointSQL);
		db.execSQL(pointSQL);
		/*
		db.execSQL("CREATE TABLE " + Points.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Points.Columns.ID + " INTEGER NOT NULL,"
				+ Points.Columns.PERSON_ID + " INTEGER NOT NULL " + References.PERSON_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
				+ Points.Columns.TOPIC_ID + " INTEGER NOT NULL " + References.TOPIC_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
			    + Points.Columns.NAME + " TEXT NOT NULL,"
				+ Points.Columns.SHARED_TO_PUBLIC + " INTEGER NOT NULL,"
				+ Points.Columns.CHANGES_PENDING + " INTEGER NOT NULL,"
				+ Points.Columns.ORDER_NUMBER + " INTEGER NOT NULL,"
				+ Points.Columns.SIDE_CODE + " INTEGER NOT NULL,"
				+ Points.Columns.RECENTLY_ENTERED_MEDIA_URL + " TEXT NOT NULL,"
				+ Points.Columns.RECENTLY_ENTERED_SOURCE + " TEXT NOT NULL,"
				+ " UNIQUE (" + Points.Columns.ID + ") ON CONFLICT REPLACE)");
		//*/
		String attachemntSQL="CREATE TABLE " + Attachments.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Attachments.Columns.ID + " INTEGER NOT NULL,"
				+ Attachments.Columns.NAME + " TEXT NOT NULL,"
				+ Attachments.Columns.FORMAT + " INTEGER NOT NULL,"
				+ Attachments.Columns.PERSON_ID + " INTEGER, "
				+ Attachments.Columns.POINT_ID + " INTEGER,"
				+ Attachments.Columns.DISCUSSION_ID + " INTEGER,"
				+ Attachments.Columns.THUMB + " BLOB,"
				+ Attachments.Columns.VIDEO_EMBED_URL + " TEXT,"
				+ Attachments.Columns.VIDEO_LINK_URL + " TEXT,"
				+ Attachments.Columns.VIDEO_THUMB_URL + " TEXT,"
				+ Attachments.Columns.LINK + " TEXT,"
				+ Attachments.Columns.TITLE + " TEXT,"
				+ Attachments.Columns.ORDER_NUMBER + " NOT NULL,"
				//+ Attachments.Columns.ISNEW + " INTEGER, "
				+ " UNIQUE (" + Attachments.Columns.ID + ") ON CONFLICT REPLACE)";
		
		Log.i("Disc CREATE ATTACHEMNT", attachemntSQL);
		db.execSQL(attachemntSQL);
		/*
		db.execSQL("CREATE TABLE " + Attachments.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Attachments.Columns.ID + " INTEGER NOT NULL,"
				+ Attachments.Columns.NAME + " TEXT NOT NULL,"
				+ Attachments.Columns.FORMAT + " INTEGER NOT NULL,"
				+ Attachments.Columns.PERSON_ID + " INTEGER, "
				+ Attachments.Columns.POINT_ID + " INTEGER,"
				+ Attachments.Columns.DISCUSSION_ID + " INTEGER,"
				+ Attachments.Columns.THUMB + " BLOB,"
				+ Attachments.Columns.VIDEO_EMBED_URL + " TEXT,"
				+ Attachments.Columns.VIDEO_LINK_URL + " TEXT,"
				+ Attachments.Columns.VIDEO_THUMB_URL + " TEXT,"
				+ Attachments.Columns.LINK + " TEXT,"
				+ Attachments.Columns.TITLE + " TEXT,"
				+ Attachments.Columns.ORDER_NUMBER + " NOT NULL,"
				+ " UNIQUE (" + Attachments.Columns.ID + ") ON CONFLICT REPLACE)");
		//*/		
		db.execSQL("CREATE TABLE " + Sources.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Sources.Columns.ID + " INTEGER NOT NULL,"
				+ Sources.Columns.LINK + " TEXT NOT NULL,"
				+ Sources.Columns.DESCRIPTION_ID + " INTEGER NOT NULL,"		
				+ Sources.Columns.ORDER_NUMBER + " INTEGER NOT NULL,"		
				+ " UNIQUE (" + Sources.Columns.ID + ") ON CONFLICT REPLACE)");
		
		// many-to-many table
		db.execSQL("CREATE TABLE " + PersonsTopics.TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + PersonsTopics.Columns.PERSON_ID + " INTEGER NOT NULL, " //+ References.PERSON_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
                + PersonsTopics.Columns.TOPIC_ID + " INTEGER NOT NULL, " //+ References.TOPIC_ID + " ON UPDATE CASCADE ON DELETE CASCADE,"
                + "UNIQUE (" + PersonsTopics.Columns.PERSON_ID + "," + PersonsTopics.Columns.TOPIC_ID + ") ON CONFLICT REPLACE)");
		
		// triggers
		db.execSQL("CREATE TRIGGER " + Triggers.PERSONS_DELETE_TOPICS 
				+ " AFTER DELETE ON " + PersonsTopics.TABLE_NAME + " FOR EACH ROW "
				+ " BEGIN DELETE FROM " + Topics.TABLE_NAME  
				+ " WHERE " + Topics.Columns.ID + "=OLD." + PersonsTopics.Columns.TOPIC_ID + ";"
                + " END;");
		// @formatter:on 
	}

	@Override
	public void onOpen(final SQLiteDatabase db) {

		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {

		Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
		if (oldVersion != DATABASE_VERSION) {
			Log.w(TAG, "Destroying old data during upgrade");
			db.execSQL("DROP TABLE IF EXISTS " + Discussions.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Persons.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Points.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Topics.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + PersonsTopics.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Comments.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + CommentsPersonReadEntry.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Descriptions.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Seats.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Sessions.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Attachments.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + Sources.TABLE_NAME);
			db.execSQL("DROP TRIGGER IF EXISTS " + Triggers.PERSONS_DELETE_TOPICS);
			onCreate(db);
		}
	}

	interface Triggers {

		static final String PERSONS_DELETE_TOPICS = "PersonsDeletesTopics";
	}

	/** {@code REFERENCES} clauses. */
	private interface References {

		static final String DISCUSSION_ID = "REFERENCES " + Discussions.TABLE_NAME + "("
				+ Discussions.Columns.ID + ")";
		static final String PERSON_ID = "REFERENCES " + Persons.TABLE_NAME + "(" + Persons.Columns.ID + ")";
		static final String POINT_ID = "REFERENCES " + Points.TABLE_NAME + "(" + Points.Columns.ID + ")";
		static final String SEAT_ID = "REFERENCES " + Seats.TABLE_NAME + "(" + Seats.Columns.ID + ")";
		static final String SESSION_ID = "REFERENCES " + Sessions.TABLE_NAME + "(" + Sessions.Columns.ID
				+ ")";
		static final String TOPIC_ID = "REFERENCES " + Topics.TABLE_NAME + "(" + Topics.Columns.ID + ")";
		
		static final String COMMENT_ID="REFERENCES "+ Comments.TABLE_NAME+"("+Comments.Columns.ID+")";
		
	}
}
