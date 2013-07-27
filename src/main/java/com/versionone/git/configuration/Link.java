package com.versionone.git.configuration;

import javax.xml.bind.annotation.XmlElement;

public class Link {
    @XmlElement(name = "NameTemplate")
    private String nameTemplate;
    @XmlElement(name = "UrlTemplate")
    private String urlTemplate;
    @XmlElement(name = "OnMenu")
    private Boolean onMenu = true;

    public String getNameTemplate() {
        return nameTemplate;
    }
    public String getUrlTemplate() {
        return urlTemplate;
    }
    public Boolean isOnMenu() {
        return onMenu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link that = (Link)o;

        if (nameTemplate != null ? !nameTemplate.equals(that.nameTemplate) : that.nameTemplate != null) return false;
        if (urlTemplate != null ? !urlTemplate.equals(that.urlTemplate) : that.urlTemplate != null) return false;
        if (!onMenu.equals(that.onMenu)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = nameTemplate.hashCode();
        result = 31 * result + (urlTemplate != null ? urlTemplate.hashCode() : 0);
        result = 31 * result + onMenu.hashCode();
        return result;
    }
}
