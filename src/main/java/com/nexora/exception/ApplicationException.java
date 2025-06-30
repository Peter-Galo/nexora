package com.nexora.exception;

public class ApplicationException extends RuntimeException {
    
    private final String code;
    
    public ApplicationException(String message) {
        super(message);
        this.code = "GENERAL_ERROR";
    }
    
    public ApplicationException(String message, String code) {
        super(message);
        this.code = code;
    }
    
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
        this.code = "GENERAL_ERROR";
    }
    
    public ApplicationException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}