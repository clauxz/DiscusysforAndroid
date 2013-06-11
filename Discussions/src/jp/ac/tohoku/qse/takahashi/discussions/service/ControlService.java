package jp.ac.tohoku.qse.takahashi.discussions.service;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.photon.PhotonController;


import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ControlService extends Service {

	private static final boolean DEBUG = true && ApplicationConstants.LOGD_SERVICE;
	private static final String TAG = ControlService.class.getSimpleName();
	/** indicates whether onRebind should be used */
	private final boolean mAllowRebind = true;
	/** Binder given to clients */
	private final IBinder mBinder;
	private final ResolverPhotonServiceCallback mPhotonCallback;
	private final PhotonController mPhotonController;
	private final ServiceHelper mServiceHelper;

	public ControlService() {

		mBinder = new LocalBinder();
		mPhotonController = new PhotonController();
		mServiceHelper = new ServiceHelper(this, mPhotonController);
		mPhotonCallback = new ResolverPhotonServiceCallback(mServiceHelper);
		mPhotonController.getCallbackHandler().addCallbackListener(mPhotonCallback);
	}

	public PhotonController getPhotonController() {

		return mPhotonController;
	}

	public ServiceHelper getServiceHelper() {

		return mServiceHelper;
	}

	@Override
	public IBinder onBind(final Intent intent) {

		if (DEBUG) {
			Log.d(TAG, "[onBind] intent: " + intent);
		}
		return mBinder;
	}

	@Override
	public void onCreate() {

		if (DEBUG) {
			Log.d(TAG, "[onCreate]");
		}
		super.onCreate();
	}

	@Override
	public void onDestroy() {

		if (DEBUG) {
			Log.d(TAG, "[onDestroy]");
		}
		mPhotonController.disconnect();
		// TODO: stop all downloads
		super.onDestroy();
	}

	@Override
	public void onRebind(final Intent intent) {

		// A client is binding to the service with bindService(),
		// after onUnbind() has already been called
		if (DEBUG) {
			Log.d(TAG, "[onRebind] intent: " + intent);
		}
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {

		if (DEBUG) {
			Log.d(TAG, "[onStartCommand] intent: " + intent + ", flags: " + flags + ", startId: " + startId);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(final Intent intent) {

		// All clients have unbound with unbindService()
		if (DEBUG) {
			Log.d(TAG, "[onUnbind] intent: " + intent);
		}
		return mAllowRebind;
	}

	/** Class for clients to access. Because we know this service always runs in the same process as its
	 * clients, we don't need to deal with IPC. */
	public class LocalBinder extends Binder {

		/** @return instance of PhotonService */
		public ControlService getService() {

			return ControlService.this;
		}
	}
}
