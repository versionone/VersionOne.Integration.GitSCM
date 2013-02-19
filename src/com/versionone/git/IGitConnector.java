package com.versionone.git;

import java.util.List;

public interface IGitConnector {
    void initRepository() throws GitException;
    List<ChangeSetInfo> getChangeSets() throws GitException;
}
