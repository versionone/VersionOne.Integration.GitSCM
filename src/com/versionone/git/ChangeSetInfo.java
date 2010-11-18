package com.versionone.git;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ChangeSetInfo {

    private final String author;
    private final String message;
    private final List<String> changedFiles;
    private final Date changeDate;
    private final String revision;
    private final List<String> references;

    public ChangeSetInfo(String author, String message, String revision, Date changeDate) {
        this(author, message, new LinkedList<String>(), revision, changeDate, new LinkedList<String>());
    }

    public ChangeSetInfo(String author, String message, List<String> changedFiles, String revision, Date changeDate, List<String> references) {
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
}
