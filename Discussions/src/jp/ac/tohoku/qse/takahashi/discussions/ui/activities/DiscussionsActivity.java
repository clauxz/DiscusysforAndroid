package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.os.Bundle;

public class DiscussionsActivity extends BaseActivity {

	@Override
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discussions);
	}
}
