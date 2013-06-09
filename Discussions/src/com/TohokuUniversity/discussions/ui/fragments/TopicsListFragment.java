package com.TohokuUniversity.discussions.ui.fragments;

import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Topics;
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

public class TopicsListFragment extends BaseListFragment {

	public TopicsListFragment() {

		super(R.string.text_empty_topics_list, Topics.Columns.ID, Topics.CONTENT_URI);
	}

	private static void validateExtras(final Bundle extras) {

		if (extras == null) {
			throw new NullPointerException("Extras was null");
		}
		if (!extras.containsKey(ExtraKey.PERSON_ID)) {
			throw new IllegalArgumentException("Extras doesnt contain person id");
		}
		if (!extras.containsKey(ExtraKey.PERSON_COLOR)) {
			throw new IllegalArgumentException("Extras doesnt contain person color");
		}
		if (!extras.containsKey(ExtraKey.PERSON_NAME)) {
			throw new IllegalArgumentException("Extras doesnt contain person name");
		}
		if (!extras.containsKey(ExtraKey.DISCUSSION_ID)) {
			throw new IllegalArgumentException("Extras doesnt contain discussion id");
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_base, null, new String[] {
				Topics.Columns.NAME, Topics.Columns.ID }, new int[] { R.id.list_item_text,
				R.id.image_person_color }, 0);
		mAdapter.setViewBinder(new ViewBinder() {

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
		Intent intent = createPointIntent(getItemId(position));
		startActivity(intent);
	}

	@Override
	protected BaseDetailFragment getDetailFragment() {

		return null;
	}

	private Intent createPointIntent(final int pointId) {

		Bundle extras = getActivity().getIntent().getExtras();
		validateExtras(extras);
		Uri uri = Topics.buildPointUri(pointId);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		int personId = extras.getInt(ExtraKey.PERSON_ID, Integer.MIN_VALUE);
		int personColor = extras.getInt(ExtraKey.PERSON_COLOR, Integer.MIN_VALUE);
		String personName = extras.getString(ExtraKey.PERSON_NAME);
		int discussionId = extras.getInt(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
		intent.putExtra(ExtraKey.PERSON_ID, personId);
		intent.putExtra(ExtraKey.PERSON_COLOR, personColor);
		intent.putExtra(ExtraKey.PERSON_NAME, personName);
		intent.putExtra(ExtraKey.DISCUSSION_ID, discussionId);
		intent.putExtra(ExtraKey.TOPIC_ID, pointId);
		return intent;
	}
}
