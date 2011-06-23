package com.versionone.git;

import java.util.List;

public interface IDbStorage {
    List<PersistentChange> getPersistedChanges();
    void persistChange(PersistentChange change);
    boolean isChangePersisted(PersistentChange change);
    void persistLastCommit(String commitHash);
    String getLastCommit();
}
