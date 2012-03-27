package com.slobodastudio.discussions;

/** Sets of global application constants. */
public class ApplicationConstants {

	public static final String BUG_SENSE_API_KEY = "8b8433f6";
	public static final boolean DEV_MODE = false;
	public static final boolean ODATA_LOCAL = false && DEV_MODE;
	public static final boolean ODATA_SANITIZE = true && DEV_MODE;
	public static final boolean PHOTON_LOCAL = false && DEV_MODE;
	public static final boolean PROVIDER_LOCAL = false && DEV_MODE;

	/** A private Constructor prevents class from instantiating. */
	private ApplicationConstants() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}
}
