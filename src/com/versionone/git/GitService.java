package com.versionone.git;

import java.util.List;

public class GitService {

    private final Configuration configuration;

    private final IDbStorage storage;
    private final IGitConnector connector;

    public GitService(Configuration config) throws ConnectorException {
        configuration = config;
        storage = new DbStorage();
        Configuration.GitSettings gitSettings = configuration.getGitSettings();
        connector = new GitConnector(gitSettings.getPassword(), gitSettings.getPassphrase(),
                gitSettings.getRepositoryPath(), gitSettings.getWatchedBranch(), gitSettings.getLocalDirectory(),
                configuration.getReferenceExpression(), storage);
        connector.cleanupLocalDirectory();
        connector.initRepository();
    }

    public void onInterval() throws ConnectorException {
        List<ChangeSetInfo> changes = connector.getBranchCommits();

        for(ChangeSetInfo item : changes) {
            System.out.println(item.getRevision());
        }
    }
}