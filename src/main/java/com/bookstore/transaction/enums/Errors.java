package com.bookstore.transaction.enums;

import lombok.Getter;

@Getter
public enum Errors {
	
	BOOK_NOT_PRESENT(1001,"Book is not Registered for ISBN nos : {FILL}"),
	BOOK_OUT_OF_STOCK(1002,"here are the maximum quant of abook availables : {FILL} ");

	private Integer code;
	private String message;

	private Errors(final Integer code, final String message) {
		this.code = code;
		this.message = message;
	}

}
