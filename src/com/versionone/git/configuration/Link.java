package com.versionone.git.configuration;

import javax.xml.bind.annotation.XmlElement;

public class Link {
    @XmlElement(name = "Name")
    private String linkNameTemplate;
    @XmlElement(name = "URL")
    private String linkUrlTemplate;
    @XmlElement(name = "OnMenu")
    private Boolean linkOnMenu;

    public String getLinkNameTemplate() {
        return linkNameTemplate;
    }
    public String getLinkUrlTemplate() {
        return linkUrlTemplate;
    }
    public Boolean isLinkOnMenu() {
        return linkOnMenu;
    }
}
