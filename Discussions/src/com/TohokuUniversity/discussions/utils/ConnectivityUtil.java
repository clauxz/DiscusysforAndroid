package com.TohokuUniversity.discussions.utils;

import com.TohokuUniversity.discussions.ApplicationConstants;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectivityUtil {

	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final String TAG = ConnectivityUtil.class.getSimpleName();

	/** A private Constructor prevents class from instantiating. */
	private ConnectivityUtil() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static boolean isNetworkConnected(final Context context) {

		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (DEBUG) {
			Log.d(TAG, "Connected wifi: " + wifi.isConnected() + ", mobile: " + mobile.isConnected());
		}
		return wifi.isConnected() || mobile.isConnected();
	}
}
