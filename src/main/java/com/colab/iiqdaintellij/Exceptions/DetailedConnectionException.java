package com.colab.iiqdaintellij.Exceptions;

import java.util.List;


public class DetailedConnectionException extends ConnectionException {
    
    private final String msg;
    
    public DetailedConnectionException(String msg, List<String> details) {
        super(msg);
        this.msg = String.format("%s - %s", msg, details.toString());
    }
    
    public String getMessage() {
        return msg;
    }
    
    public Exception getCause() {
        return new Exception(this.msg);
    }
    
    
}
