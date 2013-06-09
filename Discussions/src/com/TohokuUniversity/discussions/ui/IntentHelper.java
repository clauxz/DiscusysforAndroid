package com.TohokuUniversity.discussions.ui;

import com.TohokuUniversity.discussions.service.FileDownloader;

import android.content.Intent;
import android.net.Uri;

import java.io.File;

public class IntentHelper {

	public static Intent getViewPdfIntent(final String fileName) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		File file = FileDownloader.createFile(fileName);
		intent.setDataAndType(Uri.fromFile(file), "application/pdf");
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		return intent;
	}
}
