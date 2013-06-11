package jp.ac.tohoku.qse.takahashi.discussions.photon.constants;

import de.exitgames.client.photon.enums.LiteEventCode;

public class DiscussionEventCode {

	/** some annotation was updated */
	public static final byte ANNOTATION_CHANGED = 6;
	public static final byte APPLY_POINT_EVENT = 15;
	/** arg.point changed */
	public static final byte ARG_POINT_CHANGED = 4;
	/** Server notifies client about changed geometry. */
	public static final byte BADGE_GEOMETRY_CHANGED = 0;
	public static final byte CLUSTER_BADGE_EVENT = 20;
	public static final byte CLUSTER_STATS_EVENT = 24;
	/** client created shape */
	public static final byte CREATE_SHAPE_EVENT = 10;
	/** client locked vector shape */
	public static final byte CURSOR_EVENT = 9;
	public static final byte DEDITOR_REPORT_EVENT = 23;
	public static final byte DELETE_SINGLE_SHAPE_EVENT = 14;
	public static final byte INK_EVENT = 21;
	/** used for instant updates of global online list 1. in response to NotifyLeaveNotify server notifies
	 * clients for fast onlien list update 2. client joins lobby, not discussion room. */
	public static final byte INSTANT_USER_PLUS_MINUS = 5;
	public static final byte JOIN = LiteEventCode.Join;
	public static final byte LEAVE = LiteEventCode.Leave;
	public static final byte LINK_CREATE_EVENT = 17;
	public static final byte LINK_STATS_EVENT = 25;
	public static final byte SCENE_LOADING_DONE = 22;
	public static final byte STATE_SYNC_EVENT = 16;
	/** server broadcasts stats event */
	public static final byte STATS_EVENT = 8;
	/** Client notified server structure was changed, broadcast. */
	public static final byte STRUCTURE_CHANGED = 2;
	public static final byte UNCLUSTER_BADGE_EVENT = 19;
	public static final byte UNSELECT_ALL_EVENT = 13;
	/** new user account created/deleted */
	public static final byte USER_ACC_PLUS_MINUS = 7;
	public static final byte USER_CURSOR_CHANGED = 3;

	/** A private Constructor prevents class from instantiating. */
	private DiscussionEventCode() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static String asString(final byte eventCode) {

		switch (eventCode) {
			case ANNOTATION_CHANGED:
				return "ANNOTATION_CHANGED";
			case APPLY_POINT_EVENT:
				return "APPLY_POINT_EVENT";
			case ARG_POINT_CHANGED:
				return "ARG_POINT_CHANGED";
			case BADGE_GEOMETRY_CHANGED:
				return "BADGE_GEOMETRY_CHANGED";
			case CLUSTER_BADGE_EVENT:
				return "CLUSTER_BADGE_EVENT";
			case CREATE_SHAPE_EVENT:
				return "CREATE_SHAPE_EVENT";
			case CURSOR_EVENT:
				return "CURSOR_EVENT";
			case DELETE_SINGLE_SHAPE_EVENT:
				return "DELETE_SINGLE_SHAPE_EVENT";
			case INK_EVENT:
				return "INK_EVENT";
			case INSTANT_USER_PLUS_MINUS:
				return "INSTANT_USER_PLUS_MINUS";
			case JOIN:
				return "JOIN";
			case LEAVE:
				return "LEAVE";
			case LINK_CREATE_EVENT:
				return "LINK_CREATE_EVENT";
			case STATE_SYNC_EVENT:
				return "STATE_SYNC_EVENT";
			case STATS_EVENT:
				return "STATS_EVENT";
			case STRUCTURE_CHANGED:
				return "STRUCTURE_CHANGED";
			case UNCLUSTER_BADGE_EVENT:
				return "UNCLUSTER_BADGE_EVENT";
			case UNSELECT_ALL_EVENT:
				return "UNSELECT_ALL_EVENT";
			case USER_ACC_PLUS_MINUS:
				return "USER_ACC_PLUS_MINUS";
			case USER_CURSOR_CHANGED:
				return "USER_CURSOR_CHANGED";
			case SCENE_LOADING_DONE:
				return "SCENE_LOADING_DONE";
			case DEDITOR_REPORT_EVENT:
				return "DEDITOR_REPORT_EVENT";
			case CLUSTER_STATS_EVENT:
				return "CLUSTER_STATS_EVENT";
			case LINK_STATS_EVENT:
				return "LINK_STATS_EVENT";
			default:
				return "Inknown event code: " + eventCode;
		}
	}
}
