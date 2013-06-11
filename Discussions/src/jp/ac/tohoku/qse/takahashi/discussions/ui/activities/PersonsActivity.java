package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.data.SharedPreferenceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.utils.fragmentasynctask.SyncStatusUpdaterFragment;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class PersonsActivity extends BaseActivity {

	private static final String TAG = PersonsActivity.class.getSimpleName();
	private boolean mIsActivityCreated;
	private SyncStatusUpdaterFragment mSyncStatusUpdaterFragment;
	
	private Dialog mServerAddressDialog;
	
	public PersonsActivity() {

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
			long currentTime = System.currentTimeMillis();
			long lastUpdatedTime = SharedPreferenceHelper.getUpdatedTime(this);
			long fiveMinutes = 300000;
			if ((currentTime - lastUpdatedTime) > fiveMinutes) {
				triggerRefresh();
			}
			mIsActivityCreated = false;
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		if (DEBUG) {
			Log.d(TAG, "[onCreate] action main: " + getIntent().getAction().equals(Intent.ACTION_MAIN));
		}
		if (getIntent().getAction().equals(Intent.ACTION_MAIN)) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
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
		setTitle(R.string.activity_title_persons);
		setContentView(R.layout.activity_persons);
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

		String versionName;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			throw new RuntimeException();
		}
		Toast.makeText(this, getString(R.string.toast_version, versionName), Toast.LENGTH_SHORT).show();
	}

	public void triggerRefresh() {

		mServiceHelper.downloadAll(mSyncStatusUpdaterFragment.getReceiver());
	}
}
