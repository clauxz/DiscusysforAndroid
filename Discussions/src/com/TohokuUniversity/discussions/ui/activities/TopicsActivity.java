package com.TohokuUniversity.discussions.ui.activities;

import com.TohokuUniversity.discussions.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Discussions;
import com.TohokuUniversity.discussions.ui.ExtraKey;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TopicsActivity extends BaseActivity {

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_topics, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_discussion_info:
				startDiscussionInfoActivity();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_topics);
	}

	private void startDiscussionInfoActivity() {

		int discussionId = getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
		Uri discussionUri = Discussions.buildTableUri(discussionId);
		Intent discussionInfoIntent = new Intent(Intent.ACTION_VIEW, discussionUri, this,
				DiscussionInfoActivity.class);
		startActivity(discussionInfoIntent);
	}
}
