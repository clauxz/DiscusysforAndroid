package com.slobodastudio.discussions.utils.fragmentasynctask;

import com.slobodastudio.discussions.R;
import com.slobodastudio.discussions.ui.OnDownloadCompleteListener;
import com.slobodastudio.discussions.utils.MyLog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.widget.Toast;

/** A non-UI fragment, retained across configuration changes, that updates its activity's UI when sync status
 * changes. */
public class SyncStatusUpdaterFragment extends Fragment implements DetachableResultReceiver.Receiver {

	public static final String TAG = SyncStatusUpdaterFragment.class.getName();
	/** 100 is default value for ProggressDialog */
	int maxProgress = 100;
	private ProgressDialog mProgressDialog;
	private DetachableResultReceiver mReceiver;
	private boolean mSyncing = false;
	private String resultMessage = null;
	private int resultProgress;
	private OnDownloadCompleteListener downloadCompleteListener;

	public ResultReceiver getReceiver() {

		return mReceiver;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		// Setup progress dialog
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setTitle(getString(R.string.progress_title_download_database));
		mProgressDialog.setMessage("Test");
		mProgressDialog.setMax(maxProgress);
		mProgressDialog.setCancelable(false);
		// mProgressDialog.setIndeterminate(true);
		// mProgressDialog.setCancelable(true);
		// TODO mProgressDialog.setOnCancelListener(this);
		if (mSyncing) {
			publishMessage();
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
	}

	@Override
	public void onDetach() {

		super.onDetach();
		// release progress dialog to avoid memory leak, because of it holds activity context
		mProgressDialog.dismiss();
		mProgressDialog = null;
	}

	/** {@inheritDoc} */
	@Override
	public void onReceiveResult(final int resultCode, final Bundle resultData) {

		switch (resultCode) {
			case ResultCodes.STATUS_RUNNING: {
				resultMessage = resultData.getString(Intent.EXTRA_TEXT);
				resultProgress = resultData.getInt("EXTRA_RESULT_PROGRESS");
				mSyncing = true;
				break;
			}
			case ResultCodes.STATUS_FINISHED: {
				mSyncing = false;
				notifyDownloadComplete();
				break;
			}
			case ResultCodes.STATUS_ERROR: {
				// Error happened down in SyncService, show as toast.
				mSyncing = false;
				if (getActivity() != null) {
					final String errorText = getString(R.string.toast_sync_error, resultData
							.getString(Intent.EXTRA_TEXT));
					showLongToast(errorText);
				}
				break;
			}
			case ResultCodes.STATUS_STARTED: {
				// got max progress num
				mSyncing = true;
				maxProgress = resultData.getInt("EXTRA_MAX_PROGRESS");
				mProgressDialog.setMax(maxProgress);
				break;
			}
			default:
				break;
		}
		updateDialogView(mSyncing);
	}

	private void publishMessage() {

		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
		mProgressDialog.setProgress(resultProgress);
		mProgressDialog.setMessage(resultMessage);
	}

	private void showLongToast(final String text) {

		if (getActivity() == null) {
			MyLog.d(TAG, "Drop toast text on the floor, no activity attached: " + text);
			return;
		}
		Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
	}

	private void updateDialogView(final boolean syncing) {

		if (getActivity() == null) {
			// dialog should be already dismissed
			return;
		}
		if (mProgressDialog == null) {
			// nothing to update
			return;
		}
		if (syncing) {
			publishMessage();
		} else {
			mProgressDialog.dismiss();
			// reset dialog here
			// it will be newly created, but saved values are the same
			maxProgress = 100;
			resultMessage = "";
			resultProgress = 0;
		}
	}

	public void setDownloadCompleteListener(final OnDownloadCompleteListener downloadCompleteListener) {

		this.downloadCompleteListener = downloadCompleteListener;
	}

	private void notifyDownloadComplete() {

		if (getActivity() == null) {
			return;
		}
		if (downloadCompleteListener == null) {
			return;
		}
		downloadCompleteListener.onDownloadComplete();
	}
}
