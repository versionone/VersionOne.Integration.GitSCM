package com.versionone.git;

public interface IChangeSetWriter {
    void publish(ChangeSetInfo changeSetInfo) throws VersionOneException;
}
