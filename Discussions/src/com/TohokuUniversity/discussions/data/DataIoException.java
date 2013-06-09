package com.TohokuUniversity.discussions.data;

/** Runtime unchecked exception indicates problems with data CRUD operation raised in content provider or
 * remote open data protocol layer. */
public class DataIoException extends RuntimeException {

	private static final long serialVersionUID = -983728235361740002L;

	public DataIoException() {

		super();
	}

	public DataIoException(final String detailMessage) {

		super(detailMessage);
	}

	public DataIoException(final String detailMessage, final Throwable throwable) {

		super(detailMessage, throwable);
	}

	public DataIoException(final Throwable throwable) {

		super(throwable);
	}
}
