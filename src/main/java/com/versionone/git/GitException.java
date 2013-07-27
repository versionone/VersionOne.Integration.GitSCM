package com.versionone.git;

public class GitException extends Exception {
    private final Exception innerException;

    public GitException(Exception ex) {
        innerException = ex;
    }

    public Exception getInnerException() {
        return innerException;
    }
}
