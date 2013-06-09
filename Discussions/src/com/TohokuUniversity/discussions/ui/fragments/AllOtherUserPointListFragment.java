package com.TohokuUniversity.discussions.ui.fragments;

import com.TohokuUniversity.discussions.R;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.TohokuUniversity.discussions.ApplicationConstants;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Persons;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Points;
import com.TohokuUniversity.discussions.ui.ExtraKey;
import com.actionbarsherlock.app.SherlockListFragment;

public class AllOtherUserPointListFragment extends SherlockListFragment {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = AllOtherUserPointListFragment.class.getSimpleName();
	private int mDiscussionId;
	private int mOriginPersonId;
	private SimpleCursorAdapter mOtherPointsAdapter;
	private int mPersonId;
	private TextView mPointListTitleTextView;
	private int mTopicId;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		initFromIntentExtra();
		// Create an empty adapter we will use to display the loaded data.
		mOtherPointsAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_point, null,
				new String[] { Points.Columns.NAME, Persons.Columns.COLOR, Points.Columns.ORDER_NUMBER },
				new int[] { R.id.list_item_text, R.id.image_person_color, R.id.text_order_num }, 0);
		mOtherPointsAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(final View view, final Cursor cursor, final int columnIndex) {

				int viewId = view.getId();
				switch (viewId) {
					case R.id.image_person_color:
						ImageView colorView = (ImageView) view;
						colorView.setBackgroundColor(cursor.getInt(columnIndex));
						return true;
					case R.id.list_item_text:
						TextView itemText = (TextView) view;
						itemText.setText(cursor.getString(columnIndex));
						return true;
					case R.id.text_order_num:
						TextView orderNumView = (TextView) view;
						orderNumView.setText(cursor.getString(columnIndex));
						return true;
					default:
						return false;
				}
			}
		});
		setListAdapter(mOtherPointsAdapter);
		// Prepare the loader. Either re-connect with an existing one, or start a new one.
		getLoaderManager().initLoader(OtherUserPointsCursorLoader.LOADER_OTHER_POINTS_ID, null,
				new OtherUserPointsCursorLoader());
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		onActionView(position);
	}

	private void addListHeader() {

		LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View headerView = layoutInflater.inflate(R.layout.view_point_list_header, null, false);
		mPointListTitleTextView = (TextView) headerView.findViewById(R.id.points_listview_header);
		mPointListTitleTextView.setText(R.string.text_other_users_points);
		getListView().addHeaderView(headerView);
	}

	private Intent createViewPointIntent(final int pointId) {

		Intent intent = new Intent(Intent.ACTION_VIEW, Points.buildTableUri(pointId));
		intent.putExtra(ExtraKey.POINT_ID, pointId);
		intent.putExtra(ExtraKey.ORIGIN_PERSON_ID, mOriginPersonId);
		intent.putExtra(ExtraKey.PERSON_ID, mPersonId);
		intent.putExtra(ExtraKey.TOPIC_ID, mTopicId);
		intent.putExtra(ExtraKey.DISCUSSION_ID, mDiscussionId);
		return intent;
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
		if (!getArguments().containsKey(ExtraKey.PERSON_ID)) {
			throw new IllegalStateException("Arguments was without person id");
		}
		if (!getArguments().containsKey(ExtraKey.ORIGIN_PERSON_ID)) {
			throw new IllegalStateException("Arguments was without origin person id");
		}
		mDiscussionId = getActivity().getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID);
		mPersonId = getArguments().getInt(ExtraKey.PERSON_ID);
		mOriginPersonId = getArguments().getInt(ExtraKey.ORIGIN_PERSON_ID);
		mTopicId = getActivity().getIntent().getExtras().getInt(ExtraKey.TOPIC_ID);
		if (DEBUG) {
			Log.d(TAG, "[initFromIntentExtras] personId: " + mPersonId + ", topicId: " + mTopicId);
		}
	}

	private void onActionView(final int position) {

		if ((mOtherPointsAdapter.getCursor() != null)
				&& mOtherPointsAdapter.getCursor().moveToPosition(position)) {
			int valueIdIndex = mOtherPointsAdapter.getCursor().getColumnIndexOrThrow(Points.Columns.ID);
			int valueId = mOtherPointsAdapter.getCursor().getInt(valueIdIndex);
			// Otherwise we need to launch a new activity to display details
			Intent intent = createViewPointIntent(valueId);
			startActivity(intent);
		}
	}

	private class OtherUserPointsCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final int LOADER_OTHER_POINTS_ID = 1;

		@Override
		public Loader<Cursor> onCreateLoader(final int id, final Bundle arguments) {

			switch (id) {
				case LOADER_OTHER_POINTS_ID: {
					String where = Points.Columns.TOPIC_ID + "=? AND " + Points.Columns.PERSON_ID
							+ "!=? AND " + Points.Columns.PERSON_ID + "=" + Persons.Qualified.PERSON_ID;
					String[] args = { String.valueOf(mTopicId), String.valueOf(mOriginPersonId) };
					String sortOrder = Points.Columns.PERSON_ID + "," + Points.Columns.ORDER_NUMBER + " ASC";
					return new CursorLoader(getActivity(), Points.CONTENT_AND_PERSON_URI, null, where, args,
							sortOrder);
				}
				default:
					throw new IllegalArgumentException("Unknown loader id: " + id);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case LOADER_OTHER_POINTS_ID:
					mOtherPointsAdapter.swapCursor(null);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case LOADER_OTHER_POINTS_ID:
					mOtherPointsAdapter.swapCursor(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}
	}
}
