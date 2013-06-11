package jp.ac.tohoku.qse.takahashi.discussions.ui;


import android.content.Intent;
import android.net.Uri;

import java.io.File;

import jp.ac.tohoku.qse.takahashi.discussions.service.FileDownloader;

public class IntentHelper {

	public static Intent getViewPdfIntent(final String fileName) {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		File file = FileDownloader.createFile(fileName);
		intent.setDataAndType(Uri.fromFile(file), "application/pdf");
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		return intent;
	}
}
