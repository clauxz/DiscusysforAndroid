package jp.ac.tohoku.qse.takahashi.discussions.service;

import jp.ac.tohoku.qse.takahashi.discussions.R;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.sun.jersey.api.client.ClientHandlerException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.core4j.Enumerable;
import org.odata4j.core.OEntity;

import java.io.IOException;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.DataIoException;
import jp.ac.tohoku.qse.takahashi.discussions.data.PreferenceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.data.SharedPreferenceHelper;
import jp.ac.tohoku.qse.takahashi.discussions.data.odata.BaseOdataClient;
import jp.ac.tohoku.qse.takahashi.discussions.data.odata.HttpUtil;
import jp.ac.tohoku.qse.takahashi.discussions.data.odata.OdataReadClientWithBatchTransactions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Attachments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Descriptions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Discussions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Seats;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sessions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sources;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Topics;
import jp.ac.tohoku.qse.takahashi.discussions.ui.IntentAction;
import jp.ac.tohoku.qse.takahashi.discussions.ui.IntentHelper;
import jp.ac.tohoku.qse.takahashi.discussions.utils.ConnectivityUtil;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MyLog;

/** Background {@link Service} that synchronizes data living in {@link ScheduleProvider}. */
public class DownloadService extends IntentService {

	public static final int TYPE_ALL = 0x0;
	public static final int TYPE_POINT_FROM_TOPIC = 0x2;
	public static final int TYPE_UPDATE_POINT = 0x7;
	public static final int TYPE_PDF_FILE = 0x8;
	public static final int TYPE_SESSIONS = 0x9;
	public static final int TYPE_ALL_PER_SESSION = 0x10;
	private static final boolean DEBUG = true && ApplicationConstants.LOGD_SERVICE;
	private static final String TAG = DownloadService.class.getSimpleName();
	private ResultReceiver activityReceiver;

	public DownloadService() {

		super(TAG);
	}

	private static ResultReceiver getActivityReceiverFromExtra(final Intent intent) {

		return intent.getParcelableExtra(ServiceExtraKeys.ACTIVITY_RECEIVER);
	}

