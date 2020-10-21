package de.adesso.example.framework.exception;


public class UnknownMethodException extends RuntimeException {
	private static final long serialVersionUID = 580469134829402021L;

	public UnknownMethodException(String message) {
		super(message);
	}

}