package com.google.gwt.sample.stockwatcher.client;

import java.io.Serializable;

public class NotLoggedInException extends Exception implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5025515445789717163L;

	public NotLoggedInException() {
		super();
	}

	public NotLoggedInException(String message) {
		super(message);
	}
}