	private static final String getTypeAsString(final int typeId) {

		switch (typeId) {
			case TYPE_ALL:
				return "ALL";
			case TYPE_UPDATE_POINT:
				return "updated point";
			case TYPE_POINT_FROM_TOPIC:
				return "points for topic";
			case TYPE_PDF_FILE:
				return "pdf files";
			case TYPE_SESSIONS:
				return "session";
			case TYPE_ALL_PER_SESSION:
				return "all per session";
			default:
				throw new IllegalArgumentException("Illegal type id: " + typeId);
		}
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

		if (!IntentAction.DOWNLOAD.equals(intent.getAction())) {
			throw new IllegalArgumentException("Service was started with unknown intent action: "
					+ intent.getAction());
		}
		if (intent.getExtras() == null) {
			throw new IllegalArgumentException("Service was started without extras");
		}
		if (!intent.hasExtra(ServiceExtraKeys.ACTIVITY_RECEIVER)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.ACTIVITY_RECEIVER);
		}
		if (!intent.hasExtra(ServiceExtraKeys.TYPE_ID)) {
			throw new IllegalArgumentException("Service was started without extras: "
					+ ServiceExtraKeys.TYPE_ID);
		}
	}

	@Override
	protected void onHandleIntent(final Intent intent) {

		logd("[onHandleIntent] intent: " + intent.toString());
		validateIntent(intent);
		activityReceiver = getActivityReceiverFromExtra(intent);
		if (isConnected()) {
			ActivityResultHelper.sendProgress(activityReceiver, 
					getString(R.string.progress_connecting), 0);
		} else {
			String errorString = getString(R.string.text_error_network_off);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
			return;
		}
		try {
			switch (getTypeFromExtra(intent)) {
				case TYPE_ALL:
					downloadAll();
					break;
				case TYPE_UPDATE_POINT:
					updatePoint(intent);
					break;
				case TYPE_POINT_FROM_TOPIC:
					downloadPointsFromTopic(intent);
					break;
				case TYPE_PDF_FILE:
					downloadPdfFile(intent);
					break;
				case TYPE_SESSIONS:
					downloadSessions(intent);
					break;
				case TYPE_ALL_PER_SESSION:
					downloadPerSession(intent);
					break;
				default:
					throw new IllegalArgumentException("Illegal type id: " + getTypeFromExtra(intent));
			}
		} catch (ClientHandlerException e) {
			MyLog.e(TAG, "[onHandleIntent] ClientHandlerException. Intent action: " + intent.getAction(), e);
			String errorString = getString(R.string.text_error_client_handler);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
			return;
		} catch (DataIoException e) {
			MyLog.e(TAG, "[onHandleIntent] DataIoException. Intent action: " + intent.getAction(), e);
			int downloadType = getTypeFromExtra(intent);
			String errorMsg = getString(R.string.text_error_database_io, getTypeAsString(downloadType));
			ActivityResultHelper.sendStatusError(activityReceiver, errorMsg);
			return;
		} catch (Exception e) {
			MyLog.e(TAG, "[onHandleIntent] sync error. Intent action: " + intent.getAction(), e);
			ActivityResultHelper.sendStatusError(activityReceiver, e.getMessage());
			return;
		}
		ActivityResultHelper.sendStatusFinished(activityReceiver);
		logd("[onHandleIntent] sync finished");
	}

	private void downloadAll() {

		logd("[downloadAll]");
		long startTime = System.currentTimeMillis();
		if (!testConnection()) {
			return;
		}
		// TODO: delete all rows in all tables, clean cache here
		// TODO: only download new (without delete in odata service)
		// TODO: progress download sessions
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_calculate_count), 0);
		ImageLoader.getInstance().clearDiscCache();
		int downloadedCount = 0;
		//
		int sessionCount = getTableCount(Sessions.TABLE_NAME);
		int seatsCount = getTableCount(Seats.TABLE_NAME);
		int personsCount = getTableCount(Persons.TABLE_NAME);
		int discussionsCount = getTableCount(Discussions.TABLE_NAME);
		int pointsCount = getTableCount(Points.TABLE_NAME);
		int topicsCount = getTableCount(Topics.TABLE_NAME);
		int attachmentsCount = getTableCount(Attachments.TABLE_NAME);
		int sourcesCount = getTableCount(Sources.TABLE_NAME);
		int descriptionCount = getTableCount(Descriptions.TABLE_NAME);
		int commentsCount = getTableCount(Comments.TABLE_NAME);		
		int commentsPersonEntity=getTableCount(CommentsPersonReadEntry.TABLE_NAME);
		
		int totalCount = sessionCount + seatsCount + personsCount + discussionsCount + pointsCount
				+ topicsCount + attachmentsCount + sourcesCount + descriptionCount + commentsCount
				+commentsPersonEntity;
		
		ActivityResultHelper.sendStatusStartWithCount(activityReceiver, totalCount);
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_sessions), downloadedCount);
		OdataReadClientWithBatchTransactions odataClient = new OdataReadClientWithBatchTransactions(this);
		//
		odataClient.refreshSessions();
		logd("[downloadAll] sessions completed");
		downloadedCount += sessionCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_seats),
				downloadedCount);
		//
		odataClient.refreshSeats();
		logd("[downloadAll] seats completed");
		downloadedCount += seatsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_persons),
				downloadedCount);
		//
		odataClient.refreshPersons();
		logd("[downloadAll] persons completed");
		downloadedCount += personsCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_discussions), downloadedCount);
		//
		odataClient.refreshDiscussions();
		logd("[downloadAll] discussions completed");
		downloadedCount += discussionsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_topics),
				downloadedCount);
		odataClient.refreshTopics();
		logd("[downloadAll] topics completed");
		downloadedCount += topicsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_points),
				downloadedCount);
		odataClient.refreshPoints();
		logd("[downloadAll] points completed");
		downloadedCount += pointsCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_descriptions), downloadedCount);
		odataClient.refreshDescriptions();
		logd("[downloadAll] descriptions completed");
		downloadedCount += descriptionCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_comments), downloadedCount);
		odataClient.refreshComments();
		logd("[downloadAll] comments completed");
		downloadedCount += commentsCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_attachments), downloadedCount);
		odataClient.refreshAttachments();
		logd("[downloadAll] attachments completed");
		downloadedCount += attachmentsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_sources),
				downloadedCount);
		odataClient.refreshSources();
		logd("[downloadAll] sources completed");
		downloadedCount += sourcesCount;
		
		
		//comments person readed  entities
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_commentpersonentity),
				downloadedCount);
		odataClient.refreshCommentsPersonEntry();
		logd("[downloadAll] comments person read entities compleated");
		downloadedCount+=commentsPersonEntity;
		
		
				
		odataClient.applyBatchOperations();
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_finished), downloadedCount);
		logd("[downloadAll] load time: " + (System.currentTimeMillis() - startTime));
		SharedPreferenceHelper.setUpdatedTime(this, System.currentTimeMillis());
	}

	private void downloadPointsFromTopic(final Intent intent) {

		int topicId = getValueIdFromExtra(intent);
		if (topicId < 0) {
			throw new IllegalArgumentException("Illegal topic id for download points: " + topicId);
		}
		logd("[downloadPointsFromTopic] topic id: " + topicId);
		OdataReadClientWithBatchTransactions odata = new OdataReadClientWithBatchTransactions(this);
		odata.updateTopicPoints(topicId);
		odata.applyBatchOperations();
	}

	private Integer getTableCount(final String tableName) {

		String odataUrl = PreferenceHelper.getOdataUrl(this);
		String count = HttpUtil.getString(odataUrl + tableName + "/$count");
		if (TextUtils.isEmpty(count)) {
			return 0;
		}
		try {
			return Integer.valueOf(count);
		} catch (NumberFormatException e) {
			MyLog.e(TAG, "Failed to parse string as integer: " + count, e);
		}
		return 0;
	}

	private boolean isConnected() {

		boolean connected = false;
		if (ApplicationConstants.DEV_MODE) {
			connected = true;
		} else {
			connected = ConnectivityUtil.isNetworkConnected(this);
		}
		return connected;
	}

	private boolean testConnection() {

		String odataUrl = PreferenceHelper.getOdataUrl(this);
		HttpGet httpGet = new HttpGet(odataUrl);
		try {
			HttpUtil.getHttpClient().execute(httpGet);
			return true;
		} catch (ClientProtocolException e) {
			Log.e(TAG, "Couldnt make a connection to: " + odataUrl, e);
			String errorString = getString(R.string.text_error_client_handler);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
		} catch (IOException e) {
			Log.e(TAG, "Couldnt make a connection to: " + odataUrl, e);
			String errorString = getString(R.string.text_error_client_handler);
			ActivityResultHelper.sendStatusError(activityReceiver, errorString);
		}
		return false;
	}

	private void updatePoint(final Intent intent) {

		int pointId = getValueIdFromExtra(intent);
		if (pointId < 0) {
			throw new IllegalArgumentException("Illegal point id for download: " + pointId);
		}
		logd("[updatePoint] point id: " + pointId);
		OdataReadClientWithBatchTransactions odata = new OdataReadClientWithBatchTransactions(this);
		odata.updatePoint(pointId);
		odata.applyBatchOperations();
	}

	private void downloadPerSession(final Intent intent) {

		int sessionId = getValueIdFromExtra(intent);
		if (sessionId < 0) {
			throw new IllegalArgumentException("Illegal session id for download: " + sessionId);
		}
		logd("[downloadPerSession] session id: " + sessionId);
		long startTime = System.currentTimeMillis();
		if (!testConnection()) {
			return;
		}
		// TODO: delete all rows in all tables, clean cache here
		// TODO: only download new (without delete in odata service)
		// TODO: progress download sessions
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_calculate_count), 0);
		int downloadedCount = 0;
		//
		int seatsCount = 10;
		int personsCount = 10;
		int discussionsCount = 10;
		int pointsCount = 10;
		int topicsCount = 10;
		int attachmentsCount = 10;
		int sourcesCount = 10;
		int descriptionCount = 10;
		int commentsCount = 10;
		int totalCount = seatsCount + personsCount + discussionsCount + pointsCount + topicsCount
				+ attachmentsCount + sourcesCount + descriptionCount + commentsCount;
		ActivityResultHelper.sendStatusStartWithCount(activityReceiver, totalCount);
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_seats),
				downloadedCount);
		OdataReadClientWithBatchTransactions odataClient = new OdataReadClientWithBatchTransactions(this);
		//
		odataClient.refreshSeats();
		logd("[downloadPerSession] seats completed");
		downloadedCount += seatsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_persons),
				downloadedCount);
		//
		Enumerable<OEntity> discussions = odataClient.refreshDiscussions();
		logd("[downloadPerSession] discussions completed");
		downloadedCount += discussionsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_topics),
				downloadedCount);
		//
		Enumerable<OEntity> persons = odataClient.refreshPersonsFromSession(sessionId);
		logd("[downloadPerSession] persons completed");
		downloadedCount += personsCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_discussions), downloadedCount);
		//
		odataClient.refreshTopics();
		logd("[downloadPerSession] topics completed");
		downloadedCount += topicsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_points),
				downloadedCount);
		//
		StringBuilder sb = new StringBuilder();
		int index = 0;
		for (OEntity person : persons) {
			if (index > 0) {
				sb.append(" or ");
			}
			sb.append("Person/Id eq ");
			int personId = BaseOdataClient.getAsInt(person, Persons.Columns.ID);
			sb.append(personId);
			index++;
		}
		String personSelection = sb.toString();
		MyLog.tempv("person selection: " + personSelection);
		odataClient.refreshPointsFromPerson(personSelection);
		logd("[downloadPerSession] points completed");
		downloadedCount += pointsCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_descriptions), downloadedCount);
		//
		odataClient.refreshDescriptions();
		logd("[downloadPerSession] descriptions completed");
		downloadedCount += descriptionCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_comments), downloadedCount);
		//
		odataClient.refreshCommentsFromPerson(personSelection);
		logd("[downloadPerSession] comments completed");
		downloadedCount += commentsCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_attachments), downloadedCount);
		//
		index = 0;
		sb = new StringBuilder();
		for (OEntity discussion : discussions) {
			if (index > 0) {
				sb.append(" or ");
			}
			sb.append("Discussion/Id eq ");
			int discussionId = BaseOdataClient.getAsInt(discussion, Discussions.Columns.ID);
			sb.append(discussionId);
			index++;
		}
		MyLog.tempv("attachment select: " + sb.toString());
		odataClient.refreshAttachments(personSelection, sb.toString());
		// TODO: download discussion attachments
		logd("[downloadPerSession] attachments completed");
		downloadedCount += attachmentsCount;
		ActivityResultHelper.sendProgress(activityReceiver, getString(R.string.progress_downloading_sources),
				downloadedCount);
		//
		odataClient.refreshSources();
		logd("[downloadPerSession] sources completed");
		odataClient.applyBatchOperations();
		downloadedCount += sourcesCount;
		ActivityResultHelper.sendProgress(activityReceiver,
				getString(R.string.progress_downloading_finished), downloadedCount);
		logd("[downloadPerSession] load time: " + (System.currentTimeMillis() - startTime));
		SharedPreferenceHelper.setUpdatedTime(this, System.currentTimeMillis());
	}

	private void downloadSessions(final Intent intent) {

		logd("[downloadSessions]");
		OdataReadClientWithBatchTransactions odata = new OdataReadClientWithBatchTransactions(this);
		odata.refreshSessions();
		odata.applyBatchOperations();
	}

	private void downloadPdfFile(final Intent intent) {

		Uri uri = intent.getData();
		String pdfUrl = uri.getEncodedPath();
		logd("[downloadPdfFile] url: " + pdfUrl);
		String fileName = uri.getLastPathSegment();
		logd("[dowloadPdfFile] fileName: " + fileName);
		FileDownloader.downloadFromUrl(pdfUrl, fileName);
		Intent pdfViewIntent = IntentHelper.getViewPdfIntent(fileName);
		pdfViewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(pdfViewIntent);
	}
}
