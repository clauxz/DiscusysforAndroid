package jp.ac.tohoku.qse.takahashi.discussions.ui.fragments;

import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sessions;
import jp.ac.tohoku.qse.takahashi.discussions.ui.activities.SessionsActivity;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class SessionsListFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String COLUMN_ID = Sessions.Columns.ID;
	private static final int EMPTY_STRING_RES_ID = R.string.text_empty_sessions_list;
	private static final Uri LIST_URI = Sessions.CONTENT_URI;
	private static final String TAG = SessionsListFragment.class.getSimpleName();
	/** This is the Adapter being used to display the list's data. */
	private SimpleCursorAdapter mAdapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		// Give some text to display if there is no data.
		setEmptyText(getString(EMPTY_STRING_RES_ID));
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);
		mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null,
				new String[] { Sessions.Columns.NAME }, new int[] { android.R.id.text1 }, 0);
		setListAdapter(mAdapter);
		// Start out with a progress indicator.
		setListShown(false);
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(getActivity(), LIST_URI, new String[] { BaseColumns._ID, Sessions.Columns.ID,
				Sessions.Columns.NAME }, null, null, null);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		int sessionId = getItemId(position);
		SessionsActivity activity = (SessionsActivity) getActivity();
		activity.triggerDownloadPerSession(sessionId);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {

		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.
		mAdapter.swapCursor(data);
		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	protected int getItemId(final int position) {

		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return -1;
		}
		int columnIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
		return cursor.getInt(columnIndex);
	}
}
