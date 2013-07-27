package com.versionone.git.configuration;

import javax.xml.bind.annotation.XmlElement;

public class ChangeSet {

    @XmlElement(name = "AlwaysCreate")
    private boolean alwaysCreate;
    @XmlElement(name = "NameTemplate")
    private String nameTemplate;
    @XmlElement(name = "NameTemplateDateFormat")
    private String nameTemplateDateFormat;
    @XmlElement(name = "NameTemplateRepositoryFormat")
    private RepositoryFormatEnum nameTemplateRepositoryFormat;
    @XmlElement(name = "ChangeComment")
    private String changeComment;
    @XmlElement(name = "ReferenceAttribute")
    private String referenceAttribute;
    @XmlElement(name = "ReferenceExpression")
    private String referenceExpression;

    /**
     * Determines the format of the repository path to use if
     * the user chooses to include it in the changeset name.
     */
    public enum RepositoryFormatEnum {
        /**
         * This option will output the full URL as it is in the connection configuration,
         * for example "github.com/account/repo.git" or "git@git.yourcompany.com:folder/repo.git"
         */
        FullPath,
        /**
         * This option will exclude the server address, leaving just the folders and repo itself,
         * for example "account/repo.git" or "folder/repo.git"
         */
        FoldersOnly,
        /**
         * This option will exclude everything but the name of the repo itself,
         * for example "repo.git"
         */
        NameOnly
    }

    public boolean isAlwaysCreate() {
        return alwaysCreate;
    }

    public String getNameTemplate() {
        return nameTemplate;
    }

    public String getNameTemplateDateFormat() {
        return nameTemplateDateFormat;
    }

    public RepositoryFormatEnum getNameTemplateRepositoryFormat() {
        return nameTemplateRepositoryFormat;
    }

    public String getReferenceAttribute() {
        return referenceAttribute;
    }

    public String getReferenceExpression() {
        return referenceExpression;
    }

    public String getChangeComment() {
        return changeComment;
    }
}
