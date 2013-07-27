package com.versionone.git;

import com.versionone.git.configuration.ChangeSet;
import com.versionone.git.configuration.GitConnection;

import java.text.SimpleDateFormat;
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
     * Calculates the changeset name based on the given changeset configuration,
     * replacing the placeholders in the name template with the values required.
     * @param changeSetConfig Changeset configuration containing the template and options
     * @return Name of the changeset to use in VersionOne
     */
    public String getName(ChangeSet changeSetConfig) {
        String name = changeSetConfig.getNameTemplate();

        // Replace {0} with the date and time according to the configured format
        SimpleDateFormat dateFormatter = new SimpleDateFormat(changeSetConfig.getNameTemplateDateFormat());
        name = name.replace("{0}", dateFormatter.format(getChangeDate()));

        // Replace {1} with the name of the author
        name = name.replace("{1}", getAuthor());

        // Replace {2} with the repository the change was made in according to the configured format
        String formattedPath = getFormattedPath(changeSetConfig);
        if (formattedPath != null)
            name = name.replace("{2}", formattedPath);

        return name;
    }

    /**
     * Applies the configured format of the repository path for use in the changeset name,
     * which is useful since any given story or defect could require work across multiple repositories.
     *
     * Examples:
     * FullPath    - git@git.yourcompany.com:libraries/library.git
     * FoldersOnly - libraries/library.git
     * NameOnly    - library.git
     *
     * @param changeSetConfig to determine which format to use
     * @return Path of the repository to be inserted into the name of the changeset
     */
    private String getFormattedPath(ChangeSet changeSetConfig) {

        String fullPath = gitConnection.getRepositoryPath();

        // Determine if path is in SSH format, e.g. git@git.yourcompany.com:repo.git
        Boolean isPathSSH = fullPath.contains("@") && fullPath.contains(":");

        String formattedPath = null;

        switch (changeSetConfig.getNameTemplateRepositoryFormat()) {
            case FullPath:
                formattedPath = fullPath;
                break;
            case FoldersOnly:
                if (isPathSSH)
                    formattedPath = fullPath.substring(fullPath.lastIndexOf(":") + 1);
                else {
                    if (fullPath.contains("//")) {
                        // Path includes protocol, e.g. http://, https:// or git://github.com/folder/repo.git format
                        formattedPath = fullPath.substring(fullPath.indexOf("/", fullPath.indexOf("//") + 2) + 1);
                    }
                    else {
                        // No protocol included in path, e.g. github.com/user/project.git
                        formattedPath = fullPath.substring(fullPath.indexOf("/", 0) + 1);
                    }
                }
                break;
            case NameOnly:
                // If path is SSH and no slashes exist start from ":", otherwise always start from the last "/"
                formattedPath = fullPath.substring(fullPath.lastIndexOf(isPathSSH && !fullPath.contains("/") ? ":" : "/") + 1);
        }
        return formattedPath;
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