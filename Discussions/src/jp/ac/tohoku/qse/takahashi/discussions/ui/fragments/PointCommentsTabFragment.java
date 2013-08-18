package jp.ac.tohoku.qse.takahashi.discussions.ui.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.ws.rs.core.UriBuilder;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Comment;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.CommentPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.SelectedPoint;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Attachments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsProvider;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.SelectionBuilder;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;
import jp.ac.tohoku.qse.takahashi.discussions.ui.activities.BaseActivity;
import jp.ac.tohoku.qse.takahashi.discussions.ui.activities.PointDetailsActivity;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MyLog;
import jp.ac.tohoku.qse.takahashi.discussions.utils.NotificationComment;
import jp.ac.tohoku.qse.takahashi.discussions.utils.NotificationPoint;
import jp.ac.tohoku.qse.takahashi.discussions.utils.TextViewUtils;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class PointCommentsTabFragment extends SherlockFragment implements OnClickListener,
		OnItemClickListener {

	
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = PointCommentsTabFragment.class.getSimpleName();
	private TextView mPointNameTextView;
	private EditText mCommentEditText;
	private ListView mCommentsList;
	private SimpleCursorAdapter mCommentsAdapter;
	//private SimpleCursorAdapter mAttachmentAdapter;
	private final PointCursorLoader mPointCursorLoader;
	private int mLoggedInPersonId;
	private SelectedPoint mSelectedPoint;
	private int mSessionId;
	//private int mTopicID;
	//map of readed comments
	NotificationComment notificationComment;
	
	
	android.os.Handler deleteTimer=new android.os.Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			
			onInsertCommentReadEntry();
		};
	};
	
	
	
	public PointCommentsTabFragment() {

		// initialize default values
		mPointCursorLoader = new PointCursorLoader();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		mCommentsList = (ListView) inflater.inflate(R.layout.tab_fragment_point_comments, container, false);
		mCommentsList.setOnItemClickListener(this);
		registerForContextMenu(mCommentsList);
		addCommentsHeader(inflater);
		addCommentsFooter(inflater);
		initFromArguments();
		//initReadedComments();
		updateCommentsStatus();
		
		deleteTimer.sendEmptyMessageDelayed(0, 5*1000);
		
		return mCommentsList;
	}

	
	
	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		populateSavedInstanceState(savedInstanceState);
		setUpCommentsAdapter();
		initCommentsLoader();
			
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		if (mCommentsAdapter.getCount() > 0) {
			AdapterContextMenuInfo info = castAdapterContextMenuInfo(menuInfo);
			Cursor cursor = (Cursor) mCommentsAdapter.getItem(info.position - 1);
			if (cursor == null) {
				// For some reason the requested item isn't available, do nothing
				return;
			}
			int textIndex = cursor.getColumnIndexOrThrow(Comments.Columns.TEXT);
			int personIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.PERSON_ID);
			int personId = cursor.getInt(personIdIndex);
			if (personId == mLoggedInPersonId) {
				menu.setHeaderTitle(cursor.getString(textIndex)); // if your table name is name
				android.view.MenuInflater inflater = getActivity().getMenuInflater();
				inflater.inflate(R.menu.context_comments, menu);
			}
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_delete_comments:
				onActionDeleteComment(item);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

		Cursor cursor = (Cursor) mCommentsAdapter.getItem(position - 1);
		int commentIdIndex = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
		int commentIsNewIndex=cursor.getColumnIndex(Comments.Columns.IsReadedFlag);
		int commentId = cursor.getInt(commentIdIndex);
		int commentIsNew=cursor.getInt(commentIsNewIndex);
		Uri commentUri = Comments.buildTableUri(commentId);
		
		Intent commentIntent = new Intent(Intent.ACTION_VIEW, commentUri);
		
		Bundle b=new Bundle();
		b.putInt(ExtraKey.ORIGIN_PERSON_ID, mLoggedInPersonId);
		//b.putInt(ExtraKey.TOPIC_ID,mTopicID );
		b.putInt(ExtraKey.SELECTED_COMMENT, commentId);
		if(commentIsNew==1)
			b.putBoolean(ExtraKey.COMMENT_NEW_FLAG, true);
		else
			b.putBoolean(ExtraKey.COMMENT_NEW_FLAG, false);
		b.putParcelable(ExtraKey.SELECTED_POINT, mSelectedPoint);
		b.putInt(ExtraKey.SESSION_ID,mSessionId);
		commentIntent.putExtras(b);
		
		startActivity(commentIntent);
	}

	@Override
	public void onClick(final View v) {

		if (v.getId() == R.id.btn_add_comment) {
			String comment = TextViewUtils.toString(mCommentEditText);
			if (!TextUtils.isEmpty(comment)) {
				mCommentEditText.setText("");
				insertComment(comment);
				WPFCork_insertCommentPlaceholder();
			}
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putString(ExtraKey.COMMENT_TEXT, TextViewUtils.toString(mCommentEditText));
	}

	private void addCommentsHeader(final LayoutInflater layoutInflater) {

		View headerView = layoutInflater.inflate(R.layout.list_header_point_name, null, false);
		mPointNameTextView = (TextView) headerView.findViewById(R.id.list_header_point_name);
		mCommentsList.addHeaderView(headerView, null, false);
	}

	private void addCommentsFooter(final LayoutInflater layoutInflater) {

		View containerLayout = layoutInflater.inflate(R.layout.layout_comments_footer, null, false);
		mCommentsList.addFooterView(containerLayout, null, false);
		mCommentEditText = (EditText) containerLayout.findViewById(R.id.et_point_comment);
		containerLayout.findViewById(R.id.btn_add_comment).setOnClickListener(this);
	}

	private void initCommentsLoader() {

		Bundle args = new Bundle();
		args.putInt(ExtraKey.POINT_ID, mSelectedPoint.getPointId());
		getLoaderManager().initLoader(PointCursorLoader.COMMENTS_ID, args, mPointCursorLoader);
		getLoaderManager().initLoader(PointCursorLoader.POINT_NAME_ID, args, mPointCursorLoader);
	}

	private void initFromArguments() {

		Bundle arguments = getArguments();
		if (arguments == null) {
			throw new NullPointerException("You are trying to instantiate fragment without arguments");
		}
		if (!arguments.containsKey(ExtraKey.SELECTED_POINT)) {
			throw new IllegalStateException("fragment was called without selected point extra");
		}
		if (!arguments.containsKey(ExtraKey.ORIGIN_PERSON_ID)) {
			throw new IllegalStateException("fragment was called without logged in person id extra");
		}
		
		
		mSelectedPoint = arguments.getParcelable(ExtraKey.SELECTED_POINT);
		mLoggedInPersonId = arguments.getInt(ExtraKey.ORIGIN_PERSON_ID, Integer.MIN_VALUE);
		//mTopicID=arguments.getInt(ExtraKey.TOPIC_ID);
	}

	private void populateSavedInstanceState(final Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			String comment = savedInstanceState.getString(ExtraKey.COMMENT_TEXT);
			mCommentEditText.setText(comment);
		}
	}

	private void insertComment(final String comment) {

		Bundle commentValues = new Bundle();
		commentValues.putString(Comments.Columns.TEXT, comment);
		commentValues.putInt(Comments.Columns.POINT_ID, mSelectedPoint.getPointId());
		commentValues.putInt(Comments.Columns.PERSON_ID, mLoggedInPersonId);
		((BaseActivity) getActivity()).getServiceHelper().insertComment(commentValues, mSelectedPoint);
	}
	
	/**
	 * Function used only for fixing WPF client bug. In future such function need to delete when 
	 * WPF client will be fixed (wpf client will change algorithm for comment adding and placeholder functionality).
	 * 
	 */
	private void WPFCork_insertCommentPlaceholder()
	{
		String comment=getActivity().getResources().getString(R.string.wpf_cork_comment_placeholder_text);
		
		Bundle commentValues = new Bundle();
		commentValues.putString(Comments.Columns.TEXT, comment);
		commentValues.putInt(Comments.Columns.POINT_ID, mSelectedPoint.getPointId());
		//commentValues.putInt(Comments.Columns.PERSON_ID, Integer.valueOf(null));//mLoggedInPersonId);
		
		((BaseActivity) getActivity()).getServiceHelper().WPFCork_insertPlaceholder(commentValues, mSelectedPoint);
	}

	private void onActionDeleteComment(final MenuItem item) {

		AdapterContextMenuInfo info = castAdapterContextMenuInfo(item.getMenuInfo());
		Cursor cursor = (Cursor) mCommentsAdapter.getItem(info.position - 1);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}
		int columnIndex = cursor.getColumnIndexOrThrow(Comments.Columns.ID);
		int commentId = cursor.getInt(columnIndex);
		((BaseActivity) getActivity()).getServiceHelper().deleteComment(commentId, mSelectedPoint);
	}
	
	protected void onInsertCommentReadEntry(){
		Cursor cursor=mCommentsAdapter.getCursor();
		
		if(cursor!=null)// && 0<cursor.getCount())
		{
			if(0<cursor.getCount())
			{
				cursor.moveToFirst();
				ArrayList<Integer> comments=new ArrayList<Integer>();
				
				while(cursor.moveToNext()){
					int indexId=cursor.getColumnIndexOrThrow(Comments.Columns.ID);
					int indexCommentPoint=cursor.getColumnIndexOrThrow(Comments.Columns.POINT_ID);
					
					int id=cursor.getInt(indexId);
					int pointId=cursor.getInt(indexCommentPoint);
					
					if(pointId==mSelectedPoint.getPointId())
					{
						if(!notificationComment.IsPersonReadedComment(mLoggedInPersonId,id))
						{
							comments.add(id);
						}
					}				
				}
				
				if(0<comments.size())
				{
					Bundle commentValues=new Bundle();
					commentValues.putInt(CommentsPersonReadEntry.Columns.PERSON_ID, mLoggedInPersonId);
					//commentValues.putInt(CommentsPersonReadEntry.Columns.PERSON_ID, mLoggedInPersonId);
					
					int[] dataArr=new int[comments.size()];
					for(int i=0;i<comments.size();i++){
						dataArr[i]=comments.get(i);
					}
					
					((BaseActivity) getActivity()).getServiceHelper()
						.insertCommentPersonReadedEntities(commentValues, mSelectedPoint,dataArr);
					
					mCommentsAdapter.notifyDataSetChanged();
				}
			}
		}
	}
		
	
	private void setUpCommentsAdapter() {

		mCommentsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_comments, null,
				new String[] { Persons.Columns.NAME, Comments.Columns.TEXT, Persons.Columns.COLOR, Comments.Columns.ID },
				new int[] { R.id.text_comment_person_name, R.id.text_comment, R.id.image_person_color,
			R.id.image_comment_item_new }, 0);
		mCommentsAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {
				switch (view.getId()) {
					case R.id.image_person_color:
						ImageView colorView = (ImageView) view;
						colorView.setBackgroundColor(cursor.getInt(columnIndex));
						return true;
					case R.id.text_comment:
						TextView itemText = (TextView) view;
						itemText.setText(cursor.getString(columnIndex));
						return true;
					case R.id.text_comment_person_name:
						TextView itemName = (TextView) view;
						itemName.setText(cursor.getString(columnIndex));
						return true;
					case R.id.image_comment_item_new:
					{
						int indexId=cursor.getColumnIndexOrThrow(Comments.Columns.ID);
						int id=cursor.getInt(indexId);
						
						if(!notificationComment.IsPersonReadedComment(mLoggedInPersonId, id)){
							((ImageView)view).setImageBitmap(
									BitmapFactory.decodeResource(getResources(), R.drawable.ic_data_changed));
						}
						else{
							((ImageView)view).setImageBitmap(null);
						}
					}
					return true;
					default:
						return false;
				}
			}
		});
		mCommentsList.setAdapter(mCommentsAdapter);
	}

	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
	public static Bundle intentToFragmentArguments(final Intent intent) {

		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}
		if (!intent.hasExtra(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("intent was without discussion id");
		}
		if (!intent.hasExtra(ExtraKey.POINT_ID)) {
			throw new IllegalStateException("intent was without point id");
		}
		if (!intent.hasExtra(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("intent was without person id");
		}
		if (!intent.hasExtra(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("intent was without topic id");
		}
		int discussionId = intent.getIntExtra(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
		int personId = intent.getIntExtra(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
		int topicId = intent.getIntExtra(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
		int pointId = intent.getIntExtra(ExtraKey.POINT_ID, Integer.MIN_VALUE);
		SelectedPoint point = new SelectedPoint();
		point.setDiscussionId(discussionId);
		point.setPersonId(personId);
		point.setTopicId(topicId);
		point.setPointId(pointId);
		arguments.putParcelable(ExtraKey.SELECTED_POINT, point);
		int loggedInPersonId;
		if (intent.hasExtra(ExtraKey.ORIGIN_PERSON_ID)) {
			loggedInPersonId = intent.getIntExtra(ExtraKey.ORIGIN_PERSON_ID, Integer.MIN_VALUE);
		} else {
			loggedInPersonId = personId;
		}
		arguments.putInt(ExtraKey.ORIGIN_PERSON_ID, loggedInPersonId);
		return arguments;
	}

	
	public void updateCommentsStatus(){
		
		notificationComment=new NotificationComment(getActivity(),mLoggedInPersonId);
		
		if(getActivity() instanceof PointDetailsActivity){
			boolean com=false;
			
			if(mCommentsAdapter!=null)
			{
				Cursor cursor=mCommentsAdapter.getCursor();
				
				if(cursor!=null)// && 0<cursor.getCount())
				{
					if(0<cursor.getCount())
					{
						cursor.moveToFirst();
						do
						{
							int indexId=cursor.getColumnIndexOrThrow(Comments.Columns.ID);
							int indexCommentPoint=cursor.getColumnIndexOrThrow(Comments.Columns.POINT_ID);
							
							int id=cursor.getInt(indexId);
							int pointId=cursor.getInt(indexCommentPoint);
							
							if(pointId==mSelectedPoint.getPointId())
							{
								if(!notificationComment.IsPersonReadedComment(mLoggedInPersonId,id))
								{
									com=true;
									((PointDetailsActivity)getActivity()).setNewComments(com);
									return;
								}
							}
							
						}while(cursor.moveToNext());
					}
				}
			}((PointDetailsActivity)getActivity()).setNewComments(com);
		}		
	}
	
	public void notifyFragmentCommentsChanged(){
		updateCommentsStatus();
		
		if(mCommentsAdapter!=null){
			mCommentsAdapter.notifyDataSetChanged();
		}
	}
	
	private static AdapterContextMenuInfo castAdapterContextMenuInfo(final ContextMenuInfo contextMenuInfo) {

		try {
			// Casts the incoming data object into the type for AdapterView objects.
			return (AdapterContextMenuInfo) contextMenuInfo;
		} catch (ClassCastException e) {
			// If the menu object can't be cast, logs an error.
			throw new RuntimeException("bad menuInfo: " + contextMenuInfo, e);
		}
	}

	private class PointCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final int COMMENTS_ID = 0x00;
		private static final int POINT_NAME_ID = 0x01;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			if (!arguments.containsKey(ExtraKey.POINT_ID)) {
				throw new IllegalArgumentException("Loader was called without point id");
			}
			int myPointId = arguments.getInt(ExtraKey.POINT_ID, Integer.MIN_VALUE);
			if (DEBUG) {
				Log.d(TAG, "[onCreateLoader] point id: " + myPointId);
			}
			switch (loaderId) {
				case COMMENTS_ID: {
					String where = Comments.Columns.POINT_ID + "=?";
					String[] args = new String[] { String.valueOf(myPointId),
							String.valueOf(mLoggedInPersonId) };
					return new CursorLoader(getActivity(), Comments.CONTENT_URI, null, where, args, null);
				}
				case POINT_NAME_ID: {
					String where = Points.Columns.ID + "=?";
					String[] args = new String[] { String.valueOf(myPointId) };
					String[] projection = new String[] { BaseColumns._ID, Points.Columns.NAME };
					return new CursorLoader(getActivity(), Points.CONTENT_URI, projection, where, args, null);
				}
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case COMMENTS_ID:
					mCommentsAdapter.swapCursor(null);
					break;
				case POINT_NAME_ID:
					mPointNameTextView.setText("");
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
			if (DEBUG) {
				Log.d(TAG, "[onLoadFinished] cursor count: " + data.getCount() + ", id: " + loader.getId());
			}
			
			switch (loader.getId()) {
				case COMMENTS_ID:
					mCommentsAdapter.swapCursor(data);
					updateCommentsStatus();
					break;
				case POINT_NAME_ID:
					if (data.moveToFirst()) {
						int nameColumnIndex = data.getColumnIndexOrThrow(Points.Columns.NAME);
						String name = data.getString(nameColumnIndex);
						mPointNameTextView.setText(name);
						getSherlockActivity().getSupportActionBar().setTitle(name);
					}
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}
	}
}
