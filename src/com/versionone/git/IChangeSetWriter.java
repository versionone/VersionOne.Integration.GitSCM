package com.versionone.git;

import com.versionone.apiclient.V1Exception;

public interface IChangeSetWriter {
    void publish(ChangeSetInfo changeSetInfo) throws VersionOneException;
    void connect() throws VersionOneException;
}
