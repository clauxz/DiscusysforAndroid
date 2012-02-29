package com.slobodastudio.discussions.photon.constants;

import de.exitgames.client.photon.LiteOpKey;

public class LiteLobbyOpKey {

	/** This actor's properties for this room. */
	public static final byte ActorProperties = LiteOpKey.ActorProperties.value();
	/** (242) A lobby-name to connect this room to. */
	public static final byte LobbyName = (byte) 242;
	/** RoomName is a better name, instead of ActorSessionInstanceId (Asid). */
	public static final byte RoomName = LiteOpKey.GameId.value();
}
