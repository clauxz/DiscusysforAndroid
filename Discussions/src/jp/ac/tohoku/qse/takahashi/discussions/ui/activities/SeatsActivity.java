package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.R;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;

import android.os.Bundle;
import android.util.Log;

public class SeatsActivity extends BaseActivity {

	@Override
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_seats);
		
		int sesid=getIntent().getExtras().getInt(ExtraKey.SESSION_ID);
	}
}
