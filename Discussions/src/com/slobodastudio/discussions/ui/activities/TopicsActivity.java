package com.slobodastudio.discussions.ui.activities;

import com.slobodastudio.discussions.R;

import android.os.Bundle;

public class TopicsActivity extends BaseActivity {

	@Override
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_topics);
	}
}
