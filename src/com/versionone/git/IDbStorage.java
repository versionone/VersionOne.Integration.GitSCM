package com.versionone.git;

import java.util.Collection;

public interface IDbStorage {
    Collection<PersistentChange> getPersistedChanges();
    void persistChange(PersistentChange change);
    boolean isChangePersisted(PersistentChange change);
}
