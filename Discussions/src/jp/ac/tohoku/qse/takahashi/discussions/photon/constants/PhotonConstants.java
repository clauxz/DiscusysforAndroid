package jp.ac.tohoku.qse.takahashi.discussions.photon.constants;


public class PhotonConstants {

	/** Name of the Photon server application to which one connect. */
	public static final String APPLICATION_NAME = "DiscussionsRT";
	/** Time to dispatch all incoming commands to the application. Dispatching will empty the queue of incoming
	 * messages and will fire the related callbacks. */
	public static final int DISPATCH_INTERVAL = 10;
	public static final String LOBBY = "discussion_lobby";
	/** To spare some overhead, we will send outgoing packets in certain intervals. */
	public static final int SEND_INTERVAL = 50;
	// public static final String SERVER_URL = ApplicationConstants.PHOTON_LOCAL ? "192.168.1.122:5055"
	// : "123.108.5.30:5055";
	/** Preffer use tcp over udp. */
	public static final boolean USE_TCP = false;

	/** A private Constructor prevents class from instantiating. */
	private PhotonConstants() {

		throw new UnsupportedOperationException("Class is prevented from instantiation");
	}
}
