package jp.ac.tohoku.qse.takahashi.discussions.photon.constants;

public class DiscussionParameterKey {

	public static final byte ANCHOR_X = 31;
	public static final byte ANCHOR_Y = 32;
	public static final byte ANNOTATION_ID = 19;
	public static final byte ARG_POINT_ID = 11;
	public static final byte ARRAY_OF_BYTES = 46;
	public static final byte ARRAY_OF_DOUBLES = 36;
	public static final byte ARRAY_OF_IDS = 4;
	public static final byte ARRAY_OF_INTS = 35;
	public static final byte ARRAY_OF_ORIENTATIONS = 3;
	public static final byte ARRAY_OF_VIEW_TYPES = 12;
	public static final byte ARRAY_OF_X = 1;
	public static final byte ARRAY_OF_Y = 2;
	public static final byte AUTO_TAKE_CURSOR = 37;
	public static final byte BADGE_EXPANSION_FLAG = 10;
	public static final byte BOOL_PARAMETER1 = 28;
	public static final byte BOOL_PARAMETER2 = 29;
	public static final byte BOX_HEIGHT = 9;
	public static final byte BOX_WIDTH = 8;
	public static final byte CALL_TOKEN = 44;
	public static final byte CHANGED_TOPIC_ID = 13;
	public static final byte CLUSTER_ID = 42;
	public static final byte DEVICE_TYPE = 24;
	public static final byte DISCUSSION_ID = 22;
	public static final byte DO_BROADCAST = 43;
	public static final byte DO_LOCK = 26;
	/** structure change notification not fired on initiating client. but when moderator dashboad is open, and
	 * moderator adds/removes topic, and private board is open, we want the list of topics in private board to
	 * be updated. */
	public static final byte FORCE_SELF_NOTIFICATION = 20;
	public static final byte GEOMETRY_CHANGED_WITHSTRUCT = 14;
	public static final byte INITIAL_SHAPE_OWNER_ID = 33;
	public static final byte INK_DATA = 45;
	public static final byte LINK_END1_ID = 40;
	public static final byte LINK_END2_ID = 41;
	public static final byte MESSAGE = 0;
	public static final byte NUM_ARRAY_ENTRIES = 5;
	public static final byte POINT_CHANGE_TYPE = 39;
	public static final byte RADIUS = 30;
	public static final byte SHAPE_ID = 25;
	public static final byte SHAPE_OWNER_ID = 27;
	public static final byte SHAPE_TYPE = 34;
	public static final byte STATS_EVENT = 21;
	public static final byte STRUCT_CHANGE_ACTOR_NR = 15;
	public static final byte TAG = 38;
	public static final byte USER_CURSOR_NAME = 6;
	public static final byte USER_CURSOR_STATE = 7;
	public static final byte USER_CURSOR_USR_ID = 18;
	public static final byte USER_CURSOR_X = 16;
	public static final byte USER_CURSOR_Y = 17;
	public static final byte USER_ID = 23;

	/** A private Constructor prevents class from instantiating. */
	private DiscussionParameterKey() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}
}
