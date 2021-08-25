/**
 * 
 */
package com.htc.geocoordinates.exception;

/**
 * Represents a Class for GeoCoordinatesException to handle the exception
 * message
 * 
 * @author HTC Global Service
 * @version 1.0
 * @since 25-08-2021
 * 
 */
public class GeoCoordinatesException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String  message;

	public GeoCoordinatesException() {
	}

	/**
	 * @param message
	 */
	public GeoCoordinatesException(String message) {
		super();
		this.message = message;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

}
