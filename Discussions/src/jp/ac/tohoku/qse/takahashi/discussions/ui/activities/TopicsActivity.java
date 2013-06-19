package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Discussions;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.R.integer;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TopicsActivity extends BaseActivity {

	private int mSessionId;
	
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
		
		mSessionId=getIntent().getExtras().getInt(ExtraKey.SESSION_ID);
	}

	private void startDiscussionInfoActivity() {

		int discussionId = getIntent().getExtras().getInt(ExtraKey.DISCUSSION_ID, Integer.MIN_VALUE);
		Uri discussionUri = Discussions.buildTableUri(discussionId);
		Intent discussionInfoIntent = new Intent(Intent.ACTION_VIEW, discussionUri, this,
				DiscussionInfoActivity.class);
		
		Bundle bundle=new Bundle();
		bundle.putInt(ExtraKey.SESSION_ID,mSessionId);
		discussionInfoIntent.putExtras(bundle);
		
		startActivity(discussionInfoIntent);
	}
}
