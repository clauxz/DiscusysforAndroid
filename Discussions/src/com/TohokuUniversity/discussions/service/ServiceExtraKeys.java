package com.TohokuUniversity.discussions.service;

public class ServiceExtraKeys {

	public static final String ACTIVITY_RECEIVER = "intent.extra.key.ACTIVITY_RECEIVER";
	public static final String PHOTON_RECEIVER = "intent.extra.key.PHOTON_RECEIVER";
	public static final String SELECTED_POINT = "intent.extra.key.EXTRA_SELECTED_POINT";
	public static final String TYPE_ID = "intent.extra.key.EXTRA_TYPE_ID";
	public static final String URI = "intent.extra.key.EXTRA_URI";
	public static final String VALUE = "intent.extra.key.EXTRA_VALUE";
	public static final String VALUE_ID = "intent.extra.key.EXTRA_VALUE_ID";

	/** A private Constructor prevents class from instantiating. */
	private ServiceExtraKeys() throws UnsupportedOperationException {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}
}
