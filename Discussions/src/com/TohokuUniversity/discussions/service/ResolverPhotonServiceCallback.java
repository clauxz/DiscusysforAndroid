package com.TohokuUniversity.discussions.service;

import com.TohokuUniversity.discussions.ApplicationConstants;
import com.TohokuUniversity.discussions.data.model.ArgPointChanged;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Points;
import com.TohokuUniversity.discussions.photon.DiscussionUser;
import com.TohokuUniversity.discussions.photon.PhotonServiceCallback;

import android.util.Log;

public class ResolverPhotonServiceCallback implements PhotonServiceCallback {

	private static final boolean DEBUG = true && ApplicationConstants.LOGD_SERVICE;
	private static final String TAG = ResolverPhotonServiceCallback.class.getSimpleName();
	private final ServiceHelper mServiceHelper;

	public ResolverPhotonServiceCallback(final ServiceHelper serviceHelper) {

		super();
		mServiceHelper = serviceHelper;
	}

	@Override
	public void onArgPointChanged(final ArgPointChanged argPointChanged) {

		if (DEBUG) {
			Log.d(TAG, "[onArgPointChanged] point id: " + argPointChanged.getPointId() + " , topic id: "
					+ argPointChanged.getTopicId() + " , event type: " + argPointChanged.getEventType());
		}
		switch (argPointChanged.getEventType()) {
			case Points.PointChangedType.CREATED:
				mServiceHelper.updatePoint(argPointChanged.getPointId());
				break;
			case Points.PointChangedType.MODIFIED:
				mServiceHelper.updatePoint(argPointChanged.getPointId());
				break;
			case Points.PointChangedType.DELETED:
				mServiceHelper.deleteLocalPoint(argPointChanged.getPointId());
				break;
			default:
				Log.e(TAG, "[onArgPointChangedEvent] unknown Point Change Type: "
						+ argPointChanged.getEventType());
				break;
		}
	}

	@Override
	public void onConnect() {

		if (DEBUG) {
			Log.d(TAG, "[onConnect] empty");
		}
	}

	@Override
	public void onErrorOccured(final String message) {

		Log.e(TAG, "[onErrorOccured] Empty. message: " + message);
	}

	@Override
	public void onEventJoin(final DiscussionUser newUser) {

		if (DEBUG) {
			Log.d(TAG, "[onEventJoin] Empty. user come: " + newUser.getUserName());
		}
	}

	@Override
	public void onEventLeave(final DiscussionUser leftUser) {

		if (DEBUG) {
			Log.d(TAG, "[onEventLeave] Empty. user left: " + leftUser.getUserName());
		}
	}

	@Override
	public void onRefreshCurrentTopic() {

		if (DEBUG) {
			Log.d(TAG, "[onRefreshCurrentTopic] Empty. ");
		}
	}

	@Override
	public void onStructureChanged(final int topicId) {

		if (DEBUG) {
			Log.d(TAG, "[onStructureChanged] topic id: " + topicId);
		}
		mServiceHelper.downloadPointsFromTopic(topicId);
	}
}
