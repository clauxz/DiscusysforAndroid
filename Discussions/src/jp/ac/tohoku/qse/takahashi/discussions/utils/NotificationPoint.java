package jp.ac.tohoku.qse.takahashi.discussions.utils;

import java.util.ArrayList;
import java.util.TreeMap;

import jp.ac.tohoku.qse.takahashi.discussions.data.model.Comment;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.CommentPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Point;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsProvider;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Topics;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class NotificationPoint {

	public final static int MODE_USER=1;
	public final static int MODE_OTHER_USER=2;
	public final static int MODE_ALL_USERS=3;
	
	private DiscussionsProvider provider=null;
	private int mLoggedPerson;
	private int mTopic;
	private int mMode;
	
	
	private ArrayList<Point> points=new ArrayList<Point>();
	private ArrayList<Comment> comments=new ArrayList<Comment>();
	private TreeMap<Integer,Boolean> commentFlag=new TreeMap<Integer,Boolean>();
	
	
	public NotificationPoint(Context context,int loggedperson,int topicId,int mode)//,int pointId)
	{
		this.mLoggedPerson=loggedperson;
		this.mTopic=topicId;
		this.mMode=mode;
		
		this.build(context);//, pointid);
	}
	
	public void build(Context context){//,int pointid){
		
		provider=(DiscussionsProvider)context.getContentResolver()
				.acquireContentProviderClient(
						DiscussionsContract.CONTENT_AUTHORITY)
						.getLocalContentProvider();
		
		
		if(provider!=null)
		{
			
			this.points.clear();
			this.comments.clear();
			this.commentFlag.clear();
			
			//Uri uriPoint=Uri.parse(DiscussionsContract.getBaseUrl()+"/"+
			//			Points.A_TABLE_PREFIX + "/" + Persons.A_TABLE_PREFIX);
			Uri uriPoint=Points.CONTENT_AND_PERSON_URI;
			
			Uri uriComments=Uri.parse(DiscussionsContract.getBaseUrl()+"/"+Comments.A_TABLE_PREFIX);
			
			
			
			String selectionPoint=null;
			String[] selectionArgsPoint=null;
			String sortOrderPoint=null;
			
			String selectionComment=null;
			String[] selectionArgsComment=null;
			String sortOrderComment=Comments.TABLE_NAME+"."+Comments.Columns.ID+" ASC";
			
			switch(this.mMode){
				default:
				case MODE_USER:
					selectionPoint=Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID + "=? ";
					selectionArgsPoint=new String[]{String.valueOf(mTopic),	String.valueOf(mLoggedPerson)};
					sortOrderPoint=Points.Columns.ORDER_NUMBER + " ASC";
					break;
				case MODE_OTHER_USER:
					selectionPoint=Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID + "=? AND "
							+ Points.Columns.PERSON_ID + "=" + Persons.Qualified.PERSON_ID;
					selectionArgsPoint=new String[]{ String.valueOf(mTopic), String.valueOf(mLoggedPerson) };
					sortOrderPoint=Points.Columns.ORDER_NUMBER + " ASC";
					break;
				case MODE_ALL_USERS:
					selectionPoint=Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID
							+ "!=? AND " + Points.Columns.PERSON_ID + "=" + Persons.Qualified.PERSON_ID;
					selectionArgsPoint=new String[]{ String.valueOf(mTopic), String.valueOf(mLoggedPerson) };
					sortOrderPoint=Points.Columns.PERSON_ID + "," + Points.Columns.ORDER_NUMBER + " ASC";
					break;
			}
			
			Cursor cursorPoint=provider.query(uriPoint,
					new String[]{BaseColumns._ID, 
					Points.Columns.ID,
					Points.Columns.NAME,
					Points.Columns.PERSON_ID,
					Points.Columns.CHANGES_PENDING,
					Points.Columns.RECENTLY_ENTERED_MEDIA_URL,
					Points.Columns.RECENTLY_ENTERED_SOURCE,
					Points.Columns.SHARED_TO_PUBLIC,
					Points.Columns.SIDE_CODE,
					Points.Columns.TOPIC_ID,
					Points.Columns.ORDER_NUMBER,
					Persons.Qualified.PERSON_ID},
					selectionPoint, selectionArgsPoint, sortOrderPoint);
		
			if(cursorPoint!=null)
			{
				this.points.clear();
				cursorPoint.moveToFirst();
				
				if(0<cursorPoint.getCount())
				{
					do{
					Point rPoint=new Point(true,cursorPoint);					
						if(rPoint.getId()!=Integer.MIN_VALUE)
						   points.add(rPoint);
						
					}while(cursorPoint.moveToNext());
				}
				cursorPoint.close();
			}
				
			Cursor cursorComment=provider.query(uriComments,
					new String[]{
					BaseColumns._ID, 
					Comments.Columns.ID,
					//Comments.Columns.TEXT,
					Comments.Columns.PERSON_ID, 
					Comments.Columns.POINT_ID						
					}, 
					null, null, sortOrderComment);
			
			if(cursorComment!=null)
			{
				this.comments.clear();
				this.commentFlag.clear();
				cursorComment.moveToFirst();
				
				if(0<cursorComment.getCount())
				{
					do{
					Comment rComment=new Comment(true,cursorComment);					
						if(rComment.getId()!=Integer.MIN_VALUE)
						   comments.add(rComment);				
					}while(cursorComment.moveToNext());
					
					for(int comI=0;comI<comments.size();comI++){
						Comment com=comments.get(comI);
						NotificationComment nCom=new NotificationComment(context,mLoggedPerson);
						
						if(!nCom.IsPersonReadedComment(mLoggedPerson, com.getId())){
							commentFlag.put(com.getId(), true);
						}
						else
						{
							commentFlag.put(com.getId(), false);
						}
					}
				}				
				cursorComment.close();
			}
		}
	}
	
	/**
	 * 
	 * @param pointId
	 * @return TRUE if point contain new comments
	 */
	public boolean IsPointContainNewComments(//final int personID,final int pointId){
			                             final int pointId){
				
		for(int j=0;j<comments.size();j++){
			Comment comment=comments.get(j);
			
			if(pointId==comment.getPointId()){
				
				boolean flag=commentFlag.get(comment.getId());
				
				if(flag==true)
					return true;
			}
		}				
		return false;
	}
	public boolean IsContainNewCommentsInTopic(int topicId)
	{
		for(int i=0;i<points.size();i++){
			Point point=points.get(i);
			
			if(point.getTopicId()==topicId)
			{
				for(int j=0;j<comments.size();j++){
					Comment comment=comments.get(j);
					
					if(point.getId()==comment.getPointId()){
					
						boolean flag=commentFlag.get(comment.getId());
						
						if(flag==true)
							return true;
					}
				}
			}
		}

		return false;
	}
	/**
	 * 
	 * @return TRUE if point contain new comments
	 */
	public boolean IsContainNewCommentsInAllPoints(){

		for(int i=0;i<points.size();i++){
			Point point=points.get(i);
			
			
			for(int j=0;j<comments.size();j++){
				Comment comment=comments.get(j);
				
				if(point.getId()==comment.getPointId()){
				
					boolean flag=commentFlag.get(comment.getId());
					
					if(flag==true)
					return true;
				}
			}
		}

		return false;
	}
}
