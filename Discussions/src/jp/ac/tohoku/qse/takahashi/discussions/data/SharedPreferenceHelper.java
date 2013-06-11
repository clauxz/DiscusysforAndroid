package jp.ac.tohoku.qse.takahashi.discussions.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceHelper {

	public static long getUpdatedTime(final Context context) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getLong(PreferenceKey.UPDATED_TIME, 0);
	}

	public static void setUpdatedTime(final Context context, final long timeInMiliseconds) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(PreferenceKey.UPDATED_TIME, timeInMiliseconds);
		editor.commit();
	}
}
