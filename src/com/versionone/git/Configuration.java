package com.versionone.git;

// TODO impl XML configuration
public class Configuration {
    private int timeoutMillis;

    public Configuration() {
        timeoutMillis = 5000;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }
}
