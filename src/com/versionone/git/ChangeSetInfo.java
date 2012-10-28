package com.versionone.git;

import com.versionone.git.configuration.ChangeSet;
import com.versionone.git.configuration.GitConnection;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ChangeSetInfo {

    private final GitConnection gitConnection;
    private final String author;
    private final String message;
    private final List<String> changedFiles;
    private final Date changeDate;
    private final String revision;
    private final List<String> references;

    public ChangeSetInfo(GitConnection gitConnection, String author, String message, String revision, Date changeDate) {
        this(gitConnection, author, message, new LinkedList<String>(), revision, changeDate, new LinkedList<String>());
    }

    public ChangeSetInfo(GitConnection gitConnection, String author, String message, List<String> changedFiles, String revision, Date changeDate, List<String> references) {
        this.gitConnection = gitConnection;
        this.author = author;
        this.message = message;
        this.changedFiles = changedFiles;
        this.revision = revision;
        this.changeDate = changeDate;
        this.references = references;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public String getRevision() {
        return revision;
    }

    public List<String> getReferences() {
        return references;
    }

    /**
     * Calculates the changeset name based on the given changeset configuration
     * @param changeSetConfig Changeset configuration
     * @return Name of the changeset to use in VersionOne
     */
    public String getName(ChangeSet changeSetConfig) {
        return String.format("'%1$s' on '%2$s'", getAuthor(), getFormattedTime(getChangeDate()));
    }

    private String getFormattedTime(Date changeDate) {
        DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
        String formattedChangeDate = dateFormatter.format(changeDate);
        return String.format("%1$s UTC%2$tz", formattedChangeDate, changeDate);
    }

    /**
     * Inserts the commit ID into the link name template set for the related Git connection where this change was detected.
     * @return Link name to use in VersionOne
     */
    public String getLinkName() {
        if (gitConnection.getLink() == null || gitConnection.getLink().getNameTemplate() == null)
            return null;

        return gitConnection.getLink().getNameTemplate().replace("{0}", revision);
    }

    /**
     * Inserts the commit ID into the link URL template set for the related Git connection where this change was detected.
     * @return Link URL to use in VersionOne
     */
    public String getLinkUrl() {
        if (gitConnection.getLink() == null || gitConnection.getLink().getUrlTemplate() == null)
            return null;

        return gitConnection.getLink().getUrlTemplate().replace("{0}", revision);
    }

    public Boolean isLinkOnMenu() {
        if (gitConnection.getLink() == null)
            return false;

        return gitConnection.getLink().isOnMenu();
    }
}