package com.versionone.git;

import com.versionone.git.configuration.Link;

public interface IChangeSetWriter {
    void publish(ChangeSetInfo changeSetInfo) throws VersionOneException;
}
