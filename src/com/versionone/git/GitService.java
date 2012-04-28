package com.versionone.git;

import com.versionone.git.storage.IDbStorage;
import com.versionone.git.storage.PersistentChange;
import org.apache.log4j.Logger;

import java.util.Collection;

public class GitService {
    private final IDbStorage storage;
    private final IGitConnector gitConnector;
    private final IChangeSetWriter v1ChangeSetWriter;
    private final String repositoryId;

    private final Logger LOG = Logger.getLogger("GitIntegration");

    public GitService(IDbStorage storage, IGitConnector gitConnector, IChangeSetWriter v1ChangeSetWriter, String repositoryId) {
        this.storage = storage;
        this.gitConnector = gitConnector;
        this.v1ChangeSetWriter = v1ChangeSetWriter;
        this.repositoryId = repositoryId;
    }

    public void initialize() throws GitException {
        gitConnector.initRepository();
        LOG.info("Connection to Git server established.");
    }

    public void onInterval() throws GitException, VersionOneException {
        Collection<ChangeSetInfo> changes = gitConnector.getCommits();
        LOG.debug("Found " + changes.size() + " changes to process.");

        for(ChangeSetInfo change : changes) {
            PersistentChange persistentChange = PersistentChange.createNew(change.getRevision(), repositoryId);

            if(!storage.isChangePersisted(persistentChange)) {
                v1ChangeSetWriter.publish(change);
                storage.persistChange(persistentChange);
                LOG.debug("Change published to VersionOne server: " + change.getRevision());
            } else {
                LOG.debug("Change already processed: " + change.getRevision());
            }
        }
    }
}