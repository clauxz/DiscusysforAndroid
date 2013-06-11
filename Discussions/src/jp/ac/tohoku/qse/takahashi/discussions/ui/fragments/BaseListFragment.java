package jp.ac.tohoku.qse.takahashi.discussions.ui.fragments;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public abstract class BaseListFragment extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = BaseListFragment.class.getSimpleName();
	// This is the Adapter being used to display the list's data.
	protected SimpleCursorAdapter mAdapter;
	protected final String mColumnId;
	private final Uri mBaseUri;
	private int mCurCheckPosition = 0;
	private boolean mDualPane;
	private final int mEmptyListStringId;

	public BaseListFragment(final int mEmptyListStringId, final String mColumnId, final Uri baseUri) {

		super();
		this.mEmptyListStringId = mEmptyListStringId;
		this.mColumnId = mColumnId;
		mBaseUri = baseUri;
	}

	public void actionEdit(final int valueId) {

		Uri uri = ContentUris.withAppendedId(mBaseUri, valueId);
		Intent intent = new Intent(Intent.ACTION_EDIT, uri);
		Log.d(TAG, "[actionEdit] id: " + valueId + ", intent: " + intent);
		startActivity(intent);
	}

	public SimpleCursorAdapter getAdapter() {

		return mAdapter;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		Log.v(TAG, "[onActivityCreared] saved state: " + savedInstanceState);
		// registerForContextMenu(getListView());
		// Give some text to display if there is no data. In a real
		// application this would come from a resource.
		String emptyText = getResources().getString(mEmptyListStringId);
		setEmptyText(emptyText);
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);
		// Create an empty adapter we will use to display the loaded data.
		// mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.simple_list_item, null, new String[] {
		// mColumnName, Persons.Columns.COLOR }, new int[] { R.id.list_item_text,
		// R.id.image_person_color }, 0);
		// mAdapter.setViewBinder(new ColorTextViewBinder(mColumnName, Color.parseColor("#99CC00")));
		// setListAdapter(mAdapter);
		// Start out with a progress indicator.
		setListShown(false);
		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
		// Check to see if we have a frame in which to embed the details
		// fragment directly in the containing UI.
		View detailsFrame = getActivity().findViewById(R.id.frame_layout_details);
		mDualPane = (detailsFrame != null) && (detailsFrame.getVisibility() == View.VISIBLE);
		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
		}
		if (mDualPane) {
			// In dual-pane mode, the list view highlights the selected item.
			getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
			// Make sure our UI is in the correct state.
			showDetails(mCurCheckPosition);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		if (DEBUG) {
			Log.d(TAG, "[onCreateLoader] uri: " + getActivity().getIntent().getData());
		}
		return new CursorLoader(getActivity(), getActivity().getIntent().getData(), null, null, null, null);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		// showDetails(position);
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

	@Override
	public void onSaveInstanceState(final Bundle outState) {

		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", mCurCheckPosition);
	}

	protected abstract BaseDetailFragment getDetailFragment();

	protected int getItemId() {

		return getItemId(getSelectedItemPosition());
	}

	protected int getItemId(final android.view.MenuItem item) {

		AdapterView.AdapterContextMenuInfo info;
		try {
			// Casts the incoming data object into the type for AdapterView objects.
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			// If the menu object can't be cast, logs an error.
			throw new RuntimeException("bad menuInfo: " + item.getMenuInfo(), e);
		}
		return getItemId(info.position);
	}

	protected int getItemId(final int position) {

		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return -1;
		}
		int columnIndex = cursor.getColumnIndexOrThrow(mColumnId);
		return cursor.getInt(columnIndex);
	}

	/** Helper function to show the details of a selected item, either by displaying a fragment in-place in the
	 * current UI, or starting a whole new activity in which it is displayed. */
	protected void showDetails(final int position) {

		mCurCheckPosition = position;
		int valueId;
		if ((getAdapter().getCursor() != null) && getAdapter().getCursor().moveToPosition(position)) {
			int valueIdIndex = getAdapter().getCursor().getColumnIndexOrThrow(mColumnId);
			valueId = getAdapter().getCursor().getInt(valueIdIndex);
		} else {
			// valueId = BaseDetailsFragment.NO_SELECTION_ID;
			return;
		}
		if (mDualPane) {
			// We can display everything in-place with fragments, so update
			// the list to highlight the selected item and show the data.
			if (valueId != BaseDetailFragment.NO_SELECTION_ID) {
				getListView().setItemChecked(position, true);
			}
			// Check what fragment is currently shown, replace if needed.
			BaseDetailFragment details = (BaseDetailFragment) getFragmentManager().findFragmentById(
					R.id.frame_layout_details);
			if ((details == null) || (details.getShownId() != valueId)) {
				// Make new fragment to show this selection.
				details = getDetailFragment();
				details.setArgumentId(valueId);
				// Execute a transaction, replacing any existing fragment
				// with this one inside the frame.
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				ft.replace(R.id.frame_layout_details, details);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
		} else {
			// Otherwise we need to launch a new activity to display
			// the dialog fragment with selected text.
			Uri detailsUri = ContentUris.withAppendedId(mBaseUri, valueId);
			Intent intent = new Intent(Intent.ACTION_VIEW, detailsUri);
			startActivity(intent);
		}
	}
}
