package jp.ac.tohoku.qse.takahashi.discussions.utils.fragmentasynctask;

import jp.ac.tohoku.qse.takahashi.discussions.data.PreferenceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.data.PreferenceKey;
import jp.ac.tohoku.qse.takahashi.discussions.ui.OnDownloadCompleteListener;
import jp.ac.tohoku.qse.takahashi.discussions.ui.activities.PersonsActivity;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MyLog;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.ListPreference;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/** A non-UI fragment, retained across configuration changes, that updates its activity's UI when sync status
 * changes. */
public class SyncStatusUpdaterFragment extends  Fragment implements DetachableResultReceiver.Receiver {

	public static final String TAG = SyncStatusUpdaterFragment.class.getName();
	/** 100 is default value for ProggressDialog */
	int maxProgress = 100;
	private ProgressDialog mProgressDialog;
	private DetachableResultReceiver mReceiver;
	private boolean mSyncing = false;
	private String resultMessage = null;
	private int resultProgress;
	private OnDownloadCompleteListener downloadCompleteListener;

	private Dialog mServerAddressDialog;
	private ListPreference mServerAddressListPreference;
	
	
	public ResultReceiver getReceiver() {

		return mReceiver;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		createServerDialog();
		//createProgressDialog();
	}

	//*
	private void createServerDialog()
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_title_settings_server);
		
		builder.setNegativeButton(R.string.button_title_cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				createProgressDialog();
				if((getActivity() instanceof PersonsActivity) &&
						(getActivity()!=null))
				{
					((PersonsActivity)getActivity()).triggerRefresh();
				}
				
			}
			
		});
		
		int initSelection=0;
		initSelection=PreferenceHelper.getPhotonDbAddressPointer(getActivity());
		if( getResources().getStringArray(R.array.server_names).length<=initSelection)
			initSelection=0;
		
		builder.setSingleChoiceItems(R.array.server_names,initSelection,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				switch(which)
				{
				case PreferenceHelper.SERVER_LOCAL:
					Log.v("Discussions","[set server] LOCAL");
					PreferenceHelper.setServerAddress(getActivity(), getString(R.string.local_server_address));
					break;
				case PreferenceHelper.SERVER_PUBLIC:
					Log.v("Discussions","[set server] PUBLIC");
					PreferenceHelper.setServerAddress(getActivity(), getString(R.string.public_server_address));
					break;
				case PreferenceHelper.SERVER_OFFLINE:
					Log.v("Discussions","[set server] OFFLINE");
					PreferenceHelper.setServerAddress(getActivity(), getString(R.string.offline_server_address));
					break;
				case PreferenceHelper.SERVER_DEVELOPMENT:
					Log.v("Discussions","[set server] DEVELOPMENT");
					PreferenceHelper.setServerAddress(getActivity(), getString(R.string.development_server_address));
					break;
				}
				
				if(mServerAddressDialog!=null)
					mServerAddressDialog.cancel();
				
				createProgressDialog();
				if((getActivity() instanceof PersonsActivity) &&
						(getActivity()!=null))
				{
					((PersonsActivity)getActivity()).triggerRefresh();
				}
				
			}
		});
		
		mServerAddressDialog=builder.create();
		if(mServerAddressDialog!=null)
			mServerAddressDialog.show();
	}

	
	
	private void createProgressDialog(){
		// Setup progress dialog
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setTitle(getString(R.string.progress_title_download_database));
		mProgressDialog.setMessage("Test");
		mProgressDialog.setIndeterminate(true);
		// mProgressDialog.setMax(maxProgress);
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
				mProgressDialog.setIndeterminate(false);
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
