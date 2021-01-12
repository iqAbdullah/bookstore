package com.bookstore.book.exception;

import lombok.Getter;

public class OutOfStock extends Exception{
	private static final long serialVersionUID = 1L;
	@Getter
	private Integer code;
	
	public OutOfStock(final Integer code,final String message){
		super(message);
		this.code = code;
	}
}
