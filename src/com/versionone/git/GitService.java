package com.versionone.git;

import com.versionone.apiclient.V1Exception;
import org.apache.log4j.Logger;

import java.util.Collection;

public class GitService {

    private final Configuration configuration;

    private final IDbStorage storage;
    private final IGitConnector gitConnector;
    private final IChangeSetWriter v1ChangeSetWriter;

    private final Logger LOG = Logger.getLogger("GitIntegration");

    public GitService(Configuration config, IDbStorage storage, IGitConnector gitConnector, IChangeSetWriter v1ChangeSetWriter) {
        configuration = config;

        this.storage = storage;
        this.gitConnector = gitConnector;
        this.v1ChangeSetWriter = v1ChangeSetWriter;
    }

    public void initialize() throws GitException, VersionOneException {
        gitConnector.cleanupLocalDirectory();
        gitConnector.initRepository();
    }

    public void onInterval() throws GitException, VersionOneException {
        Collection<ChangeSetInfo> changes;

        if(configuration.getProcessingThroughBranchesName()) {
            changes = gitConnector.getMergedBranches();
        } else {
            changes = gitConnector.getBranchCommits();
        }

        LOG.info("Found " + changes.size() + " changes to process.");

        for(ChangeSetInfo change : changes) {
            PersistentChange persistentChange = PersistentChange.createNew(change.getRevision());

            if(!storage.isChangePersisted(persistentChange)) {
                v1ChangeSetWriter.publish(change);
                storage.persistChange(persistentChange);
                LOG.info("Change published to VersionOne server: " + change.getRevision());
            } else {
                LOG.info("Change already processed: " + change.getRevision());
            }
        }
    }
}