package com.versionone.git;

public class ConnectorException extends Exception {

    private final Exception innerException;

    public ConnectorException(Exception ex) {
        innerException = ex;
    }

    public Exception getInnerException() {
        return innerException;
    }
}
