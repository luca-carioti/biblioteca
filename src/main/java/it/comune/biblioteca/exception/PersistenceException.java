package it.comune.biblioteca.exception;

import it.comune.biblioteca.enums.ExceptionCodeEnum;

public class PersistenceException extends RuntimeException {

    private final ExceptionCodeEnum code;

    public PersistenceException(ExceptionCodeEnum code) {
	super(code.toString());
	this.code = code;
    }

    public PersistenceException(ExceptionCodeEnum code, Throwable cause) {
	super(code.toString(), cause);
	this.code = code;
    }

    public PersistenceException(ExceptionCodeEnum code, String message) {
	super(code.toString() + "| " + message);
	this.code = code;
    }

    public PersistenceException(ExceptionCodeEnum code, String message, Throwable cause) {
	super(code.toString() + "| " + message, cause);
	this.code = code;
    }

    public ExceptionCodeEnum getCode() {
	return code;
    }
}