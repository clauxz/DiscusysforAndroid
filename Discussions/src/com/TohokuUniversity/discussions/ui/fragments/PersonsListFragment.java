package com.TohokuUniversity.discussions.ui.fragments;

import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Persons;
import com.TohokuUniversity.discussions.ui.ExtraKey;
import com.TohokuUniversity.discussions.R;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class PersonsListFragment extends BaseListFragment {

	public PersonsListFragment() {

		super(R.string.text_empty_persons_list, Persons.Columns.ID, Persons.CONTENT_URI);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_base, null, new String[] {
				Persons.Columns.NAME, Persons.Columns.COLOR }, new int[] { R.id.list_item_text,
				R.id.image_person_color }, 0);
		mAdapter.setViewBinder(new ViewBinder() {

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
					default:
						return false;
				}
			}
		});
		setListAdapter(mAdapter);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {

		super.onListItemClick(l, v, position, id);
		Uri uri = Persons.buildDiscussionUri(getItemId(position));
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra(ExtraKey.PERSON_ID, getItemId(position));
		intent.putExtra(ExtraKey.PERSON_NAME, getItemName(position));
		intent.putExtra(ExtraKey.PERSON_COLOR, getItemColor(position));
		startActivity(intent);
	}

	@Override
	protected BaseDetailFragment getDetailFragment() {

		return null;
	}

	protected int getItemColor(final int position) {

		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			throw new NullPointerException("Cant read person color from null cursor");
		}
		int columnIndex = cursor.getColumnIndexOrThrow(Persons.Columns.COLOR);
		return cursor.getInt(columnIndex);
	}

	protected String getItemName(final int position) {

		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		if (cursor == null) {
			// For some reason the requested item isn't available, do nothing
			return null;
		}
		int columnIndex = cursor.getColumnIndexOrThrow(Persons.Columns.NAME);
		return cursor.getString(columnIndex);
	}
}
