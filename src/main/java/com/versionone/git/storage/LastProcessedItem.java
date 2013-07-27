package com.versionone.git.storage;

import java.io.Serializable;

public class LastProcessedItem implements Serializable {
    private String value;
    private String repositoryId;
    private String branchRef;

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getBranchRef() {
        return branchRef;
    }

    public void setBranchRef(String branchRef) {
        this.branchRef = branchRef;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
