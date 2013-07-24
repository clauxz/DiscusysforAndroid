package jp.ac.tohoku.qse.takahashi.discussions.ui.activities;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.service.ControlService;
import jp.ac.tohoku.qse.takahashi.discussions.service.OdataSyncResultListener;
import jp.ac.tohoku.qse.takahashi.discussions.service.ServiceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.service.ControlService.LocalBinder;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public abstract class BaseActivity extends SherlockFragmentActivity {

	protected static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = BaseActivity.class.getSimpleName();
	protected boolean mBound = false;
	protected final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(final ComponentName className, final IBinder service) {

			if (DEBUG) {
				Log.d(TAG, "[onServiceConnected] className: " + className);
			}
			// We've bound to PhotonService, cast the IBinder and get PhotonService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mServiceHelper = mService.getServiceHelper();
			mBound = true;
			boolean syncing = mService.getServiceHelper().isSyncing();
			Log.d(TAG, "onServiceConnected] syncing: " + syncing);
			setSupportProgressBarIndeterminateVisibility(syncing);
			mServiceHelper.setOdataListener(mListener);
			onControlServiceConnected();
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			Log.e(TAG, "[onServiceDisconnected] className: " + className);
			mBound = false;
			mService = null;
			mServiceHelper = null;
		}
	};
	protected ControlService mService;
	protected ServiceHelper mServiceHelper;
	private final OdataSyncResultListener mListener = new OdataSyncResultListener() {

		@Override
		public void handleError(final String message) {

			showProgressDialog(false);
			final String errorText = getString(R.string.toast_sync_error, message);
			Toast.makeText(BaseActivity.this, errorText, Toast.LENGTH_LONG).show();
		}

		@Override
		public void updateSyncStatus(final boolean syncing) {

			setSupportProgressBarIndeterminateVisibility(syncing);
			showProgressDialog(syncing);
		}
	};

	public ServiceHelper getServiceHelper() {

		return mServiceHelper;
	}

	/** Invoke "home" action, returning to {@link HomeActivity}. */
	public void goHome() {

		if (this instanceof PersonsActivity) {
			return;
		}
		
		if(this instanceof WebReportViewActivity){
			onBackPressed();
			return;
		}
			
		Intent intent = new Intent(Intent.ACTION_VIEW, Persons.CONTENT_URI);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/** Invoke "search" action, triggering a default search. */
	public void goSearch() {

		startSearch(null, false, Bundle.EMPTY, false);
	}

	@Override
	public boolean onKeyLongPress(final int keyCode, final KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goHome();
			return true;
		}
		return super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				goHome();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	protected abstract void onControlServiceConnected();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		setTheme(R.style.Theme_Sherlock_Light);
		super.onCreate(savedInstanceState);
		// This has to be called before setContentView and you must use the
		// class in com.actionbarsherlock.view and NOT android.view
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		// requestWindowFeature(Window.FEATURE_PROGRESS);
		setSupportProgressBarIndeterminateVisibility(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.ic_action_home);
	}

	@Override
	protected void onPause() {

		if (DEBUG) {
			Log.d(TAG, "[onPause]");
		}
		if (mBound) {
			mServiceHelper.setOdataListener(null);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {

		super.onResume();
		if (DEBUG) {
			Log.d(TAG, "[onResume]");
		}
		if (mBound) {
			mServiceHelper.setOdataListener(mListener);
		}
	}

	@Override
	protected void onStart() {

		if(this instanceof CommentDetailsActivity){
			Log.i("Disc START","onStart "+CommentDetailsActivity.class.getSimpleName());
		}
		else
		{
			Log.i("Disc START",this.getClass().getSimpleName());
		}
		
		super.onStart();
		if (DEBUG) {
			Log.d(TAG, "[onStart]");
		}
		Intent intent = new Intent(this, ControlService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {

		if (DEBUG) {
			Log.d(TAG, "[onStop]");
		}
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
		super.onStop();
	}

	public boolean isBound() {

		return mBound;
	}

	protected void showProgressDialog(final boolean shown) {

		// TODO Auto-generated method stub
	}
}
