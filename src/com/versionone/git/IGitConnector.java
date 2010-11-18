package com.versionone.git;

import java.util.List;

public interface IGitConnector {
    void cleanupLocalDirectory();
    void initRepository() throws ConnectorException;
    List<ChangeSetInfo> getBranchCommits() throws ConnectorException;
    List<ChangeSetInfo> getMergedBranches() throws ConnectorException;
}
