package jp.ac.tohoku.qse.takahashi.discussions.service;

public interface OdataSyncResultListener {

	void handleError(String message);

	void updateSyncStatus(boolean syncing);
}
