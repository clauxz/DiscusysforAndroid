package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.data.PreferenceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.ArgPointChanged;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.SelectedPoint;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.photon.DiscussionUser;
import jp.ac.tohoku.qse.takahashi.discussions.photon.PhotonServiceCallback;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MyLog;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;

public class CommentDetailsActivity extends BaseActivity implements PhotonServiceCallback  {

	private static final String TAG = PointsActivity.class.getSimpleName();
	private static final String EXTRA_URI = "extras_uri";
	private TextView commentTextView;
	private ImageView personColorImageView;
	private TextView personNameTextView;

	//private int mTopicId;
	private int mLoggedPersonID;
	private int mSelectedCommentId;
	private boolean mIsNewComment;
	
	private SelectedPoint mSelectedPoint;
	
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onControlServiceConnected() {

		markReadedComment();
	}
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		initFromIntentExtra();
		Log.i("Disc",CommentDetailsActivity.class.getSimpleName()+" onCreate");
		setContentView(R.layout.activity_comment);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		commentTextView = (TextView) findViewById(R.id.textViewComment);
		personColorImageView = (ImageView) findViewById(R.id.image_person_color);
		personNameTextView = (TextView) findViewById(R.id.text_comment_person_name);
		startCommentsLoader();
	}

	
	private void connectPhoton() {

		Log.i("Disc","connectPhoton: " + mBound + " " + mService.getPhotonController().isConnected());
		MyLog.v(TAG, "connectPhoton: " + mBound + " " + mService.getPhotonController().isConnected());
		/*
		if (mBound && !mService.getPhotonController().isConnected()) {
			mService.getPhotonController().connect(this, mDiscussionId,
					PreferenceHelper.getPhotonDbAddress(this), mPersonName, mPersonId);
			mService.getPhotonController().getCallbackHandler().addCallbackListener(this);
		}
		//*/
	}
	
	private void startCommentsLoader() {

		CommentLoader loader = new CommentLoader();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_URI, getIntent().getData());
		getSupportLoaderManager().initLoader(CommentLoader.COMMENT_ID, args, loader);
	}

	private void initFromIntentExtra() {

		
		if (!getIntent().hasExtra(ExtraKey.ORIGIN_PERSON_ID)) {
			throw new IllegalStateException("Activity intent was without person id");
		}
		/*
		if (!getIntent().hasExtra(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("Activity intent was without topic id");
		}
		//*/
		if (!getIntent().hasExtra(ExtraKey.SELECTED_POINT)) {
			throw new IllegalStateException("Activity intent was without point id");
		}
		if (!getIntent().hasExtra(ExtraKey.SELECTED_COMMENT)) {
			throw new IllegalStateException("Activity intent was without selected id");
		}
		if (!getIntent().hasExtra(ExtraKey.COMMENT_NEW_FLAG)) {
			throw new IllegalStateException("Activity intent was without comment ISNEW flag");
		}
		/*
		if(getIntent().hasExtra(ExtraKey.SESSION_ID)
				&& getIntent().getExtras().getInt(ExtraKey.SESSION_ID)!=0) // zore session is not exist ( not experiment mode)
		{
			this.showExtMenu=true;
			mSessionId=getIntent().getExtras().getInt(ExtraKey.SESSION_ID);
			Log.v("Discussions","[Session ID] exists");
		}
		else
		{
			this.showExtMenu=false;
			mSessionId=Integer.MIN_VALUE;
			Log.v("Discussions","[Session ID] not exists");
		}
		//*/
		mLoggedPersonID = getIntent().getExtras().getInt(ExtraKey.ORIGIN_PERSON_ID);
		//mTopicId = getIntent().getExtras().getInt(ExtraKey.TOPIC_ID);
		mSelectedPoint = getIntent().getExtras().getParcelable(ExtraKey.SELECTED_POINT);
		mSelectedCommentId=getIntent().getExtras().getInt(ExtraKey.SELECTED_COMMENT);
		mIsNewComment=getIntent().getExtras().getBoolean(ExtraKey.COMMENT_NEW_FLAG);
		
		//Log.i("Disc","onCreate");
		
		/*
		if (mTopicId == -1) {
			throw new IllegalStateException("Activity intent has illegal topic id -1");
		}
		/*/
		/*
		mDiscussionId = getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID);
		if (DEBUG) {
			Log.d(TAG, "[initFromIntentExtras] personId: " + mPersonId + ", topicId: " + mTopicId
					+ ", discussionId: " + mDiscussionId + ", personName: " + mPersonName);
		}
		//*/
	}

	/*
	private void initVars(Bundle bundle){
		if(bundle!=null)
		{
			mLoggedPersonID=bundle.getInt(ExtraKey.ORIGIN_PERSON_ID);
			mTopicId=bundle.getInt(ExtraKey.TOPIC_ID, Integer.MIN_VALUE);
			mSelectedCommentId=bundle.getInt(ExtraKey.SELECTED_COMMENT);
			mIsNewComment=bundle.getBoolean(ExtraKey.COMMENT_NEW_FLAG);
			
			Log.i("Disc COMEMNT","PERSON ID:"+String.valueOf(mLoggedPersonID)
					+" COMMENT ID:"+String.valueOf(mSelectedCommentId)+" IS NEW:"
					+String.valueOf(mIsNewComment));
			
			mSelectedPoint = bundle.getParcelable(ExtraKey.SELECTED_POINT);
		}
		else
		{
			mLoggedPersonID=Integer.MIN_VALUE;
			mSelectedCommentId=Integer.MIN_VALUE;
			mIsNewComment=false;
		}	
	}
	//*/
	
	private void markReadedComment()
	{
		Bundle commentValues=new Bundle();
		commentValues.putInt(CommentsPersonReadEntry.Columns.COMMENT_ID, mSelectedCommentId);
		commentValues.putInt(CommentsPersonReadEntry.Columns.PERSON_ID, mLoggedPersonID);
		//*
		Log.i("Disc markReadedComment","commentValues:"+String.valueOf(commentValues));
		Log.i("Disc markReadedComment","mSelectedPoint:"+String.valueOf(mSelectedPoint));
		//mConnection
		//mService
		Log.i("Disc markReadedComment","mConnection:"+String.valueOf(mConnection));
		Log.i("Disc markReadedComment","mService:"+String.valueOf(mService));
		Log.i("Disc markReadedComment","getServiceHelper():"+String.valueOf(getServiceHelper()));
		//*/
		
		getServiceHelper().insertCommentPersonReadedEntry(commentValues, mSelectedPoint);
		
		Log.i("Disc","markReadedComment");
	}
	
	private class CommentLoader implements LoaderCallbacks<Cursor> {

		private static final int COMMENT_ID = 0x00;
		private static final int PERSON_ID = 0x01;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case COMMENT_ID:
					return getCommentLoader(arguments);
				case PERSON_ID:
					return getPersonLoader(arguments);
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case COMMENT_ID:
					commentTextView.setText("");
					break;
				case PERSON_ID:
					personNameTextView.setText("");
					personColorImageView.setBackgroundColor(0);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case COMMENT_ID:
					swapComment(data);
					break;
				case PERSON_ID:
					swapPerson(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		private CursorLoader getCommentLoader(final Bundle arguments) {

			Uri commentsUri = getUriFromArguments(arguments);
			return new CursorLoader(CommentDetailsActivity.this, commentsUri, null, null, null, null);
		}

		private CursorLoader getPersonLoader(final Bundle arguments) {

			Uri personUri = getUriFromArguments(arguments);
			return new CursorLoader(CommentDetailsActivity.this, personUri, null, null, null, null);
		}

		private Uri getUriFromArguments(final Bundle arguments) {

			if (!arguments.containsKey(EXTRA_URI)) {
				throw new IllegalArgumentException("Loader was called without extra discussion uri");
			}
			return arguments.getParcelable(EXTRA_URI);
		}

		private void startPersonLoader(final int personId) {

			CommentLoader loader = new CommentLoader();
			Bundle args = new Bundle();
			args.putParcelable(EXTRA_URI, Persons.buildTableUri(personId));
			getSupportLoaderManager().initLoader(CommentLoader.PERSON_ID, args, loader);
		}

		private void swapComment(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int commentColumn = cursor.getColumnIndexOrThrow(Comments.Columns.TEXT);
				int personIdColumn = cursor.getColumnIndexOrThrow(Comments.Columns.PERSON_ID);
				String comment = cursor.getString(commentColumn);
				commentTextView.setText(comment);
				int personId = cursor.getInt(personIdColumn);
				startPersonLoader(personId);
			}
		}

		private void swapPerson(final Cursor cursor) {

			if (cursor.moveToFirst()) {
				int personNameColumn = cursor.getColumnIndexOrThrow(Persons.Columns.NAME);
				int personColorColumn = cursor.getColumnIndexOrThrow(Persons.Columns.COLOR);
				String name = cursor.getString(personNameColumn);
				personNameTextView.setText(name);
				int color = cursor.getInt(personColorColumn);
				personColorImageView.setBackgroundColor(color);
			}
		}
	}

	
	
	
	
	
	@Override
	public void onArgPointChanged(ArgPointChanged argPointChanged) {
		
		if (DEBUG) {
			Log.d(TAG, "[onArgPointChanged] Empty point id: " + argPointChanged.getPointId());
		}
	}

	@Override
	public void onConnect() {
		
		if (DEBUG) {
			Log.d(TAG, "[onConnect] Empty. ");
		}
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				//pagerTitleStrip.setTextColor(Color.BLACK);// visualize photon is in offline mode
			}
		});
		
	}

	@Override
	public void onErrorOccured(String message) {
		
		Log.e(TAG, "[onErrorOccured] Empty. message: " + message);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {

				//pagerTitleStrip.setTextColor(Color.RED);// visualize photon is in offline mode
			}
		});
		
	}

	@Override
	public void onEventJoin(DiscussionUser newUser) {
		
		if (DEBUG) {
			Log.d(TAG, "[onEventJoin] Empty. user come: " + newUser.getUserName());
		}
	}

	@Override
	public void onEventLeave(DiscussionUser leftUser) {
		
		if (DEBUG) {
			Log.d(TAG, "[onEventLeave] Empty. user left: " + leftUser.getUserName());
		}
		
	}

	@Override
	public void onRefreshCurrentTopic() {
		
		if (DEBUG) {
			Log.d(TAG, "[onRefreshCurrentTopic]");// topic id: " + mTopicId);
		}
		//mServiceHelper.downloadPointsFromTopic(mTopicId);
	}
	

	@Override
	public void onStructureChanged(int changedTopicId) {
		
		if (DEBUG) {
			Log.d(TAG, "[onStructureChanged] Empty. topic id: " + changedTopicId);
		}
	}
}