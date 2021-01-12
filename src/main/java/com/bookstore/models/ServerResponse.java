package com.bookstore.models;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ServerResponse<T> extends ResponseEntity<T> {
	
	private static final String SUCCESS_UPPERCASE = "SUCCESS";
	private static final String MESSAGE = "message";

    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Success with data
     * 
     * @param data
     * @return HttpStatus.OK
     * */
    public ServerResponse(T data) {
        super(data, HttpStatus.OK);
    }
    
    /**
     * Success with data
     * 
     * @param    data,Httpstatus
	 * @return   ResponseEntity wrapper
	 * 
     * */
    public ServerResponse(T data, HttpStatus status) {
        super(data, status);
    }
    
    /**
     * Success response with success strings
     * 
     * @param   
	 * @return   ResponseEntity wrapper
     * */
	public static ServerResponse<JsonNode> getDefaultServiceOkResponse() {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put(MESSAGE, SUCCESS_UPPERCASE);
        return new ServerResponse<>(objectNode, HttpStatus.OK);
    }
	
	/**
	 * Response for Failure with a message
	 * 
	 * @param   message,e
	 * @return   ResponseEntity wrapper
	 */
    public static ServerResponse<JsonNode> getDefaultServiceFailedResponse(String message) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put(MESSAGE, message);
        return new ServerResponse<>(objectNode, HttpStatus.BAD_REQUEST);
    }
    
	/**
	 * Response for failure with an exception
	 * 
	 * @param    message,e
	 * @return   ResponseEntity wrapper
	 */
    public static ServerResponse<JsonNode> getServiceFailedResponseWithException(String message, Exception e) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put(MESSAGE, message+ e.getLocalizedMessage());
        return new ServerResponse<>(objectNode, HttpStatus.BAD_REQUEST);
    }

    /**
     * OK response with some message
     *
     * @param    message
     * @return   ResponseEntity wrapper
     */
    public static ServerResponse<JsonNode> getDefaultServiceOkResponse(String message) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put(MESSAGE, message);
        return new ServerResponse<>(objectNode, HttpStatus.OK);
    }

}