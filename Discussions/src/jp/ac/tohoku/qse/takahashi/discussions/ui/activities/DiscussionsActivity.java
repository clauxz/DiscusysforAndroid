package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.R;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;

import android.os.Bundle;
import android.util.Log;

public class DiscussionsActivity extends BaseActivity {

	private int mSessionId;
	
	@Override
	protected void onControlServiceConnected() {

		// No operation with service in this activity
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_discussions);
		
		
		mSessionId=getIntent().getExtras().getInt(ExtraKey.SESSION_ID);
		
		
	}
	
	public int getSessionID(){
		return mSessionId;
	}
}
