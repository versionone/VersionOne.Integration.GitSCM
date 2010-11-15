package com.versionone.git;

import java.util.Collection;

public class DbStorage implements IDbStorage {
    public Collection<PersistentChange> getPersistedChanges() {
        // TODO
        return null;
    }

    public void persistChange(PersistentChange change) {
        // TODO
    }

    public boolean isChangePersisted(PersistentChange change) {
        // TODO
        return false;
    }
}
