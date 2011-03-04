package com.versionone.git.configuration;

import javax.xml.bind.annotation.XmlElement;

public class VersionOneSettings {
    @XmlElement(name = "Path")
    private String versionOnePath;

    @XmlElement(name = "UserName")
    private String versionOneUserName;

    @XmlElement(name = "Password")
    private String versionOnePassword;

    @XmlElement(name = "ProxySettings")
    private ProxySettings proxySettings;

    public String getPath() {
        return versionOnePath;
    }

    public String getUserName() {
        return versionOneUserName;
    }

    public String getPassword() {
        return versionOnePassword;
    }

    public ProxySettings getProxySettings() {
        return proxySettings;
    }
}
