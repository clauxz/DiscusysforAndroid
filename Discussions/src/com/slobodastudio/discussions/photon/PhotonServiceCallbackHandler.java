package com.slobodastudio.discussions.photon;

import java.util.LinkedList;
import java.util.List;

public class PhotonServiceCallbackHandler implements PhotonServiceCallback {

	private final List<PhotonServiceCallback> m_callbacks = new LinkedList<PhotonServiceCallback>();

	public void addCallbackListener(final PhotonServiceCallback cb) {

		if (cb == null) {
			throw new IllegalArgumentException("Cant add callback listener, because it's null");
		}
		m_callbacks.add(cb);
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
	public void onEventJoin(DiscussionUser newUser) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onEventJoin(null);
		}
	}

	@Override
	public void onEventLeave(final DiscussionUser leftUser) {

		for (PhotonServiceCallback h : m_callbacks) {
			h.onEventLeave(leftUser);
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