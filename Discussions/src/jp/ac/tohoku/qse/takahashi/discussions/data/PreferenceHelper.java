package jp.ac.tohoku.qse.takahashi.discussions.data;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class PreferenceHelper {

	public static String getOdataUrl(final Context context) {

		return "http://" + getServerAddress(context) + "/DiscSvc/discsvc.svc/";
	}

	public final static int SERVER_LOCAL=0;
	public final static int SERVER_PUBLIC=1;
	public final static int SERVER_OFFLINE=2;
	public final static int SERVER_DEVELOPMENT=3;
	
	
	public static String getPhotonDbAddress(final Context context) {

		String serverAddress = getServerAddress(context);
		String localServer = context.getString(R.string.local_server_address);
		if (TextUtils.equals(serverAddress, localServer)) {
			return context.getString(R.string.local_database_address);
		}
		String developmentServer = context.getString(R.string.development_server_address);
		if (TextUtils.equals(serverAddress, developmentServer)) {
			return context.getString(R.string.development_database_address);
		}
		String publicServer = context.getString(R.string.public_server_address);
		if (TextUtils.equals(serverAddress, publicServer)) {
			return context.getString(R.string.public_database_address);
		}
		String offlineServer = context.getString(R.string.offline_server_address);
		if (TextUtils.equals(serverAddress, offlineServer)) {
			return context.getString(R.string.offline_database_address);
		}
		throw new IllegalStateException("Could not find database connection string for this server: "
				+ serverAddress);
	}
	
	
	public static int getPhotonDbAddressPointer(final Context context) {

		String serverAddress = getServerAddress(context);
		String localServer = context.getString(R.string.local_server_address);
		if (TextUtils.equals(serverAddress, localServer)) {
			return PreferenceHelper.SERVER_LOCAL;
		}
		String developmentServer = context.getString(R.string.development_server_address);
		if (TextUtils.equals(serverAddress, developmentServer)) {
			return PreferenceHelper.SERVER_DEVELOPMENT;
		}
		String publicServer = context.getString(R.string.public_server_address);
		if (TextUtils.equals(serverAddress, publicServer)) {
			return PreferenceHelper.SERVER_PUBLIC;
		}
		String offlineServer = context.getString(R.string.offline_server_address);
		if (TextUtils.equals(serverAddress, offlineServer)) {
			return PreferenceHelper.SERVER_OFFLINE;
		}
		
		return 0;
	}
	

	public static String getPhotonUrl(final Context context) {

		return getServerAddress(context) + ":5055";
	}

	public static String getServerAddress(final Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String defaultServer = context.getString(R.string.local_server_address);
		String serverAddress = prefs.getString(PreferenceKey.SERVER_ADDRESS, defaultServer);
		return serverAddress;
	}
	
	
	public static void setServerAddress(final Context context ,final String address)
	{
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor =prefs.edit();
		String defaultServer = context.getString(R.string.local_server_address);
		
		if(address!=null && address.length()!=0)
			defaultServer=address;
		
		editor.putString(PreferenceKey.SERVER_ADDRESS, defaultServer);
		editor.commit();
	}
	
	public static void setTypedServerAddressFlag(final Context contex, final boolean flag){
		
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(contex);
		SharedPreferences.Editor editor =prefs.edit();
		
		editor.putBoolean(PreferenceKey.IS_SERVER_ADDRESS_TYPED, flag);
		editor.commit();
	}
	
	public static boolean isTypedServerAddressFlag(final Context contex){
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(contex);
		
		return prefs.getBoolean(PreferenceKey.IS_SERVER_ADDRESS_TYPED,false);
	}
}
