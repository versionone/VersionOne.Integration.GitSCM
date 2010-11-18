package com.versionone.git;

import java.util.List;

public class GitService {

    private final Configuration configuration;

    private final IDbStorage storage;
    private final IGitConnector connector;

    public GitService(Configuration config) throws ConnectorException {
        configuration = Configuration.mock();
        storage = new DbStorage();
        connector = new GitConnector(configuration.getPassword(), configuration.getPassphrase(), 
                configuration.getRepositoryPath(), configuration.getWatchedBranch(), configuration.getLocalDirectory(),
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