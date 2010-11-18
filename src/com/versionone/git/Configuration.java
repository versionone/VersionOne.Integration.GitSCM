package com.versionone.git;

// TODO impl XML configuration
public class Configuration {
    private int timeoutMillis;

    private String versionOnePath;
    private String versionOneUserName;
    private String versionOnePassword;

    private String repositoryPath;
    private String watchedBranch;
    private String password;
    private String passphrase;
    private String localDirectory;

    private String referenceAttribute;
    private String referenceExpression;

    private String linkNameTemplate;
    private String linkUrlTemplate;

    private Boolean isProcessingThroughBranchesName;

    private Boolean linkOnMenu;

    public Configuration() {
        timeoutMillis = 5000;
    }

    public Boolean getProcessingThroughBranchesName() {
        return isProcessingThroughBranchesName;
    }

    public String getLinkNameTemplate() {
        return linkNameTemplate;
    }

    public String getLinkUrlTemplate() {
        return linkUrlTemplate;
    }

    public Boolean getLinkOnMenu() {
        return linkOnMenu;
    }

    public String getVersionOnePath() {
        return versionOnePath;
    }

    public String getVersionOneUserName() {
        return versionOneUserName;
    }

    public String getVersionOnePassword() {
        return versionOnePassword;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public String getPassword() {
        return password;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public String getLocalDirectory() {
        return localDirectory;
    }

    public String getReferenceAttribute() {
        return referenceAttribute;
    }

    public String getReferenceExpression() {
        return referenceExpression;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public String getWatchedBranch() {
        return watchedBranch;
    }
}
