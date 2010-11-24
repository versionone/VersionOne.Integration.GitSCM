package com.versionone.git;

public class VersionOneException extends Exception {
    private final Exception innerException;

    VersionOneException(String msg, Exception ex) {
        this.innerException = ex;
    }

    public Exception getInnerException() {
        return innerException;
    }
}
