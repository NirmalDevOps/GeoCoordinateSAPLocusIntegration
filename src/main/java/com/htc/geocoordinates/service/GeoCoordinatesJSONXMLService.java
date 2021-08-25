package com.htc.geocoordinates.service;

import org.json.JSONException;
import org.springframework.http.ResponseEntity;

public interface GeoCoordinatesJSONXMLService {

	ResponseEntity<String> responseWithHeader(String inputJSON) throws JSONException;

}
