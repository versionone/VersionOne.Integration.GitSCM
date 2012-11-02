package com.versionone.git;

import com.versionone.git.storage.IDbStorage;
import com.versionone.git.storage.PersistentChange;
import org.apache.log4j.Logger;
import org.eclipse.jgit.util.StringUtils;

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

        if(!gitConnector.watchedBranchExists()) {
            LOG.warn("Watched branch '" + gitConnector.getWatchedBranchName() + "' does not exist.");
        } else {
            LOG.debug("Watched branch '" + gitConnector.getWatchedBranchName() + "' exists.");
        }

        LOG.info("Connection to Git repository established successfully");
    }

    public void onInterval() throws GitException, VersionOneException {
        Collection<ChangeSetInfo> changes = gitConnector.getChangeSets();
        LOG.debug("Found " + changes.size() + " changes to process");

        for(ChangeSetInfo change : changes) {
            PersistentChange persistentChange = PersistentChange.createNew(change.getRevision(), repositoryId);

            if(!storage.isChangePersisted(persistentChange)) {
                v1ChangeSetWriter.publish(change);
                storage.persistChange(persistentChange);
            } else {
                LOG.warn(String.format("Ignoring changeset %1$s by %2$s on %3$s to %4$s because it has already been processed before",
                        change.getRevision(),
                        change.getAuthor(),
                        change.getChangeDate(),
                        StringUtils.join(change.getReferences(), ", ")));
            }
        }
    }
}