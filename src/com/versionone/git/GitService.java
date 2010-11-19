package com.versionone.git;

import org.apache.log4j.Logger;

import java.util.List;

public class GitService {

    private final Configuration configuration;

    private final IDbStorage storage;
    private final IGitConnector connector;

    private final Logger LOG = Logger.getLogger("GitIntegration");

    public GitService(Configuration config) throws ConnectorException {
        configuration = config;
        storage = new DbStorage();
        Configuration.GitSettings gitSettings = configuration.getGitSettings();
        connector = new GitConnector(gitSettings.getPassword(), gitSettings.getPassphrase(),
                gitSettings.getRepositoryPath(), gitSettings.getWatchedBranch(), gitSettings.getLocalDirectory(),
                configuration.getReferenceExpression());
        connector.cleanupLocalDirectory();
        connector.initRepository();
    }

    public void onInterval() throws ConnectorException {
        List<ChangeSetInfo> changes;

        if(configuration.getProcessingThroughBranchesName()) {
            throw new UnsupportedOperationException("not yet done, there's a story for it");
        } else {
            changes = connector.getBranchCommits();
        }

        for(ChangeSetInfo change : changes) {
            PersistentChange persistentChange = PersistentChange.createNew(change.getRevision());

            if(!storage.isChangePersisted(persistentChange)) {
                // TODO publish to VersionOne
                storage.persistChange(persistentChange);
                LOG.info("Change published to VersionOne server: " + change.getRevision());
            } else {
                LOG.info("Change already processed: " + change.getRevision());
            }
        }
    }
}