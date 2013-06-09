package com.TohokuUniversity.discussions.photon.constants;

/** Device type for a time line and SPSS integration */
public class DeviceType {

	public static final int ANDROID = 0;
	public static final int STICKY = 2;
	public static final int WPF = 1;

	/** A private Constructor prevents class from instantiating. */
	private DeviceType() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}
}
