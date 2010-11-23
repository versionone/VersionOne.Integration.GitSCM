package com.versionone.git;

import org.apache.log4j.Logger;

import java.util.Collection;

public class GitService {

    private final Configuration configuration;

    private final IDbStorage storage;
    private final IGitConnector connector;

    private final Logger LOG = Logger.getLogger("GitIntegration");

    public GitService(Configuration config, IDbStorage storage, IGitConnector connector) {
        configuration = config;

        this.storage = storage;
        this.connector = connector;
    }

    public void initialize() throws GitException {
        connector.cleanupLocalDirectory();
        connector.initRepository();
    }

    public void onInterval() throws GitException {
        Collection<ChangeSetInfo> changes;

        if(configuration.getProcessingThroughBranchesName()) {
            changes = connector.getMergedBranches();
        } else {
            changes = connector.getBranchCommits();
        }

        LOG.info("Found " + changes.size() + " changes to process.");

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