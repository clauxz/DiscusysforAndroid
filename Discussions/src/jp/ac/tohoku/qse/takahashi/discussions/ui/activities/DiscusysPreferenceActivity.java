package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.data.PreferenceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.data.PreferenceKey;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.service.ControlService;
import jp.ac.tohoku.qse.takahashi.discussions.service.ServiceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.service.ControlService.LocalBinder;
import jp.ac.tohoku.qse.takahashi.discussions.utils.fragmentasynctask.DetachableResultReceiver;
import jp.ac.tohoku.qse.takahashi.discussions.utils.fragmentasynctask.ResultCodes;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class DiscusysPreferenceActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener, DetachableResultReceiver.Receiver {

	/** 100 is default value for ProggressDialog */
	int maxProgress = 100;
	protected boolean mBound = false;
	protected final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className, final IBinder service) {

			// We've bound to PhotonService, cast the IBinder and get PhotonService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mServiceHelper = mService.getServiceHelper();
			mBound = true;
			boolean syncing = mService.getServiceHelper().isSyncing();
			setSupportProgressBarIndeterminateVisibility(syncing);
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			mBound = false;
			mService = null;
			mServiceHelper = null;
		}
	};
	protected ControlService mService;
	protected ServiceHelper mServiceHelper;
	private ProgressDialog mProgressDialog;
	private DetachableResultReceiver mReceiver;
	//private ListPreference mServerAddressListPreference;
	private Preference mServerAddressPreference;
	private boolean mSyncing = false;
	private String resultMessage = null;
	private int resultProgress;

	/** Invoke "home" action, returning to {@link HomeActivity}. */
	public void goHome() {

		Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(final com.actionbarsherlock.view.Menu menu) {

		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.actionbar_refresh, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				goHome();
				return true;
			case R.id.menu_refresh:
				triggerRefresh();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onReceiveResult(final int resultCode, final Bundle resultData) {

		switch (resultCode) {
			case ResultCodes.STATUS_RUNNING: {
				resultMessage = resultData.getString(Intent.EXTRA_TEXT);
				resultProgress = resultData.getInt("EXTRA_RESULT_PROGRESS");
				mSyncing = true;
				break;
			}
			case ResultCodes.STATUS_FINISHED: {
				if (mSyncing) {
					// close preference activity if successfully update
					finish();
				}
				mSyncing = false;
				break;
			}
			case ResultCodes.STATUS_ERROR: {
				// Error happened down in SyncService, show as toast.
				mSyncing = false;
				final String errorText = getString(R.string.toast_sync_error, resultData
						.getString(Intent.EXTRA_TEXT));
				showLongToast(errorText);
				break;
			}
			case ResultCodes.STATUS_STARTED: {
				// got max progress num
				mSyncing = true;
				maxProgress = resultData.getInt("EXTRA_MAX_PROGRESS");
				mProgressDialog.setMax(maxProgress);
				break;
			}
			default:
				break;
		}
		updateDialogView(mSyncing);
	}

	
	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {

		if (PreferenceKey.SERVER_ADDRESS.equals(key)) {
			Log.i("Disc pref onChange", String.valueOf(key));
			Log.i("Disc pref key",String.valueOf(key));
			
			//mServerAddressListPreference.setSummary(PreferenceHelper.getServerAddress(this));
			mServerAddressPreference.setSummary(PreferenceHelper.getServerAddress(this));
			
			triggerRefresh();
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.discusys_preference);
		//mServerAddressListPreference = (ListPreference) getPreferenceScreen().findPreference(
		//		PreferenceKey.SERVER_ADDRESS);
		mServerAddressPreference=getPreferenceScreen().findPreference(PreferenceKey.SERVER_ADDRESS);
		
		
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_action_home);
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setTitle(getString(R.string.progress_title_download_database));
		mProgressDialog.setMessage("Test");
		mProgressDialog.setMax(maxProgress);
		mProgressDialog.setCancelable(false);
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
	}

	@Override
	protected void onResume() {

		super.onResume();
		//mServerAddressListPreference.setSummary(PreferenceHelper.getServerAddress(this));
		mServerAddressPreference.setSummary(PreferenceHelper.getServerAddress(this));
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
	}

	@Override
	protected void onStart() {

		super.onStart();
		Intent intent = new Intent(this, ControlService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {

		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	private void publishMessage() {

		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
		mProgressDialog.setProgress(resultProgress);
		mProgressDialog.setMessage(resultMessage);
	}

	private void showLongToast(final String text) {

		if (this == null) {
			return;
		}
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
	}

	private void triggerRefresh() {

		mServiceHelper.downloadAll(mReceiver);
	}

	private void updateDialogView(final boolean syncing) {

		if (this == null) {
			// dialog should be already dismissed
			return;
		}
		if (mProgressDialog == null) {
			// nothing to update
			return;
		}
		if (syncing) {
			publishMessage();
		} else {
			mProgressDialog.dismiss();
			// reset dialog here
			// it will be newly created, but saved values are the same
			maxProgress = 100;
			resultMessage = "";
			resultProgress = 0;
		}
	}
}
