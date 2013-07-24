package jp.ac.tohoku.qse.takahashi.discussions.service;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.odata4j.core.OEntity;

import java.io.File;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Attachment;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Comment;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.CommentPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Description;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Point;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.SelectedPoint;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Source;
import jp.ac.tohoku.qse.takahashi.discussions.data.odata.HttpUtil;
import jp.ac.tohoku.qse.takahashi.discussions.data.odata.OdataWriteClient;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Attachments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Descriptions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sources;
import jp.ac.tohoku.qse.takahashi.discussions.photon.PhotonHelper;
import jp.ac.tohoku.qse.takahashi.discussions.photon.constants.StatsEvent;
import jp.ac.tohoku.qse.takahashi.discussions.ui.IntentAction;
import jp.ac.tohoku.qse.takahashi.discussions.utils.ConnectivityUtil;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MediaStoreHelper;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MyLog;
import jp.ac.tohoku.qse.takahashi.discussions.utils.YoutubeHelper;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class UploadService extends IntentService {

	/**
	 * Such constant can be removed when WPF client comment placeholder will be fixed.
	 * All logic which works with WPFCORK prefix also can be removed from code files.
	 */
	public static final int TYPE_INSERT_WPFCORK_COMMENTPLACEHOLDER=0x8;
	public static final int TYPE_INSERT_ATTACHMENT = 0x6;
	public static final int TYPE_INSERT_COMMENT = 0x5;
	public static final int TYPE_INSERT_COMMENTPERSONREADEDENTRY=0x9;
	public static final int TYPE_INSERT_DESCRIPTION = 0x3;
	public static final int TYPE_INSERT_POINT_AND_DESCRIPTION = 0x4;
	public static final int TYPE_INSERT_SOURCE = 0x7;
	public static final int TYPE_UPDATE_DESCRIPTION = 0x2;
	public static final int TYPE_UPDATE_POINT = 0x1;
	private static final boolean DEBUG = true && ApplicationConstants.LOGD_SERVICE;
	private static final String TAG = UploadService.class.getSimpleName();
	private ResultReceiver mActivityReceiver;

	public UploadService() {

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

	private static Uri getUriFromExtra(final Intent intent) {

		if (!intent.hasExtra(ServiceExtraKeys.URI)) {
			throw new IllegalArgumentException("[getUriFromExtra] called without required extra: "
					+ ServiceExtraKeys.URI);
		}
		return intent.getParcelableExtra(ServiceExtraKeys.URI);
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	private static void validateIntent(final Intent intent) {

		if (!IntentAction.UPLOAD.equals(intent.getAction())) {
			throw new IllegalArgumentException("Service was started with unknown intent: "
					+ intent.getAction());
		}
		if (intent.getExtras() == null) {
			throw new IllegalArgumentException("Service was started without extras");
		}
		if (!intent.hasExtra(ServiceExtraKeys.ACTIVITY_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.ACTIVITY_RECEIVER);
		}
		if (!intent.hasExtra(ServiceExtraKeys.PHOTON_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.PHOTON_RECEIVER);
		}
		if (!intent.hasExtra(ServiceExtraKeys.TYPE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.TYPE_ID);
		}
		if (!intent.hasExtra(ServiceExtraKeys.VALUE)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.VALUE);
		}
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		logd("[onHandleIntent] intent: " + intent.toString());
		validateIntent(intent);
		mActivityReceiver = getActivityReceiverFromExtra(intent);
		if (isConnected()) {
			ActivityResultHelper.sendStatusStart(mActivityReceiver);
		} else {
			String errorString = getString(R.string.text_error_network_off);
			ActivityResultHelper.sendStatusError(mActivityReceiver, errorString);
			stopSelf();
			return;
		}
		try {
			switch (getTypeFromExtra(intent)) {
				case TYPE_INSERT_POINT_AND_DESCRIPTION:
					insertPointAndDescription(intent);
					break;
				case TYPE_INSERT_DESCRIPTION:
					insertDescription(intent);
					break;
				case TYPE_UPDATE_POINT:
					updatePoint(intent);
					break;
				case TYPE_UPDATE_DESCRIPTION:
					updateDescription(intent);
					break;
				case TYPE_INSERT_COMMENT:
					insertComment(intent);
					break;
				case TYPE_INSERT_COMMENTPERSONREADEDENTRY:
					insertCommentPersonReadedEntry(intent);
					break;
				case TYPE_INSERT_WPFCORK_COMMENTPLACEHOLDER:
					WPFCork_insertCommentPlaceholder(intent);
					break;
				case TYPE_INSERT_ATTACHMENT:
					insertAttachment(intent);
					break;
				case TYPE_INSERT_SOURCE:
					insertSource(intent);
					break;
				default:
					throw new IllegalArgumentException("Illegal type id: " + getTypeFromExtra(intent));
			}
		} catch (Exception e) {
			MyLog.e(TAG, "[onHandleIntent] sync error. Intent action: " + intent.getAction(), e);
			ActivityResultHelper.sendStatusError(mActivityReceiver, e.getMessage());
			stopSelf();
			return;
		}
		ActivityResultHelper.sendStatusFinished(mActivityReceiver);
		logd("[onHandleIntent] sync finished");
	}

	private int createPointOrderNumber(final Point point) {

		String where = Points.Columns.PERSON_ID + "=" + point.getPersonId() + " AND "
				+ Points.Columns.TOPIC_ID + "=" + point.getTopicId();
		Cursor cursorCount = getContentResolver().query(Points.CONTENT_URI, null, where, null, null);
		if (cursorCount.getCount() == 0) {
			// new point is first
			cursorCount.close();
			return 0;
		}
		String[] columns = new String[] { "MAX(" + Points.Columns.ORDER_NUMBER + ")" };
		Cursor cursorMaxNum = getContentResolver().query(Points.CONTENT_URI, columns, where, null, null);
		int maxOrderNum = 0;
		if (cursorMaxNum.moveToFirst()) {
			maxOrderNum = cursorMaxNum.getInt(0) + 1;
		}
		cursorCount.close();
		cursorMaxNum.close();
		return maxOrderNum;
	}

	private void insertAttachment(final Intent intent) {

		Attachment attachment = intent.getParcelableExtra(ServiceExtraKeys.VALUE);
		logd("[insertAttachment] " + attachment.getTitle());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		Uri attachmentUri = getUriFromExtra(intent);
		int attachmentId;
		ActivityResultHelper.sendStatusStartWithCount(mActivityReceiver, 100);
		switch (attachment.getFormat()) {
			case Attachments.AttachmentType.JPG: {
				String scheme = attachmentUri.getScheme();
				if ("content".equals(scheme)) {
					ActivityResultHelper.sendProgress(mActivityReceiver, "Loading image from sdcard", 0);
					attachment.setTitle(MediaStoreHelper.getTitleFromUri(this, attachmentUri));
				} else if ("file".equals(scheme)) {
					ActivityResultHelper.sendProgress(mActivityReceiver, "Loading image from sdcard", 0);
					attachment.setTitle(attachmentUri.getLastPathSegment());
				} else if ("http".equals(scheme)) {
					ActivityResultHelper.sendProgress(mActivityReceiver, "Downloading image to sdcard", 0);
					String fileName = attachmentUri.getLastPathSegment();
					FileDownloader.downloadFromUrl(attachmentUri.toString(), fileName);
					File file = FileDownloader.createFile(fileName);
					attachmentUri = Uri.fromFile(file);
				}
				else if("data".equals(scheme))
				{
					ActivityResultHelper.sendProgress(mActivityReceiver, "Loading image search result", 0);
					String fileName="searchResaltImageTemp.jpg";
					if(attachmentUri!=null &&  attachmentUri.getLastPathSegment()!=null)
						fileName= attachmentUri.getLastPathSegment();
					
					FileDownloader.saveFileFromBase64(attachmentUri.toString(), fileName);
					File file = FileDownloader.createFile(fileName);
					
					attachmentUri = Uri.fromFile(file);
					
				}
				
				ActivityResultHelper.sendProgress(mActivityReceiver, "Uploading image from sdcard to server",
						30);
				attachmentId = HttpUtil.insertImageAttachment(this, attachmentUri);
				PhotonHelper.sendStatsEvent(StatsEvent.IMAGE_ADDED, selectedPoint, photonReceiver);
				break;
			}
			case Attachments.AttachmentType.PDF:
				ActivityResultHelper.sendProgress(mActivityReceiver, "Loading pdf from sdcard", 0);
				String pdfFileName = attachmentUri.getLastPathSegment();
				attachment.setTitle(pdfFileName.replace(".pdf", ""));
				String scheme = attachmentUri.getScheme();
				logd("[insertAttachment] pdf uri: " + attachmentUri.toString());
				Uri pdfUploadUri;
				if ("http".equals(scheme)) {
					ActivityResultHelper.sendProgress(mActivityReceiver, "Loading pdf to sdcard", 0);
					String fileName = Attachments.getPdfAttachmentFileName(attachmentUri);
					FileDownloader.downloadFromUrl(attachmentUri.toString(), fileName);
					File file = FileDownloader.createFile(fileName);
					pdfUploadUri = Uri.fromFile(file);
				} else if ("file".equals(scheme)) {
					pdfUploadUri = attachmentUri;
				} else {
					pdfUploadUri = null;
				}
				logd("[insertAttachment] upload pdf uri: " + pdfUploadUri.toString());
				ActivityResultHelper.sendProgress(mActivityReceiver, "Uploading pdf from sdcard to server",
						30);
				attachmentId = HttpUtil.insertPdfAttachment(this, pdfUploadUri);
				PhotonHelper.sendStatsEvent(StatsEvent.PDF_ADDED, selectedPoint, photonReceiver);
				break;
			case Attachments.AttachmentType.YOUTUBE:
				ActivityResultHelper.sendProgress(mActivityReceiver, "Uploading youtube video to server", 30);
				OEntity entity = odataWrite.insertAttachment(attachment);
				attachmentId = (Integer) entity.getProperty(Attachments.Columns.ID).getValue();
				attachment.setTitle(YoutubeHelper.getVideoTitle(attachmentUri.toString()));
				attachment.setLink(attachmentUri.toString());
				attachment.setVideoLinkURL(attachmentUri.toString());
				String vid = attachmentUri.getQueryParameter("v");
				attachment.setVideoEmbedURL("http://www.youtube.com/embed/" + vid);
				attachment.setVideoThumbURL(YoutubeHelper.getThumbImageUrl(attachmentUri.toString()));
				PhotonHelper.sendStatsEvent(StatsEvent.YOUTUBE_ADDED, selectedPoint, photonReceiver);
				break;
			default:
				throw new UnsupportedOperationException(
						"[insertAttachment] was called with unknown attachment type: "
								+ attachment.getFormat());
		}
		ActivityResultHelper.sendProgress(mActivityReceiver, "Creating attachment info on server", 60);
		attachment.setName(attachment.getTitle());
		odataWrite.updateAttachment(attachment, attachmentId);
		attachment.setAttachmentId(attachmentId);
		ActivityResultHelper.sendProgress(mActivityReceiver, "Saving attachment info", 80);
		ContentValues cv = attachment.toContentValues();
		getContentResolver().insert(Attachments.CONTENT_URI, cv);
		PhotonHelper.sendArgPointUpdated(selectedPoint, photonReceiver);
	}

	private void insertComment(final Intent intent) {

		Bundle commentBundle = intent.getBundleExtra(ServiceExtraKeys.VALUE);
		Comment comment = new Comment(commentBundle);
		logd("[insertComment] " + comment.getText());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		OEntity entity = odataWrite.insertComment(comment);
		int commentId = (Integer) entity.getProperty(Comments.Columns.ID).getValue();
		logd("[insertComment] new comment id: " + commentId);
		comment.setId(commentId);
		getContentResolver().insert(Comments.CONTENT_URI, comment.toContentValues());
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		PhotonHelper.sendArgPointUpdated(selectedPoint, photonReceiver);
		PhotonHelper.sendStatsEvent(StatsEvent.COMMENT_ADDED, selectedPoint, photonReceiver);
	}

	/**
	 * Comment is marked that it was readed by person.
	 * @param intent
	 */
	private void insertCommentPersonReadedEntry(Intent intent){
		Bundle markBundles=intent.getBundleExtra(ServiceExtraKeys.VALUE);
		CommentPersonReadEntry commentEntry=new CommentPersonReadEntry(markBundles);
		logd("[insertCommentPersonReadedEntry]  COMMENT ID:" + String.valueOf(commentEntry.getCommentId())
				+" PERSON ID:"+String.valueOf(commentEntry.getPersonId()));
		
		OdataWriteClient odataWrite=new OdataWriteClient(this);
		OEntity entity=odataWrite.insertCommentPersonReadedEntry(commentEntry);
		int commentEntryId=(Integer)entity.getProperty(CommentsPersonReadEntry.Columns.ID).getValue();
		logd("[insertCommentEntry] new commentPersonReadedEntry id: " + commentEntryId);
		commentEntry.setId(commentEntryId);
		getContentResolver().insert(CommentsPersonReadEntry.CONTENT_URI, commentEntry.toContentValues());
		SelectedPoint selectedPoint=getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		PhotonHelper.sendArgPointUpdated(selectedPoint, photonReceiver);
		PhotonHelper.sendStatsEvent(StatsEvent.COMMENTPERSONREADEDENTRY_ADDED, selectedPoint, photonReceiver);
	}
	/**
	 * WPF client comment feedback bug fix code. Used only until WPF client will not be fixed.
	 * Such function only insert new comment placeholder which WPF client require. 
	 * <b>When WPF client will be fixed all code with WPFCork prefix can be removed from code files</b>.
	 * 
	 * @param intent
	 */
	private void WPFCork_insertCommentPlaceholder(Intent intent)
	{
		Bundle commentBundle = intent.getBundleExtra(ServiceExtraKeys.VALUE);
		Comment comment = new Comment(commentBundle,false);
		logd("[insertComment] " + comment.getText());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		
		OEntity entity = odataWrite.WPFCork_insertComment(comment);
		
		int commentId = (Integer) entity.getProperty(Comments.Columns.ID).getValue();
		logd("[insertComment] new comment id: " + commentId);
		comment.setId(commentId);
		getContentResolver().insert(Comments.CONTENT_URI, comment.WPFCork_toContentValues());
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		PhotonHelper.sendArgPointUpdated(selectedPoint, photonReceiver);
		PhotonHelper.sendStatsEvent(StatsEvent.COMMENT_ADDED, selectedPoint, photonReceiver);
	}
	
	
	
	private void insertDescription(final Intent intent) {

		Bundle descriptionBundle = intent.getBundleExtra(ServiceExtraKeys.VALUE);
		Description description = new Description(descriptionBundle);
		logd("[insertDescription] " + description.toMyString());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		OEntity entity = odataWrite.insertDescription(description);
		int newId = (Integer) entity.getProperty(Descriptions.Columns.ID).getValue();
		logd("[insertDescription] new description id: " + newId);
		description.setId(newId);
		getContentResolver().insert(Descriptions.CONTENT_URI, description.toContentValues());
	}

	private void insertPointAndDescription(final Intent intent) {

		Bundle pointBundle = intent.getBundleExtra(ServiceExtraKeys.VALUE);
		Point point = new Point(pointBundle);
		logd("[insertPoint] " + point.toMyString());
		point.setOrderNumber(createPointOrderNumber(point));
		Bundle descriptionBundle = intent.getBundleExtra(ServiceExtraKeys.VALUE);
		Description description = new Description(descriptionBundle);
		logd("[insertDescription] " + description.toMyString());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		OEntity entity = odataWrite.insertPoint(point);
		int newPointId = (Integer) entity.getProperty(Points.Columns.ID).getValue();
		logd("[insertPoint] new point id: " + newPointId);
		point.setId(newPointId);
		description.setPointId(newPointId);
		OEntity entityDesription = odataWrite.insertDescription(description);
		int newId = (Integer) entityDesription.getProperty(Descriptions.Columns.ID).getValue();
		logd("[insertDescription] new description id: " + newId);
		description.setId(newId);
		getContentResolver().insert(Points.CONTENT_URI, point.toContentValues());
		getContentResolver().insert(Descriptions.CONTENT_URI, description.toContentValues());
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		PhotonHelper.sendArgPointCreated(point, photonReceiver);
		PhotonHelper.sendStatsEvent(StatsEvent.BADGE_CREATED, selectedPoint, photonReceiver);
	}

	private void insertSource(final Intent intent) {

		Source source = intent.getParcelableExtra(ServiceExtraKeys.VALUE);
		logd("[insertSource] " + source.getLink());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		OEntity entity = odataWrite.insertSource(source);
		int sourceId = (Integer) entity.getProperty(Sources.Columns.ID).getValue();
		logd("[insertSource] new attachment id: " + sourceId);
		source.setSourceId(sourceId);
		ContentValues cv = source.toContentValues();
		getContentResolver().insert(Sources.CONTENT_URI, cv);
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		PhotonHelper.sendArgPointUpdated(selectedPoint, photonReceiver);
		PhotonHelper.sendStatsEvent(StatsEvent.SOURCE_ADDED, selectedPoint, photonReceiver);
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

	private void updateDescription(final Intent intent) {

		Bundle descriptionBundle = intent.getBundleExtra(ServiceExtraKeys.VALUE);
		Description description = new Description(descriptionBundle);
		logd("[updateDescription] " + description.toMyString());
		OdataWriteClient odataWrite = new OdataWriteClient(this);
		odataWrite.updateDescription(description);
		String where = Descriptions.Columns.ID + "=?";
		String[] args = new String[] { String.valueOf(description.getId()) };
		getContentResolver().update(Descriptions.CONTENT_URI, description.toContentValues(), where, args);
	}

	private void updatePoint(final Intent intent) {

		Bundle pointBundle = intent.getBundleExtra(ServiceExtraKeys.VALUE);
		Point point = new Point(pointBundle);
		logd("[updatePoint] " + point.toMyString());
		updatePointOnServer(point);
		updatePointOnLocal(point);
		SelectedPoint selectedPoint = getSelectedPointFromExtra(intent);
		ResultReceiver photonReceiver = getPhotonReceiverFromExtra(intent);
		PhotonHelper.sendArgPointUpdated(point, photonReceiver);
		PhotonHelper.sendStatsEvent(StatsEvent.BADGE_EDITED, selectedPoint, photonReceiver);
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
}
