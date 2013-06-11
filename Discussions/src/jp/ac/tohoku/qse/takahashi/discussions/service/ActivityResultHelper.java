package jp.ac.tohoku.qse.takahashi.discussions.service;

import jp.ac.tohoku.qse.takahashi.discussions.utils.fragmentasynctask.ResultCodes;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

public class ActivityResultHelper {

	/** A private Constructor prevents class from instantiating. */
	private ActivityResultHelper() throws UnsupportedOperationException {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static void sendProgress(final ResultReceiver activityReceiver, final String message,
			final int progress) {

		if (activityReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putString(Intent.EXTRA_TEXT, message);
			bundle.putInt("EXTRA_RESULT_PROGRESS", progress);
			activityReceiver.send(ResultCodes.STATUS_RUNNING, bundle);
		}
	}

	public static void sendStatusError(final ResultReceiver activityReceiver, final String errorMessage) {

		if (activityReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putString(Intent.EXTRA_TEXT, errorMessage);
			activityReceiver.send(ResultCodes.STATUS_ERROR, bundle);
		}
	}

	public static void sendStatusFinished(final ResultReceiver activityReceiver) {

		if (activityReceiver != null) {
			activityReceiver.send(ResultCodes.STATUS_FINISHED, Bundle.EMPTY);
		}
	}

	public static void sendStatusStart(final ResultReceiver activityReceiver) {

		if (activityReceiver != null) {
			activityReceiver.send(ResultCodes.STATUS_RUNNING, Bundle.EMPTY);
		}
	}

	public static void sendStatusStartWithCount(final ResultReceiver activityReceiver, final int maxCount) {

		if (activityReceiver != null) {
			final Bundle bundle = new Bundle();
			bundle.putInt("EXTRA_MAX_PROGRESS", maxCount);
			activityReceiver.send(ResultCodes.STATUS_STARTED, bundle);
		}
	}
}
