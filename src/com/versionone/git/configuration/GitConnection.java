package com.versionone.git.configuration;

import javax.xml.bind.annotation.XmlElement;

public class GitConnection {
    @XmlElement(name = "Path")
    private String repositoryPath;
    @XmlElement(name = "WatchedBranchName")
    private String watchedBranch;
    @XmlElement(name = "Password")
    private String password;
    @XmlElement(name = "SshPassphrase")
    private String passphrase;
    @XmlElement(name = "UseBranchName")
    private Boolean useBranchName = false;
    @XmlElement(name = "Link")
    private Link link;


    public String getRepositoryPath() {
        return repositoryPath;
    }

    public String getPassword() {
        return password;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public String getWatchedBranch() {
        return watchedBranch;
    }

    public Boolean getUseBranchName() {
        return useBranchName;
    }

    public Link getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GitConnection that = (GitConnection) o;

        if (passphrase != null ? !passphrase.equals(that.passphrase) : that.passphrase != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (!repositoryPath.equals(that.repositoryPath)) return false;
        if (!useBranchName.equals(that.useBranchName)) return false;
        if (watchedBranch != null ? !watchedBranch.equals(that.watchedBranch) : that.watchedBranch != null) return false;
        if (link != null ? !link.equals(that.link) : that.link != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = repositoryPath.hashCode();
        result = 31 * result + (watchedBranch != null ? watchedBranch.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (passphrase != null ? passphrase.hashCode() : 0);
        result = 31 * result + useBranchName.hashCode();
        result = 31 * result + link.hashCode();
        return result;
    }
}
