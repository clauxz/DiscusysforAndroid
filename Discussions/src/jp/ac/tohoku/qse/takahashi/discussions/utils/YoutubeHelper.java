package jp.ac.tohoku.qse.takahashi.discussions.utils;

import jp.ac.tohoku.qse.takahashi.discussions.data.odata.HttpUtil;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class YoutubeHelper {

	private static final String TAG = YoutubeHelper.class.getSimpleName();

	public static String getThumbImageUrl(final String youtubeUrl) {

		if (youtubeUrl == null) {
			return null;
		}
		try {
			return String.format("http://img.youtube.com/vi/%s/0.jpg", Uri.parse(youtubeUrl)
					.getQueryParameter("v"));
		} catch (UnsupportedOperationException e) {
			Log.e(TAG, "Failed to format image thumb url from youtubeUrl: " + youtubeUrl, e);
			return null;
		}
	}

	/** Makes internet connection, move out of UiThread */
	public static String getVideoTitle(final String youtubeUrl) {

		if (youtubeUrl == null) {
			return null;
		}
		String embededURL = "http://www.youtube.com/oembed?url=" + youtubeUrl + "&format=json";
		try {
			return new JSONObject(HttpUtil.getString(embededURL)).getString("title");
		} catch (JSONException e) {
			Log.e(TAG, "Failed to read title from url: " + embededURL, e);
			return null;
		}
	}
}
