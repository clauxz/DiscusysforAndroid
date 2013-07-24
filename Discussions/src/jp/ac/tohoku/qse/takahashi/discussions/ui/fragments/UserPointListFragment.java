package jp.ac.tohoku.qse.takahashi.discussions.ui.fragments;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

public class UserPointListFragment extends SherlockListFragment {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = UserPointListFragment.class.getSimpleName();
	private SimpleCursorAdapter mUserPointsAdapter;
	private int mDiscussionId;
	private int mPersonId;
	private int mTopicId;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		initFromIntentExtra();
		
		// Create an empty adapter we will use to display the loaded data.
		mUserPointsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_point, null,
				new String[] { Points.Columns.NAME, Points.Columns.ID, Points.Columns.ORDER_NUMBER,Points.Columns.ISNEW },
				new int[] { R.id.list_item_text, R.id.image_person_color, R.id.text_order_num, R.id.image_item_new }
		, 0);
		mUserPointsAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

				int viewId = view.getId();
				switch (viewId) {
					case R.id.image_person_color:
						ImageView colorView = (ImageView) view;
						int color = getActivity().getIntent().getExtras().getInt(ExtraKey.PERSON_COLOR);
						colorView.setBackgroundColor(color);
						return true;
					case R.id.list_item_text:
						TextView itemText = (TextView) view;
						itemText.setText(cursor.getString(columnIndex));
						return true;
					case R.id.text_order_num:
						TextView orderNumView = (TextView) view;
						orderNumView.setText(cursor.getString(columnIndex));
						return true;
					case R.id.image_item_new:
						{
							int index=cursor.getColumnIndex(Points.Columns.ISNEW);
							int isNew=cursor.getInt(index);
							
							if(ApplicationConstants.OBJECT_NEW==isNew){
								((ImageView)view).setImageBitmap(
										BitmapFactory.decodeResource(getResources(), R.drawable.ic_data_changed));
							}
							else
							{
								((ImageView)view).setImageBitmap(null);
							}
						}
						return true;
					default:
						return false;
				}
			}
		});
		setListAdapter(mUserPointsAdapter);
		// Prepare the loader. Either re-connect with an existing one, or start a new one.
		getLoaderManager().initLoader(UserPointsCursorLoader.LOADER_USER_POINTS_ID, null,
				new UserPointsCursorLoader());
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		onActionEdit(position);
		
	}

	private void initFromIntentExtra() {

		if (!getActivity().getIntent().hasExtra(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("Activity intent was without person id");
		}
		if (!getActivity().getIntent().hasExtra(ExtraKey.TOPIC_ID)) {
			throw new IllegalStateException("Activity intent was without topic id");
		}
		if (!getActivity().getIntent().hasExtra(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalStateException("Activity intent was without discussion id");
		}
		mDiscussionId = getActivity().getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID);
		mPersonId = getActivity().getIntent().getExtras().getInt(ExtraKey.PERSON_ID);
		mTopicId = getActivity().getIntent().getExtras().getInt(ExtraKey.TOPIC_ID);
		if (DEBUG) {
			Log.d(TAG, "[initFromIntentExtras] personId: " + mPersonId + ", topicId: " + mTopicId);
		}
	}

	private void onActionEdit(final int position) {

		if ((mUserPointsAdapter.getCursor() != null)
				&& mUserPointsAdapter.getCursor().moveToPosition(position)) {
			int valueIdIndex = mUserPointsAdapter.getCursor().getColumnIndexOrThrow(Points.Columns.ID);
			int pointId = mUserPointsAdapter.getCursor().getInt(valueIdIndex);
			Intent intent = createEditPointIntent(pointId);
			startActivity(intent);
		}
	}

	private Intent createEditPointIntent(final int pointId) {

		Intent intent = new Intent(Intent.ACTION_EDIT, Points.buildTableUri(pointId));
		intent.putExtra(ExtraKey.DISCUSSION_ID, mDiscussionId);
		intent.putExtra(ExtraKey.POINT_ID, pointId);
		intent.putExtra(ExtraKey.PERSON_ID, mPersonId);
		intent.putExtra(ExtraKey.TOPIC_ID, mTopicId);
		return intent;
	}

	private class UserPointsCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final int LOADER_USER_POINTS_ID = 0x05;

		@Override
		public Loader<Cursor> onCreateLoader(final int id, final Bundle arguments) {

			switch (id) {
				case LOADER_USER_POINTS_ID: {
					String where = Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID + "=? ";
					String[] args = { String.valueOf(mTopicId), String.valueOf(mPersonId) };
					String sortOrder = Points.Columns.ORDER_NUMBER + " ASC";
					return new CursorLoader(getActivity(), Points.CONTENT_URI, null, where, args, sortOrder);
				}
				default:
					throw new IllegalArgumentException("Unknown loader id: " + id);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case LOADER_USER_POINTS_ID:
					mUserPointsAdapter.swapCursor(null);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case LOADER_USER_POINTS_ID:
					mUserPointsAdapter.swapCursor(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}
	}
}
