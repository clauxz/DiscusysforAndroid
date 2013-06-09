package com.TohokuUniversity.discussions.ui;

import com.TohokuUniversity.discussions.ui.activities.WebViewActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.net.URLEncoder;

public class ActivityHelper {

	public static void startSearchWebActivityForResult(final Activity activity, final String pointTitle,
			final int requestCode) {

		String url = buildGoogleLink("http://google.com/search", pointTitle, null);
		startWebActivityForResult(activity, requestCode, url);
	}

	public static void startSearchPictureActivityForResult(final Activity activity, final String pointTitle,
			final int requestCode) {

		String url = buildGoogleLink("http://images.google.com/images", pointTitle, null);
		startWebActivityForResult(activity, requestCode, url);
	}

	public static void startSearchPdfActivityForResult(final Activity activity, final String pointTitle,
			final int requestCode) {

		String url = buildGoogleLink("http://google.com/search", pointTitle, "pdf");
		startWebActivityForResult(activity, requestCode, url);
	}

	public static void startScholarPdfActivityForResult(final Activity activity, final String pointTitle,
			final int requestCode) {			

		String url = buildGoogleLink("http://scholar.google.com/scholar", pointTitle, "pdf");
		startWebActivityForResult(activity, requestCode, url);
	}

	public static void startWebActivityForResult(final Activity activity, final int requestCode,
			final String url) {

		Intent intent = new Intent(activity, WebViewActivity.class);
		Uri uri = Uri.parse(url);
		intent.setData(uri);
		activity.startActivityForResult(intent, requestCode);
	}

	private static String buildGoogleLink(final String baseLink, final String searchParams,
			final String fileType) {

		StringBuilder sb = new StringBuilder();
		sb.append(baseLink);
		sb.append("?q=");
		sb.append(URLEncoder.encode(searchParams));
		if (!TextUtils.isEmpty(fileType)) {
			sb.append("&as_filetype=");
			sb.append(fileType);
		}
		return sb.toString();
	}
}
