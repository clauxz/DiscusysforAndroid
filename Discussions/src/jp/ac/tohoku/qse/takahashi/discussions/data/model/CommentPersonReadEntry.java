package jp.ac.tohoku.qse.takahashi.discussions.data.model;

import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class CommentPersonReadEntry implements Value{

	private int id;
	private final Integer personId;
	private final Integer commentId;
	
	public CommentPersonReadEntry(){
		id=Integer.MIN_VALUE;
		personId=Integer.MIN_VALUE;
		commentId=Integer.MIN_VALUE;		
	}
	
	public CommentPersonReadEntry(final Bundle commentBundle) {

		id = commentBundle.getInt(CommentsPersonReadEntry.Columns.ID, Integer.MIN_VALUE);
		personId = commentBundle.getInt(CommentsPersonReadEntry.Columns.PERSON_ID, Integer.MIN_VALUE);
		commentId = commentBundle.getInt(CommentsPersonReadEntry.Columns.COMMENT_ID, Integer.MIN_VALUE);
	}

	public CommentPersonReadEntry(final Cursor cursor) {

		super();
		if (cursor.getCount() != 1) {
			throw new IllegalArgumentException("Cursor shoud contain single value, was: " + cursor.getCount());
		}
		if (cursor.moveToFirst()) {
			int idIndex = cursor.getColumnIndexOrThrow(CommentsPersonReadEntry.Columns.ID);
			int personIdIndex = cursor.getColumnIndexOrThrow(CommentsPersonReadEntry.Columns.PERSON_ID);
			int commentIdIndex  = cursor.getColumnIndexOrThrow(CommentsPersonReadEntry.Columns.COMMENT_ID);
			id = cursor.getInt(idIndex);
			personId = cursor.getInt(personIdIndex);
			commentId = cursor.getInt(commentIdIndex);
		} else {
			throw new IllegalArgumentException("Cursor was without value");
		}
	}
	
	public CommentPersonReadEntry(final boolean fromRow,final Cursor cursor) {

		super();
		if(fromRow==true) 
		{
			
			int idIndex = cursor.getColumnIndexOrThrow(CommentsPersonReadEntry.Columns.ID);
			int personIdIndex = cursor.getColumnIndexOrThrow(CommentsPersonReadEntry.Columns.PERSON_ID);
			int commentIdIndex  = cursor.getColumnIndexOrThrow(CommentsPersonReadEntry.Columns.COMMENT_ID);
			id = cursor.getInt(idIndex);
			personId = cursor.getInt(personIdIndex);
			commentId = cursor.getInt(commentIdIndex);
		}
		else
		{
			id=Integer.MIN_VALUE;
			personId=Integer.MIN_VALUE;
			commentId=Integer.MIN_VALUE;
		}
		
		//Log.i("Disc CommentReadedEntry","id:"+String.valueOf(id)+" commentId:"+String.valueOf(commentId)+
		//		" personId:"+String.valueOf(personId));
	}
	
	public int getId() {

		return id;
	}

	public Integer getPersonId() {

		return personId;
	}

	public Integer getCommentId() {

		return commentId;
	}

	public void setId(final int id) {

		this.id = id;
	}
	
	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(CommentsPersonReadEntry.Columns.ID, id);
		cv.put(CommentsPersonReadEntry.Columns.PERSON_ID, personId);
		cv.put(CommentsPersonReadEntry.Columns.COMMENT_ID, commentId);

		return cv;
	}
	
	@Override
	public String toMyString() {

		return toContentValues().toString();
	}
}
