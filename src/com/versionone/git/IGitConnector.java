package com.versionone.git;

import java.util.List;

public interface IGitConnector {
    void cleanupLocalDirectory();
    void initRepository() throws GitException;
    List<ChangeSetInfo> getBranchCommits() throws GitException;
    List<ChangeSetInfo> getMergedBranches() throws GitException;
}
