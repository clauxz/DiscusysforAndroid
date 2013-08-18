package jp.ac.tohoku.qse.takahashi.discussions.utils;

import java.util.ArrayList;
import java.util.TreeMap;

import jp.ac.tohoku.qse.takahashi.discussions.data.model.CommentPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsProvider;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry.Columns;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class NotificationComment {

	ArrayList<CommentPersonReadEntry> readed=new ArrayList<CommentPersonReadEntry>();
	//TreeMap<Integer,CommentPersonReadEntry> readed=new TreeMap<Integer, CommentPersonReadEntry>();
	
	DiscussionsProvider provider=null;
	
	public NotificationComment(Context context,int LoggedInPersonId){
		this.build(context, LoggedInPersonId);
	}
	
	public void build(Context context,int LoggedInPersonId){
		
		if(context!=null)
		{
			provider=(DiscussionsProvider)context.getContentResolver()
					.acquireContentProviderClient(
							DiscussionsContract.CONTENT_AUTHORITY)
							.getLocalContentProvider();
			
			if(provider!=null)
			{
				Uri uri=Uri.parse(DiscussionsContract.getBaseUrl()+"//"+ CommentsPersonReadEntry.A_TABLE_PREFIX);
				Cursor cursor=provider.query(uri, 
						new String[]{
							BaseColumns._ID,
							CommentsPersonReadEntry.Columns.ID,
							CommentsPersonReadEntry.Columns.COMMENT_ID,
							CommentsPersonReadEntry.Columns.PERSON_ID},  
						null,null,CommentsPersonReadEntry.Columns.ID+" ASC");
				
				if(cursor!=null){
					this.readed.clear();
					cursor.moveToFirst();
					
					if(0<cursor.getCount())
						do{
							CommentPersonReadEntry rCom=new CommentPersonReadEntry(true,cursor);					
							if(rCom.getId()!=Integer.MIN_VALUE)
							   readed.add(rCom);
							
						}while(cursor.moveToNext());
					
				cursor.close();
				
				}
			}
		}
	}
	
	public void Clear(){
		this.readed.clear();
	}
	
	public boolean IsPersonReadedComment(final int personID,final int commentId){
		for(int i=0;i<this.readed.size();i++){
			CommentPersonReadEntry comment=this.readed.get(i);
			if(personID==comment.getPersonId()
					&&
					commentId==comment.getCommentId()){
				return true;
			}
		}
		return false;
	}
	
	
	
}
