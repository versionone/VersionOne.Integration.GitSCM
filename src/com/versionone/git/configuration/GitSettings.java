package com.versionone.git.configuration;

import javax.xml.bind.annotation.XmlElement;

public class GitSettings {
    @XmlElement(name = "Path")
    private String repositoryPath;
    @XmlElement(name = "WatchedBranchName")
    private String watchedBranch;
    @XmlElement(name = "Password")
    private String password;
    @XmlElement(name = "SshPassphrase")
    private String passphrase;
    @XmlElement(name = "LocalDirectory")
    private String localDirectory;
    @XmlElement(name = "UseBranchName")
    private Boolean useBranchName;

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
    public String getWatchedBranch() {
        return watchedBranch;
    }

    public Boolean getUseBranchName() {
        return useBranchName;
    }
}
