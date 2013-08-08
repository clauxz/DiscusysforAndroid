package jp.ac.tohoku.qse.takahashi.discussions.service;

import java.util.ArrayList;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Attachment;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.SelectedPoint;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Source;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.photon.PhotonController;
import jp.ac.tohoku.qse.takahashi.discussions.ui.IntentAction;
import jp.ac.tohoku.qse.takahashi.discussions.utils.fragmentasynctask.ResultCodes;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

/** Should attach mOdataListener when activity is active. */
public class ServiceHelper {

	private static final boolean DEBUG = true && ApplicationConstants.LOGD_SERVICE;
	private static final String TAG = ServiceHelper.class.getSimpleName();
	private final ResultReceiver mActivityReceiver;
	private final Context mContext;
	private OdataSyncResultListener mOdataListener;
	private boolean mOdataSyncing;
	private final PhotonController mPhotonController;

	public ServiceHelper(final Context context, final PhotonController photonController) {

		super();
		mContext = context;
		mPhotonController = photonController;
		// default values
		mOdataSyncing = false;
		mActivityReceiver = new ActivityResultReceiver(new Handler());
	}

	public void deleteAttachment(final int attachmentId, final SelectedPoint selectedPoint) {

		Intent intent = new Intent(IntentAction.DELETE);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DeleteService.TYPE_DELETE_ATTACHMENT);
		intent.putExtra(ServiceExtraKeys.VALUE_ID, attachmentId);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}
	
	
	public void deleteAttachedURL(final int urlId,final SelectedPoint selectedPoint){
		Intent intent=new Intent(IntentAction.DELETE);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DeleteService.TYPE_DELETE_URL);
		intent.putExtra(ServiceExtraKeys.VALUE_ID, urlId);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void deleteComment(final int commentId, final SelectedPoint selectedPoint) {

		Intent intent = new Intent(IntentAction.DELETE);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DeleteService.TYPE_DELETE_COMMENT);
		intent.putExtra(ServiceExtraKeys.VALUE_ID, commentId);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public int deleteLocalPoint(final int pointId) {

		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(pointId) };
		return mContext.getContentResolver().delete(Points.CONTENT_URI, where, args);
	}

	public void deletePoint(final SelectedPoint selectedPoint) {

		Intent intent = new Intent(IntentAction.DELETE);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DeleteService.TYPE_DELETE_POINT);
		intent.putExtra(ServiceExtraKeys.VALUE_ID, selectedPoint.getPointId());
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void downloadAll(final ResultReceiver dialogReceiver) {

		final Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DownloadService.TYPE_ALL);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, dialogReceiver);
		mContext.startService(intent);
	}

	public void downloadAllPerSession(final ResultReceiver dialogReceiver, final int sessionId) {

		final Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DownloadService.TYPE_ALL_PER_SESSION);
		intent.putExtra(ServiceExtraKeys.VALUE_ID, sessionId);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, dialogReceiver);
		mContext.startService(intent);
	}

	public void downloadSessions() {

		final Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DownloadService.TYPE_SESSIONS);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		mContext.startService(intent);
	}

	public void downloadPointsFromTopic(final int topicId) {

		Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DownloadService.TYPE_POINT_FROM_TOPIC);
		intent.putExtra(ServiceExtraKeys.VALUE_ID, topicId);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		mContext.startService(intent);
	}

	public void insertAttachment(final Attachment attachment, final SelectedPoint selectedPoint) {

		insertAttachment(attachment, selectedPoint, null);
	}

	public void insertAttachment(final Attachment attachment, final SelectedPoint selectedPoint, final Uri uri) {

		insertAttachment(attachment, selectedPoint, uri, mActivityReceiver);
	}

	public void insertAttachment(final Attachment attachment, final SelectedPoint selectedPoint,
			final Uri uri, final ResultReceiver receiver) {

		logd("[insertAttachment]" + attachment.getTitle());
		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, UploadService.TYPE_INSERT_ATTACHMENT);
		intent.putExtra(ServiceExtraKeys.VALUE, attachment);
		intent.putExtra(ServiceExtraKeys.URI, uri);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, receiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void insertComment(final Bundle commentValues, final SelectedPoint selectedPoint) {

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, UploadService.TYPE_INSERT_COMMENT);
		intent.putExtra(ServiceExtraKeys.VALUE, commentValues);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}
	
	public void insertCommentPersonReadedEntry(final Bundle commentValues,final SelectedPoint selectedPoint){
		Intent intent=new Intent(IntentAction.UPLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, UploadService.TYPE_INSERT_COMMENTPERSONREADEDENTRY);
		intent.putExtra(ServiceExtraKeys.VALUE, commentValues);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}
	
	//*
	public void insertCommentPersonReadedEntities(final Bundle commentValues,final SelectedPoint selectedPoint,final int[] commentsIDs){
		
		Intent intent=new Intent(IntentAction.UPLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, UploadService.TYPE_INSERT_COMMENTPERSONREADEDENTRY_ARRAY);
		intent.putExtra(ServiceExtraKeys.VALUE, commentValues);
		intent.putExtra(ServiceExtraKeys.VALUE_INTEGER_ARRAY, commentsIDs);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}
	//*/
	/**
	 * Function used only for fixing WPF client bug. In future such function need to delete when 
	 * WPF client will be fixed (wpf client will change algorithm for comment adding and placeholder functionality).
	 * 
	 */
	public void WPFCork_insertPlaceholder(final Bundle commentValues, final SelectedPoint selectedPoint) {

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, UploadService.TYPE_INSERT_WPFCORK_COMMENTPLACEHOLDER);
		intent.putExtra(ServiceExtraKeys.VALUE, commentValues);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void insertPointAndDescription(final Bundle values, final SelectedPoint selectedPoint) {

		// TODO: separate point and description objects from one bundle
		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, UploadService.TYPE_INSERT_POINT_AND_DESCRIPTION);
		intent.putExtra(ServiceExtraKeys.VALUE, values);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void insertSource(final Source source, final SelectedPoint selectedPoint) {

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, UploadService.TYPE_INSERT_SOURCE);
		intent.putExtra(ServiceExtraKeys.VALUE, source);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public boolean isSyncing() {

		return mOdataSyncing;
	}

	public void setOdataListener(final OdataSyncResultListener odataListener) {

		mOdataListener = odataListener;
	}

	public void updateDescription(final Bundle descriptionValues) {

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, UploadService.TYPE_UPDATE_DESCRIPTION);
		intent.putExtra(ServiceExtraKeys.VALUE, descriptionValues);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void updatePoint(final Bundle pointValue, final SelectedPoint selectedPoint) {

		Intent intent = new Intent(IntentAction.UPLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, UploadService.TYPE_UPDATE_POINT);
		intent.putExtra(ServiceExtraKeys.VALUE, pointValue);
		intent.putExtra(ServiceExtraKeys.SELECTED_POINT, selectedPoint);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		intent.putExtra(ServiceExtraKeys.PHOTON_RECEIVER, mPhotonController.getResultReceiver());
		mContext.startService(intent);
	}

	public void updatePoint(final int pointId) {

		Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DownloadService.TYPE_UPDATE_POINT);
		intent.putExtra(ServiceExtraKeys.VALUE_ID, pointId);
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		mContext.startService(intent);
	}

	public void downloadPdf(final String pdfUrl) {

		Intent intent = new Intent(IntentAction.DOWNLOAD);
		intent.putExtra(ServiceExtraKeys.TYPE_ID, DownloadService.TYPE_PDF_FILE);
		intent.setData(Uri.parse(pdfUrl));
		intent.putExtra(ServiceExtraKeys.ACTIVITY_RECEIVER, mActivityReceiver);
		mContext.startService(intent);
	}

	public class ActivityResultReceiver extends ResultReceiver {

		public ActivityResultReceiver(final Handler handler) {

			super(handler);
		}

		@Override
		public void onReceiveResult(final int resultCode, final Bundle resultData) {

			if (DEBUG) {
				Log.d(TAG, "[onReceiveResult] resultCode: " + resultCode + ", resultData: " + resultData);
			}
			switch (resultCode) {
				case ResultCodes.STATUS_RUNNING:
					mOdataSyncing = true;
					break;
				case ResultCodes.STATUS_FINISHED:
					mOdataSyncing = false;
					break;
				case ResultCodes.STATUS_ERROR:
					// Error happened down in SyncService, show as toast.
					mOdataSyncing = false;
					if (mOdataListener != null) {
						mOdataListener.handleError(resultData.getString(Intent.EXTRA_TEXT));
					}
					break;
				default:
					throw new IllegalArgumentException("Unknown result code: " + resultCode);
			}
			if (mOdataListener != null) {
				mOdataListener.updateSyncStatus(mOdataSyncing);
			}
		}
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}
}
