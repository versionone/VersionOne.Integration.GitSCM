package com.versionone.git;

// TODO impl XML configuration
public class Configuration {
    private int timeoutMillis;
    //VersionOne
    private String versionOnePath;
    private String versionOneUserName;
    private String versionOnePassword;
    //Git
    private String gitPath;
    private String gitPassword;
    private String gitPassPhrase;
    private String gitLocalDirectory;
    //Reference
    private String referenceAttribute;
    private String referenceExpression;
    //Link
    private String linkNameTemplate;
    private String linkUrlTemplate;
    //processing type
    private Boolean isProcessingThroughBranchesName;

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

    private Boolean linkOnMenu;

    public Configuration() {
        timeoutMillis = 5000;
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

    public String getGitPath() {
        return gitPath;
    }

    public String getGitPassword() {
        return gitPassword;
    }

    public String getGitPassPhrase() {
        return gitPassPhrase;
    }

    public String getGitLocalDirectory() {
        return gitLocalDirectory;
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
}
