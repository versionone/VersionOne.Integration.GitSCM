package com.versionone.git.storage;

import java.util.List;

public interface IDbStorage {
    List<PersistentChange> getPersistedChanges();
    void persistChange(PersistentChange change);
    boolean isChangePersisted(PersistentChange change);
    void persistLastCommit(String commitHash, String repositoryId, String branchRef);
    String getLastCommit(String repositoryId, String branchRef);
}
