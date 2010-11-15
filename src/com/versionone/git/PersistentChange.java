package com.versionone.git;

public class PersistentChange {
    private int id;
    private String hash;

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

    public static PersistentChange createNew(String changeHash) {
        PersistentChange change = new PersistentChange();
        change.setId(-1);
        change.setHash(changeHash);

        return change;
    }

    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof PersistentChange)) {
            return false;
        }

        PersistentChange other = (PersistentChange) obj;

        if(getHash() == null) {
           return other.getHash() == null;
        } else {
            return getHash().equals(other.getHash());
        }
    }

    public int hashCode() {
        return super.hashCode();
    }
}
