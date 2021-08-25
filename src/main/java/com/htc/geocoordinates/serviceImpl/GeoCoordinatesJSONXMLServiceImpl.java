package com.htc.geocoordinates.serviceImpl;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.htc.geocoordinates.exception.GeoCoordinatesException;
import com.htc.geocoordinates.service.GeoCoordinatesJSONXMLService;
import com.htc.geocoordinates.util.GeoCoordinatesConstants;


/**
 * Represents a GeoCoordinatesJSONXMLServiceImpl Class for Service
 * 
 * @author HTC Global Service
 * @version 1.0
 * @since 25-08-2021
 * 
 */
public class GeoCoordinatesJSONXMLServiceImpl implements GeoCoordinatesJSONXMLService {

	public static final Logger LOGGER = LoggerFactory.getLogger(GeoCoordinatesJSONXMLServiceImpl.class);

	String xmlResponse = null;

	public HttpHeaders setHeaders() {
		HttpHeaders responseHeaders = new HttpHeaders();

		try {
			responseHeaders.add(System.getenv("CONNECTION_KEY"), System.getenv("CONNECTION_VALUE"));
			responseHeaders.add(System.getenv("CONTENT_TYPE_KEY"), System.getenv("CONTENT_TYPE_VALUE"));
			responseHeaders.add(System.getenv("USER_ID_KEY"), System.getenv("USER_ID_VALUE"));
			responseHeaders.add(System.getenv("PASSWORD_KEY"), System.getenv("PASSWORD_VALUE"));
			responseHeaders.add(System.getenv("ACTION_REQUEST_KEY"), System.getenv("ACTION_REQUEST_VALUE"));
			responseHeaders.add(System.getenv("RECEIVER_ID_KEY"), System.getenv("RECEIVER_ID_VALUE"));
			responseHeaders.add(System.getenv("DATA_TYPE_KEY"), System.getenv("DATA_TYPE_VALUE"));
			responseHeaders.add(System.getenv("APRF_KEY"), System.getenv("APRF_VALUE"));
			responseHeaders.add(System.getenv("SNRF_KEY"), System.getenv("SNRF_VALUE"));
		} catch (Exception e) {
			LOGGER.info("Unable to set response header");
			throw new GeoCoordinatesException("Unable to set response header");
		}
		return responseHeaders;
	}

	@Override
	public ResponseEntity<String> responseWithHeader(String inputJSON) throws JSONException {

		try {
			System.out.println("Input JSON : " + inputJSON);
			// trying to convert JSON to XML
			xmlResponse = converter(inputJSON, GeoCoordinatesConstants.ROOT);
			System.out.println("Output XML : " + xmlResponse);
			// Trying to set Headers
			HttpHeaders headers = setHeaders();
			
			return ResponseEntity.ok().headers(headers).body(xmlResponse);

		} catch (JSONException e) {
			LOGGER.info("Error in responseWithHeader method");
			throw new GeoCoordinatesException("Error in responseWithHeader method");
		}
	}

	public String converter(String json, String root) throws JSONException {
		try {
			JSONObject jsonObject = new JSONObject(json);
			return GeoCoordinatesConstants.ENCODING_UTF_8 +"<"+ root + ">" + XML.toString(jsonObject) + "</" + root + ">";
		} catch (JSONException e) {
			LOGGER.info("Unable to convert JSON TO XML");
			throw new GeoCoordinatesException("Unable to convert JSON TO XML");
		}
	}
}
