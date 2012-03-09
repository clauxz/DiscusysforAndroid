package com.slobodastudio.discussions.ui.fragments;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.utils.MyLog;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public abstract class BaseListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	protected final String mColumnId;
	private final Uri baseUri;
	// This is the Adapter being used to display the list's data.
	private SimpleCursorAdapter mAdapter;
	private final String mColumnName;
	private int mCurCheckPosition = 0;
	private boolean mDualPane;
	private final int mEmptyListStringId;

	public BaseListFragment(final int mEmptyListStringId, final String mColumnName, final String mColumnId,
			final Uri baseUri) {

		super();
		this.mEmptyListStringId = mEmptyListStringId;
		this.mColumnName = mColumnName;
		this.mColumnId = mColumnId;
		this.baseUri = baseUri;
	}

	public SimpleCursorAdapter getAdapter() {

		return mAdapter;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(getListView());
		// Give some text to display if there is no data. In a real
		// application this would come from a resource.
		String emptyText = getResources().getString(mEmptyListStringId);
		setEmptyText(emptyText);
		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);
		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.simple_list_item, null,
				new String[] { mColumnName }, new int[] { R.id.list_item_text }, 0);
		setListAdapter(mAdapter);
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
	public boolean onContextItemSelected(final MenuItem item) {

		MyLog.v("Fragment", (String) item.getTitle());
		switch (item.getItemId()) {
			case R.id.menu_details:
				Toast.makeText(getActivity(), "Details pressed", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menu_discussions:
				Toast.makeText(getActivity(), "Discussions pressed", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menu_persons:
				Toast.makeText(getActivity(), "Persons pressed", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menu_points:
				Toast.makeText(getActivity(), "Points pressed", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menu_topics:
				Toast.makeText(getActivity(), "Topics pressed", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menu_edit:
				Toast.makeText(getActivity(), "Edit pressed", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menu_delete:
				Toast.makeText(getActivity(), "Delete pressed", Toast.LENGTH_SHORT).show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo info;
		try {
			// Casts the incoming data object into the type for AdapterView objects.
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			// If the menu object can't be cast, logs an error.
			throw new RuntimeException("bad menuInfo: " + menuInfo, e);
		}
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return;
		}
		int columnIndex = cursor.getColumnIndexOrThrow(mColumnName);
		menu.setHeaderTitle(cursor.getString(columnIndex));// if your table name is name
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.list_context_menu, menu);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {

		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(getActivity(), getActivity().getIntent().getData(), null, null, null, null);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		showDetails(position);
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
		// old cursor once we return.)
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

	/** Helper function to show the details of a selected item, either by displaying a fragment in-place in the
	 * current UI, or starting a whole new activity in which it is displayed. */
	void showDetails(final int position) {

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
			if (valueId != BaseDetailsFragment.NO_SELECTION_ID) {
				getListView().setItemChecked(position, true);
			}
			// Check what fragment is currently shown, replace if needed.
			BaseDetailsFragment details = (BaseDetailsFragment) getFragmentManager().findFragmentById(
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
			Uri detailsUri = ContentUris.withAppendedId(baseUri, valueId);
			Intent intent = new Intent(Intent.ACTION_VIEW, detailsUri);
			startActivity(intent);
		}
	}

	protected abstract BaseDetailsFragment getDetailFragment();

	protected int getItemId(final MenuItem item) {

		AdapterView.AdapterContextMenuInfo info;
		try {
			// Casts the incoming data object into the type for AdapterView objects.
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			// If the menu object can't be cast, logs an error.
			throw new RuntimeException("bad menuInfo: " + item.getMenuInfo(), e);
		}
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return -1;
		}
		int columnIndex = cursor.getColumnIndexOrThrow(mColumnId);
		return cursor.getInt(columnIndex);
	}
}
