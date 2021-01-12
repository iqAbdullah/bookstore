package com.bookstore.book.exception;

import lombok.Getter;


public class BookDoesNotExist extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Getter
	private Integer code;
	
	public BookDoesNotExist(final Integer code,final String message){
		super(message);
		this.code = code;
	}

	public BookDoesNotExist() {
		super();
		// TODO Auto-generated constructor stub
	}

}
