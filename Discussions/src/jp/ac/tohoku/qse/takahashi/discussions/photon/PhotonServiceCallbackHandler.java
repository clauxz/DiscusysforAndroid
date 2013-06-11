package jp.ac.tohoku.qse.takahashi.discussions.photon;


import java.util.LinkedList;
import java.util.List;

import jp.ac.tohoku.qse.takahashi.discussions.data.model.ArgPointChanged;

public class PhotonServiceCallbackHandler implements PhotonServiceCallback {

	private final List<PhotonServiceCallback> m_callbacks = new LinkedList<PhotonServiceCallback>();

	public void addCallbackListener(final PhotonServiceCallback cb) {

		if (cb == null) {
			throw new IllegalArgumentException("Cant add callback listener, because it's null");
		}
		m_callbacks.add(cb);
	}

	@Override
	public void onArgPointChanged(final ArgPointChanged argPointChanged) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onArgPointChanged(argPointChanged);
		}
	}

	@Override
	public void onConnect() {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onConnect();
		}
	}

	@Override
	public void onErrorOccured(final String message) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onErrorOccured(message);
		}
	}

	@Override
	public void onEventJoin(final DiscussionUser newUser) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onEventJoin(newUser);
		}
	}

	@Override
	public void onEventLeave(final DiscussionUser leftUser) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onEventLeave(leftUser);
		}
	}

	@Override
	public void onRefreshCurrentTopic() {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onRefreshCurrentTopic();
		}
	}

	@Override
	public void onStructureChanged(final int topicId) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onStructureChanged(topicId);
		}
	}

	public void removeCallbackListener(final PhotonServiceCallback cb) {

		if (cb == null) {
			throw new IllegalArgumentException("Cant add callback listener, because it's null");
		}
		m_callbacks.remove(cb);
	}
}
