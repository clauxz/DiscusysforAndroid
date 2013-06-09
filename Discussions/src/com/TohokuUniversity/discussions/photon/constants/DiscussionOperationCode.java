package com.TohokuUniversity.discussions.photon.constants;

import de.exitgames.client.photon.enums.LiteOpCode;

public class DiscussionOperationCode {

	public static final byte CLUSTER_BADGE_REQUEST = 25;
	public static final byte CLUSTER_MOVE_REQUEST = 27;
	public static final byte CLUSTER_STATS_REQUEST = 32;
	/** client creates shape */
	public static final byte CREATE_SHAPE_REQUEST = 14;
	/** client tries to set (or unset) own cursor on shape */
	public static final byte CURSOR_REQUEST = 13;
	public static final byte DEDITOR_REPORT = 33;
	/** client removes shape */
	public static final byte DELETE_SHAPES_REQUEST = 15;
	public static final byte DELETE_SINGLE_SHAPE_REQUEST = 18;
	public static final byte GET_PROPERTIES = LiteOpCode.GetProperties;
	/** when client connects to annotation stream; it sends initial load request to server */
	public static final byte INITIAL_SCENE_LOAD_REQUEST = 21;
	public static final byte INK_REQUEST = 28;
	public static final byte JOIN = LiteOpCode.Join;
	public static final byte LEAVE = LiteOpCode.Leave;
	public static final byte LINK_CREATE_REQUEST = 22;
	public static final byte LINK_REPORT_REQUEST = 34;
	/** when user edits annotation and saves it, the client sends this, server broadcasts to other clients */
	public static final byte NOTIFY_ANNOTATION_UPDATED = 9;
	/** any attribute of given arg.point changed. replacement for NotifyStructureChanged when only single
	 * arg.point changed */
	public static final byte NOTIFY_ARGPOINT_CHANGED = 7;
	/** badge was expanded or collapsed */
	public static final byte NOTIFY_BADGE_EXPANSION_CHANGED = 4;
	/** client moved a badge and pushes changes to server; server broadcasts the changes to all clients in the
	 * room */
	public static final byte NOTIFY_BADGE_GEOMETRY_CHANGED = 2;
	/** server supports online status list across all discussions. when client disconnects, server detects
	 * disconnection and puts the user offline. it takes time. client can send this notification to inform
	 * server it's about to disconnect to speed up things. lite leave request doesn't work */
	public static final byte NOTIFY_LEAVE_USER = 8;
	/** when user changes his name after login (by clicking user name near avatar at top right of main form)
	 * other clients show previous name in online list. By sending this to server, client ensures that server
	 * broadcasts [acc plus minus] event to force all clients to update online list */
	public static final byte NOTIFY_NAME_CHANGED = 12;
	/** group/ungroup or drag/drop operation happened */
	public static final byte NOTIFY_STRUCTURE_CHANGED = 5;
	/** when moderator creates new user account or deletes existing one, the client sends this to server.
	 * server broadcasts to other clients, including the original client */
	public static final byte NOTIFY_USER_ACC_PLUS_MINUS = 10;
	/** client notifies its mouse cursor moved */
	public static final byte NOTIFY_USER_CURSOR_STATE = 6;
	/** client requests geometry of badges from server */
	public static final byte REQUEST_BADGE_GEOMETRY = 1;
	/** client asks server to refresh list of points (there are added/deleted points) */
	public static final byte REQUEST_SYNC_POINTS = 3;
	/** shape state changed; initiator notifies */
	public static final byte STATE_SYNC_REQUEST = 20;
	/** when one of stats events occurs, client who generated the event sends it to server. server adds event
	 * record to DB */
	public static final byte STATS_EVENT = 11;
	public static final byte TEST = 0;
	public static final byte UNCLUSTER_BADGE_REQUEST = 24;
	/** client unselected all shapes */
	public static final byte UNSELECT_ALL_REQUEST = 17;

	/** A private Constructor prevents class from instantiating. */
	private DiscussionOperationCode() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}

	public static String asString(final byte operationCode) {

		switch (operationCode) {
			case CLUSTER_BADGE_REQUEST:
				return "CLUSTER_BADGE_REQUEST";
			case CLUSTER_MOVE_REQUEST:
				return "CLUSTER_MOVE_REQUEST";
			case CREATE_SHAPE_REQUEST:
				return "CREATE_SHAPE_REQUEST";
			case CURSOR_REQUEST:
				return "CURSOR_REQUEST";
			case DELETE_SHAPES_REQUEST:
				return "DELETE_SHAPES_REQUEST";
			case DELETE_SINGLE_SHAPE_REQUEST:
				return "DELETE_SINGLE_SHAPE_REQUEST";
			case GET_PROPERTIES:
				return "GET_PROPERTIES";
			case INITIAL_SCENE_LOAD_REQUEST:
				return "INITIAL_SCENE_LOAD_REQUEST";
			case INK_REQUEST:
				return "INK_REQUEST";
			case JOIN:
				return "JOIN";
			case LEAVE:
				return "LEAVE";
			case LINK_CREATE_REQUEST:
				return "LINK_CREATE_REQUEST";
			case NOTIFY_ANNOTATION_UPDATED:
				return "NOTIFY_ANNOTATION_UPDATED";
			case NOTIFY_ARGPOINT_CHANGED:
				return "NOTIFY_ARGPOINT_CHANGED";
			case NOTIFY_BADGE_EXPANSION_CHANGED:
				return "NOTIFY_BADGE_EXPANSION_CHANGED";
			case NOTIFY_BADGE_GEOMETRY_CHANGED:
				return "NOTIFY_BADGE_GEOMETRY_CHANGED";
			case NOTIFY_LEAVE_USER:
				return "NOTIFY_LEAVE_USER";
			case NOTIFY_NAME_CHANGED:
				return "NOTIFY_NAME_CHANGED";
			case NOTIFY_STRUCTURE_CHANGED:
				return "NOTIFY_STRUCTURE_CHANGED";
			case NOTIFY_USER_ACC_PLUS_MINUS:
				return "NOTIFY_USER_ACC_PLUS_MINUS";
			case NOTIFY_USER_CURSOR_STATE:
				return "NOTIFY_USER_CURSOR_STATE";
			case REQUEST_BADGE_GEOMETRY:
				return "REQUEST_BADGE_GEOMETRY";
			case REQUEST_SYNC_POINTS:
				return "REQUEST_SYNC_POINTS";
			case STATE_SYNC_REQUEST:
				return "STATE_SYNC_REQUEST";
			case STATS_EVENT:
				return "STATS_EVENT";
			case TEST:
				return "TEST";
			case UNCLUSTER_BADGE_REQUEST:
				return "UNCLUSTER_BADGE_REQUEST";
			case UNSELECT_ALL_REQUEST:
				return "UNSELECT_ALL_REQUEST";
			case CLUSTER_STATS_REQUEST:
				return "CLUSTER_STATS_REQUEST";
			case DEDITOR_REPORT:
				return "DEDITOR_REPORT";
			case LINK_REPORT_REQUEST:
				return "LINK_REPORT_REQUEST";
			default:
				return "Inknown operation code: " + operationCode;
		}
	}
}
