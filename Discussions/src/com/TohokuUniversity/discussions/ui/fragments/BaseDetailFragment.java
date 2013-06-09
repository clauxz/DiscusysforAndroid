package com.TohokuUniversity.discussions.ui.fragments;


import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.TohokuUniversity.discussions.ui.ExtraKey;
import com.actionbarsherlock.app.SherlockFragment;

/** Override onCreate view to add your own details view. */
public abstract class BaseDetailFragment extends SherlockFragment {

	protected static final int NO_SELECTION_ID = -1;
	private final Uri mBaseUri;

	public BaseDetailFragment(final Uri baseUri) {

		super();
		this.mBaseUri = baseUri;
	}

	public Uri getDetailsUri() {

		if (getArguments().containsKey(ExtraKey.ID)) {
			return ContentUris.withAppendedId(mBaseUri, getArguments().getInt(ExtraKey.ID,
					NO_SELECTION_ID));
		}
		return getActivity().getIntent().getData();
	}

	public int getShownId() {

		return getArguments().getInt(ExtraKey.ID);
	}

	@Override
	public abstract View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	public void setArgumentId(final int id) {

		Bundle args = new Bundle();
		args.putInt(ExtraKey.ID, id);
		setArguments(args);
	}
}