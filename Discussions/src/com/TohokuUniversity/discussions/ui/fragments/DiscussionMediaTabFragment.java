package com.TohokuUniversity.discussions.ui.fragments;

import com.TohokuUniversity.discussions.R;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Attachments;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Discussions;
import com.TohokuUniversity.discussions.ui.ExtraKey;
import com.TohokuUniversity.discussions.ui.view.MediaGridView;
import com.actionbarsherlock.app.SherlockFragment;

public class DiscussionMediaTabFragment extends SherlockFragment {

	private static final String TAG = DiscussionMediaTabFragment.class.getSimpleName();
	private final AttachmentsCursorLoader mAttachmentsCursorLoader;
	// private MediaList mediaList;
	private MediaGridView mediaList;

	public DiscussionMediaTabFragment() {

		mAttachmentsCursorLoader = new AttachmentsCursorLoader();
	}

	/** Converts an intent into a {@link Bundle} suitable for use as fragment arguments. */
	public static Bundle intentToFragmentArguments(final Intent intent) {

		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}
		final String action = intent.getAction();
		if (action != null) {
			arguments.putString(ExtraKey.ACTION, action);
		}
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			arguments.putAll(intent.getExtras());
		}
		return arguments;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		registerForContextMenu(mediaList);
		mediaList.setAdapter(null);
		mediaList.setAttachmentsAdapter();
		initAttachmentsLoader();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
			final Bundle savedInstanceState) {

		mediaList = (MediaGridView) inflater.inflate(R.layout.fragment_discussion_media_grid_view, container,
				false);
		return mediaList;
	}

	private void initAttachmentsLoader() {

		Bundle args = new Bundle();
		Uri uri;
		if (getArguments() != null) {
			uri = getArguments().getParcelable(ExtraKey.URI);
		} else {
			uri = getActivity().getIntent().getData();
		}
		args.putParcelable(ExtraKey.URI, uri);
		getLoaderManager().initLoader(AttachmentsCursorLoader.ATTACHMENTS_ID, args, mAttachmentsCursorLoader);
	}

	private class AttachmentsCursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final int ATTACHMENTS_ID = 0x00;

		@Override
		public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle arguments) {

			switch (loaderId) {
				case ATTACHMENTS_ID: {
					String where = Attachments.Columns.DISCUSSION_ID + "=?";
					Uri discusionUri = arguments.getParcelable(ExtraKey.URI);
					int discusionId = Integer.valueOf(Discussions.getValueId(discusionUri));
					String[] args = new String[] { String.valueOf(discusionId) };
					return new CursorLoader(getActivity(), Attachments.CONTENT_URI, null, where, args, null);
				}
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loaderId);
			}
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {

			switch (loader.getId()) {
				case ATTACHMENTS_ID:
					mediaList.getAdapter().swapCursor(null);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {

			switch (loader.getId()) {
				case ATTACHMENTS_ID:
					mediaList.getAdapter().swapCursor(data);
					break;
				default:
					throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
			}
		}
	}
}
