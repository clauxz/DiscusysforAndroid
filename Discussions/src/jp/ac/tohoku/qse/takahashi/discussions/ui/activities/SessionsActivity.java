package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Seats;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sessions;
import jp.ac.tohoku.qse.takahashi.discussions.ui.ExtraKey;
import jp.ac.tohoku.qse.takahashi.discussions.ui.OnDownloadCompleteListener;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MyLog;
import jp.ac.tohoku.qse.takahashi.discussions.utils.fragmentasynctask.SyncStatusUpdaterFragment;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class SessionsActivity extends BaseActivity implements OnDownloadCompleteListener {

	private static final String TAG = SessionsActivity.class.getSimpleName();
	private boolean mIsActivityCreated;
	// TODO: separate same with PersonsActivity code to one BaseMainActivity
	private SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;
	private int sessionIdPending = Integer.MIN_VALUE;

	public SessionsActivity() {

		// default values
		mIsActivityCreated = false;
	}

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_main_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_refresh:
				triggerRefresh();
				return true;
			case R.id.menu_settings:
				Intent preferenceIntent = new Intent(this, DiscusysPreferenceActivity.class);
				startActivity(preferenceIntent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onControlServiceConnected() {

		if (DEBUG) {
			Log.d(TAG, "[onControlServiceConnected] action main: " + mIsActivityCreated + ", isBound: "
					+ mBound);
		}
		if (mIsActivityCreated && mBound) {
			// when app first run
			triggerRefresh();
			mIsActivityCreated = false;
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		if (DEBUG) {
			Log.d(TAG, "[onCreate] action main: " + getIntent().getAction().equals(Intent.ACTION_MAIN));
		}
		if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Sessions.CONTENT_URI);
			setIntent(intent);
			if (savedInstanceState == null) {
				// first time activity created
				showCurrentVersionInToast();
				mIsActivityCreated = true;
			}
		}
		super.onCreate(savedInstanceState);
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		setTitle(R.string.activity_title_sessions);
		setContentView(R.layout.activity_sessions);
		// AnalyticsUtils.getInstance(this).trackPageView("/Home");
		FragmentManager fm = getSupportFragmentManager();
		mSyncStatusUpdaterFragment = (SyncStatusUpdaterFragment) fm
				.findFragmentByTag(SyncStatusUpdaterFragment.TAG);
		if (mSyncStatusUpdaterFragment == null) {
			mSyncStatusUpdaterFragment = new SyncStatusUpdaterFragment();
			fm.beginTransaction().add(mSyncStatusUpdaterFragment, SyncStatusUpdaterFragment.TAG).commit();
			// TODO should be called here triggerRefresh();
		}
	}

	private void showCurrentVersionInToast() {

		try {
			String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			Toast.makeText(this, getString(R.string.toast_version, versionName), Toast.LENGTH_SHORT).show();
		} catch (NameNotFoundException e) {
			MyLog.e(TAG, "Failed to get package info name", e);
		}
	}

	private void triggerRefresh() {

		if (mBound) {
			mServiceHelper.downloadSessions();
		}
	}

	public void triggerDownloadPerSession(final int sessionId) {

		if (mBound) {
			sessionIdPending = sessionId;
			mSyncStatusUpdaterFragment.setDownloadCompleteListener(this);
			mServiceHelper.downloadAllPerSession(mSyncStatusUpdaterFragment.getReceiver(), sessionId);
		}
	}

	@Override
	public void onDownloadComplete() {

		Intent intent = new Intent(Intent.ACTION_VIEW, Seats.CONTENT_URI);
		intent.putExtra(ExtraKey.SESSION_ID, sessionIdPending);
		startActivity(intent);
	}
}
