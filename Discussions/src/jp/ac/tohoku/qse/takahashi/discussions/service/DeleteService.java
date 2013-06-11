package jp.ac.tohoku.qse.takahashi.discussions.service;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Point;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.SelectedPoint;
import jp.ac.tohoku.qse.takahashi.discussions.data.odata.OdataWriteClient;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Attachments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.photon.PhotonHelper;
import jp.ac.tohoku.qse.takahashi.discussions.photon.constants.StatsEvent;
import jp.ac.tohoku.qse.takahashi.discussions.ui.IntentAction;
import jp.ac.tohoku.qse.takahashi.discussions.utils.ConnectivityUtil;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MyLog;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.ResultReceiver;
import android.util.Log;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class DeleteService extends IntentService {

	public static final int TYPE_DELETE_ATTACHMENT = 0x2;
	public static final int TYPE_DELETE_COMMENT = 0x1;
	public static final int TYPE_DELETE_POINT = 0x0;
	private static final boolean DEBUG = true && ApplicationConstants.LOGD_SERVICE;
	private static final String TAG = DeleteService.class.getSimpleName();

	public DeleteService() {

		super(TAG);
	}

	private static ResultReceiver getActivityReceiverFromExtra(final Intent intent) {

		return intent.getParcelableExtra(ServiceExtraKeys.ACTIVITY_RECEIVER);
	}

	private static ResultReceiver getPhotonReceiverFromExtra(final Intent intent) {

		return intent.getParcelableExtra(ServiceExtraKeys.PHOTON_RECEIVER);
	}

	private static SelectedPoint getSelectedPointFromExtra(final Intent intent) {

		if (!intent.hasExtra(ServiceExtraKeys.SELECTED_POINT)) {
			throw new IllegalArgumentException("[getSelectedPointFromExtra] called without required extra: "
					+ ServiceExtraKeys.SELECTED_POINT);
		}
		return intent.getParcelableExtra(ServiceExtraKeys.SELECTED_POINT);
	}

	private static int getTypeFromExtra(final Intent intent) {

		return intent.getIntExtra(ServiceExtraKeys.TYPE_ID, Integer.MIN_VALUE);
	}

	private static int getValueIdFromExtra(final Intent intent) {

		return intent.getIntExtra(ServiceExtraKeys.VALUE_ID, Integer.MIN_VALUE);
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	private static void validateIntent(final Intent intent) {

		if (!IntentAction.DELETE.equals(intent.getAction())) {
			throw new IllegalArgumentException("Service was started with unknown intent: "
					+ intent.getAction());
		}
		if (intent.getExtras() == null) {
			throw new IllegalArgumentException("Service was started without extras");
		}
		if (!intent.hasExtra(ServiceExtraKeys.PHOTON_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.PHOTON_RECEIVER);
		}
		if (!intent.hasExtra(ServiceExtraKeys.TYPE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.TYPE_ID);
		}
		if (!intent.hasExtra(ServiceExtraKeys.VALUE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.VALUE_ID);
		}
		if (!intent.hasExtra(ServiceExtraKeys.ACTIVITY_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.ACTIVITY_RECEIVER);
		}
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		logd("[onHandleIntent] intent: " + intent.toString());
		validateIntent(intent);
		final ResultReceiver activityReceiver = getActivityReceiverFromExtra(intent);
		if (isConnected()) {
			ActivityResultHelper.sendStatusStart(activityReceiver);
		} else {
			String errorString = getString(R.string.text_error_network_off);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
			stopSelf();
			return;
		}
		try {
			switch (getTypeFromExtra(intent)) {
				case TYPE_DELETE_POINT:
					deletePoint(intent);
					break;
				case TYPE_DELETE_COMMENT:
					deleteComment(intent);
					break;
				case TYPE_DELETE_ATTACHMENT:
					deleteAttachment(intent);
					break;
				default:
					throw new IllegalArgumentException("Illegal type id: " + getTypeFromExtra(intent));
			}
		} catch (Exception e) {
			MyLog.e(TAG, "[onHandleIntent] sync error. Intent action: " + intent.getAction(), e);
			ActivityResultHelper.sendStatusError(activityReceiver, e.getMessage());
			stopSelf();
			return;
		}
		ActivityResultHelper.sendStatusFinished(activityReceiver);
		logd("[onHandleIntent] delete service finished");
	}

	private void deleteAttachment(final Intent intent) {

		int attachmentId = getValueIdFromExtra(intent);
		logd("[deleteAttachment] id: " + attachmentId);
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.deleteAttachment(attachmentId);
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		PhotonHelper.sendArgPointUpdated(selectedPoint, photonReceiver);
		PhotonHelper.sendStatsEvent(StatsEvent.MEDIA_REMOVED, selectedPoint, photonReceiver);
		String where = Attachments.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(attachmentId) };
		getContentResolver().delete(Attachments.CONTENT_URI, where, args);
	}

	private void deleteComment(final Intent intent) {

		int commentId = getValueIdFromExtra(intent);
		logd("[deleteComment] comment id: " + commentId);
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.deleteComment(commentId);
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		PhotonHelper.sendArgPointUpdated(selectedPoint, photonReceiver);
		PhotonHelper.sendStatsEvent(StatsEvent.COMMENT_REMOVED, selectedPoint, photonReceiver);
		String where = Comments.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(commentId) };
		getContentResolver().delete(Comments.CONTENT_URI, where, args);
	}

	private void deletePoint(final Intent intent) {

		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		//
		int pointId = selectedPoint.getPointId();
		logd("[deletePoint] point id: " + pointId);
		//
		Point deletePoint = getPointFromLocal(pointId);
		deletePointOnServer(deletePoint);
		deletePointOnLocal(deletePoint);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		updatePointOrderNumbers(deletePoint, photonReceiver);
		PhotonHelper.sendArgPointDeleted(deletePoint, photonReceiver);
		PhotonHelper.sendStatsEvent(StatsEvent.BADGE_EDITED, selectedPoint, photonReceiver);
	}

	private int deletePointOnLocal(final Point point) {

		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(point.getId()) };
		return getContentResolver().delete(Points.CONTENT_URI, where, args);
	}

	private void deletePointOnServer(final Point point) {

		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.deletePoint(point.getId());
	}

	private Point getPointFromLocal(final int pointId) {

		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(pointId) };
		Cursor cursor = getContentResolver().query(Points.CONTENT_URI, null, where, args, null);
		Point point;
		if (cursor.moveToFirst()) {
			point = new Point(cursor);
		} else {
			// dump, empty point
			point = new Point();
		}
		cursor.close();
		return point;
	}

	private boolean isConnected() {

		boolean connected;
		if (ApplicationConstants.DEV_MODE) {
			connected = true;
		} else {
			connected = ConnectivityUtil.isNetworkConnected(this);
		}
		return connected;
	}

	private void updatePoint(final Point point, final ResultReceiver photonReceiver) {

		updatePointOnServer(point);
		updatePointOnLocal(point);
		PhotonHelper.sendArgPointUpdated(point, photonReceiver);
	}

	private int updatePointOnLocal(final Point point) {

		String where = Points.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(point.getId()) };
		return getContentResolver().update(Points.CONTENT_URI, point.toContentValues(), where, args);
	}

	private void updatePointOnServer(final Point point) {

		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.updatePoint(point);
	}

	private void updatePointOrderNumbers(final Point deletedPoint, final ResultReceiver photonReceiver) {

		int orderNum = deletedPoint.getOrderNumber();
		String whereUpdated = Points.Columns.ORDER_NUMBER + ">" + orderNum + " AND "
				+ Points.Columns.PERSON_ID + "= " + deletedPoint.getPersonId() + " AND "
				+ Points.Columns.TOPIC_ID + "=" + deletedPoint.getTopicId();
		Cursor pointGreaterDeletedCursor = getContentResolver().query(Points.CONTENT_URI, null, whereUpdated,
				null, Points.Columns.ORDER_NUMBER);
		// decrease order num foreach point after deleted by one
		for (pointGreaterDeletedCursor.moveToFirst(); !pointGreaterDeletedCursor.isAfterLast(); pointGreaterDeletedCursor
				.moveToNext()) {
			Point point = new Point(pointGreaterDeletedCursor);
			point.setOrderNumber(point.getOrderNumber() - 1);
			updatePoint(point, photonReceiver);
		}
		pointGreaterDeletedCursor.close();
	}
}
