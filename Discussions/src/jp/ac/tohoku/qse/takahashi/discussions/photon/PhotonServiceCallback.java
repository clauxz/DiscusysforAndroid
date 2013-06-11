package jp.ac.tohoku.qse.takahashi.discussions.photon;

import jp.ac.tohoku.qse.takahashi.discussions.data.model.ArgPointChanged;

public interface PhotonServiceCallback {

	void onArgPointChanged(ArgPointChanged argPointChanged);

	void onConnect();

	void onErrorOccured(String message);

	void onEventJoin(DiscussionUser newUser);

	void onEventLeave(DiscussionUser leftUser);

	void onRefreshCurrentTopic();

	void onStructureChanged(int topicId);
}
