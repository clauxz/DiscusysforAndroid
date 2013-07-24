package jp.ac.tohoku.qse.takahashi.discussions.photon.constants;

public class StatsEvent {

	public static final int ARG_POINT_TOPIC_CHANGED = 19;
	public static final int BADGE_CREATED = 2;
	public static final int BADGE_EDITED = 3;
	public static final int BADGE_MOVED = 4;
	public static final int BADGE_ZOOM_IN = 5;
	public static final int CLUSTER_CREATED = 6;
	public static final int CLUSTER_DELETED = 7;
	public static final int CLUSTER_IN = 8;
	public static final int CLUSTER_MOVED = 10;
	public static final int CLUSTER_OUT = 9;
	public static final int COMMENT_ADDED = 29;
	public static final int COMMENT_REMOVED = 30;
	public static final int COMMENTPERSONREADEDENTRY_ADDED=32;
	public static final int FREE_DRAWING_CREATED = 13;
	public static final int FREE_DRAWING_MOVED = 16;
	public static final int FREE_DRAWING_REMOVED = 14;
	public static final int FREE_DRAWING_RESIZE = 15;
	public static final int IMAGE_ADDED = 22;
	public static final int IMAGE_URL_ADDED = 23;
	public static final int LINK_CREATED = 11;
	public static final int LINK_REMOVED = 12;
	public static final int MEDIA_REMOVED = 28;
	public static final int URL_REMOVED=31;
	public static final int PDF_ADDED = 24;
	public static final int PDF_URL_ADDED = 25;
	public static final int RECORDIND_STARTED = 0;
	public static final int RECORDING_STOPPED = 1;
	public static final int SCENE_ZOOMED_IN = 17;
	public static final int SCENE_ZOOMED_OUT = 18;
	public static final int SCREENSHOT_ADDED = 27;
	public static final int SOURCE_ADDED = 20;
	public static final int YOUTUBE_ADDED = 26;

	/** A private Constructor prevents class from instantiating. */
	private StatsEvent() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}
}
