package com.htc.geocoordinates.main;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htc.geocoordinates.exception.GeoCoordinatesException;
import com.htc.geocoordinates.serviceImpl.GeoCoordinatesJSONXMLServiceImpl;
import com.htc.geocoordinates.util.GeoCoordinatesConstants;

/**
 * Represents a Class for GeoCoordinates to hit the Receiver API and get the
 * Response
 * 
 * @author HTC Global Service
 * @version 1.0
 * @since 25-08-2021
 * 
 */

public class GeoCoordinates implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private final ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private static final Logger LOGGER = LoggerFactory.getLogger(GeoCoordinates.class);

	GeoCoordinatesJSONXMLServiceImpl jsonxmlService = new GeoCoordinatesJSONXMLServiceImpl();

	/**
	 * This method is used to handle initial request from SAP
	 * 
	 * @param input, context
	 * @return
	 */
	@Override
	public synchronized APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent geoCodeinputJSON,
			Context context) {

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		LambdaLogger logger = context.getLogger();

		String geoCodeLocation = null;

		try {
			geoCodeLocation = jsonxmlService.converter(geoCodeinputJSON.getBody().toString(),
					GeoCoordinatesConstants.ROOT);
			logger.log("Successfully invoked the converted and converted the orderhive JSON to Locus XML");
		} catch (JSONException e) {
			LOGGER.error("Error occured while converting from JSON TO XML: " + e);
		}

		// setClientAuthorizationKey(requestForReceiver);

		// After successfully converted the JSON to XML, Trying to call the OpenText
		// API'S to process.
		if (!geoCodeLocation.isEmpty()) {
		
			ResponseEntity<String> receiverResponse = getResponseFromReceiverAPI(geoCodeLocation,logger);
		
			System.out.println("Receiver Response is :\t" + receiverResponse);

			if (receiverResponse.getStatusCode().value() == (HttpStatus.OK.value())) {
				response = apiGetwayProxyResponse(receiverResponse, GeoCoordinatesConstants.SUCCESS,logger);
			} else {
				response = apiGetwayProxyResponse(receiverResponse, GeoCoordinatesConstants.FAILURE,logger);
			}
		} else {
			response.setStatusCode(HttpStatus.CONFLICT.value());
			response.setBody(GeoCoordinatesConstants.ERROR_IN_PROCESSING);
		}
		System.out.println("Final Response  :" + response);
		return response;
	}

	private ResponseEntity<String> getResponseFromReceiverAPI(String requestXML,LambdaLogger logger) {

		ResponseEntity<String> receiverResponse = null;

		try {
			String endPointUrl = buildUrl(logger);

			// Getting Headers
			HttpHeaders headers = jsonxmlService.setHeaders();
			RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
			logger.log("Rest Template Obj initialized");
			HttpEntity<String> entity = new HttpEntity<String>(requestXML, headers);
			logger.log("HttpEntity Body As aString::" + entity.getBody().toString());
			logger.log("Going to call Open Text API::" + endPointUrl);
			
			// restTemplate.getInterceptors().add(new
			// BasicAuthorizationInterceptor(GeoCoordinatesConstants.CLIENT_ID,
			// GeoCoordinatesConstants.AUTHORIZATION_KEY));

			// send request and parse result
			if(endPointUrl!=null)
			receiverResponse = restTemplate.exchange(endPointUrl, HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException e) {
			//System.out.println("inside HttpClientErrorException");
			receiverResponse = new ResponseEntity<String>(e.getStatusCode());

			LOGGER.error(GeoCoordinatesConstants.ERROR_MESSAGE + e.getMessage());
		} catch (RestClientException e) {
			//System.out.println("inside HttpClientErrorException");
			receiverResponse = new ResponseEntity<String>(((HttpStatusCodeException) e).getStatusCode());
			LOGGER.error(GeoCoordinatesConstants.ERROR_MESSAGE + e.getMessage());
		}

		catch (Exception e) {
			//System.out.println("inside Exception");
			receiverResponse = new ResponseEntity<String>(((HttpStatusCodeException) e).getStatusCode());
			LOGGER.error(GeoCoordinatesConstants.ERROR_MESSAGE + e.getMessage());
		}
		return receiverResponse;
	}

	/**
	 * This private method is used to build the URL to call the receiver API.
	 */
	private String buildUrl(LambdaLogger logger) {
		String receiverEndPointURL = null;
		StringBuilder endPointURLBuilder = null;
		try {
			endPointURLBuilder = new StringBuilder(System.getenv("RECEIVER_END_POINT_URL"));
		} catch (Exception e) {
			logger.log("Unable to find RECEIVER_END_POINT_URL from environment variable or incorrect EndPointURL.");
			throw new GeoCoordinatesException(
					"Unable to find RECEIVER_END_POINT_URL from environment variable or incorrect EndPointURL.");

		}
		if (endPointURLBuilder != null)
			receiverEndPointURL = endPointURLBuilder.toString();
		return receiverEndPointURL;
	}

	/**
	 * This private method is used to set the connection and reading time out.
	 */
	private SimpleClientHttpRequestFactory getClientHttpRequestFactory() {
		SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
		// Connect timeout
		clientHttpRequestFactory.setConnectTimeout(GeoCoordinatesConstants.CONNECTION_TIME_OUT);
		// Read timeout
		clientHttpRequestFactory.setReadTimeout(GeoCoordinatesConstants.READING_TIME_OUT);
		return clientHttpRequestFactory;
	}

	/**
	 * This private method is used to build the URL to call the receiver API.
	 */
	private APIGatewayProxyResponseEvent apiGetwayProxyResponse(ResponseEntity<String> receiverResponse,
			String status,LambdaLogger logger) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

		if (status.equalsIgnoreCase(GeoCoordinatesConstants.SUCCESS)) {
			response.setStatusCode(HttpStatus.OK.value());
			logger.log(GeoCoordinatesConstants.SUCCESS_CODE + HttpStatus.OK.value());

		} else {
			response.setStatusCode(receiverResponse.getStatusCodeValue());
			LOGGER.error(GeoCoordinatesConstants.ERROR_STATUS_CODE + receiverResponse.getStatusCodeValue());

		}
		response.setBody(receiverResponse.toString());
		return response;
	}

	/*
	 * private void setClientAuthorizationKey(String requestForReceiver) { String
	 * secretJsonData = getSecret(); if (!secretJsonData.isEmpty()) { JSONObject
	 * jsonObj = new JSONObject(secretJsonData);
	 * requestForReceiver.setClientId(jsonObj.getString("CLIENT_ID"));
	 * requestForReceiver.setAuthorizationKey(jsonObj.getString("BASIC_AUTH_KEY"));
	 * 
	 * } else { LOGGER.info("Secret key is not available"); } }
	 */

	/*
	 * private String getSecret() { BasicAWSCredentials creds = new
	 * BasicAWSCredentials(System.getenv("ACCESS_KEY"),
	 * System.getenv("SECRET_KEY"));
	 * 
	 * AWSSecretsManager client =
	 * AWSSecretsManagerClientBuilder.standard().withRegion(System.getenv("REGION"))
	 * .withCredentials(new AWSStaticCredentialsProvider(creds)).build();
	 * 
	 * String secret = "", decodedBinarySecret = ""; GetSecretValueRequest
	 * getSecretValueRequest = new GetSecretValueRequest()
	 * .withSecretId(System.getenv("SECRET_NAME")); GetSecretValueResult
	 * getSecretValueResult = null;
	 * 
	 * try { getSecretValueResult = client.getSecretValue(getSecretValueRequest); }
	 * catch (DecryptionFailureException e) { throw e; } catch
	 * (InternalServiceErrorException e) { throw e; } catch
	 * (InvalidParameterException e) { throw e; } catch (InvalidRequestException e)
	 * { throw e; } catch (ResourceNotFoundException e) { throw e; } if
	 * (getSecretValueResult.getSecretString() != null) { secret =
	 * getSecretValueResult.getSecretString(); } else { decodedBinarySecret = new
	 * String(
	 * Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
	 * }
	 * 
	 * return secret; }
	 */
}
