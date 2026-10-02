package com.automation.api.builder;

import java.io.UncheckedIOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;

/**
 * Turns HTTP responses into Java objects.
 * <p>
 * Uses Jackson to map JSON onto the POJOs in
 * {@code com.automation.api.response}. JSON fields that have no matching
 * Java field are ignored, so the API can add fields without breaking the tests.
 */
public final class ResponseBuilder {

	// Shared, thread-safe mapper; ignores JSON fields that the POJO does not declare
	private static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private ResponseBuilder() {
	}

	/**
	 * Deserializes a JSON object into an instance of the given class.
	 *
	 * @param json            the JSON object as a string.
	 * @param responseClass   the class to map the JSON onto.
	 * @return T              the mapped object.
	 * @throws UncheckedIOException if the JSON cannot be mapped onto the class.
	 */
	public static <T> T getResponseAsObject(String json, Class<T> responseClass) {
		try {
			return MAPPER.readValue(json, responseClass);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException("Response could not be read as " + responseClass.getSimpleName(), e);
		}
	}

	/**
	 * Deserializes a response whose body is a JSON array into a list of objects.
	 *
	 * @param response        the response of the HTTP request; its body must be a JSON array.
	 * @param responseClass   the class to map each array element onto.
	 * @return List&lt;T&gt;  one mapped object per element of the array.
	 * @throws UncheckedIOException if the body is not a JSON array of that class.
	 */
	public static <T> List<T> getResponseAsObjectList(Response response, Class<T> responseClass) {
		try {
			return MAPPER.readValue(response.asString(),
					MAPPER.getTypeFactory().constructCollectionType(List.class, responseClass));
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException("Response could not be read as a list of " + responseClass.getSimpleName(), e);
		}
	}
}
