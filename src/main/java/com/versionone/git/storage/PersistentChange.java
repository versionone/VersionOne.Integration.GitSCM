package com.versionone.git.storage;

public class PersistentChange {
    private int id;
    private String hash;
    private String repositoryId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public static PersistentChange createNew(String changeHash, String repositoryId) {
        PersistentChange change = new PersistentChange();
        change.setId(-1);
        change.setHash(changeHash);
        change.setRepositoryId(repositoryId);

        return change;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersistentChange that = (PersistentChange) o;
        boolean result = id == that.id;
        result = result && hash.equals(that.hash);
        result = result && repositoryId.equals(that.repositoryId);

        return result;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + hash.hashCode();
        result = 31 * result + repositoryId.hashCode();
        return result;
    }
}
