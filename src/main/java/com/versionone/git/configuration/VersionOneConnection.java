package com.versionone.git.configuration;

import javax.xml.bind.annotation.XmlElement;

public class VersionOneConnection {
    @XmlElement(name = "Path")
    private String versionOnePath;

    @XmlElement(name = "UserName")
    private String versionOneUserName;

    @XmlElement(name = "Password")
    private String versionOnePassword;

    @XmlElement(name = "ProxySettings")
    private ProxySettings proxySettings;

    @XmlElement(name = "IntegratedAuth")
    private Boolean integratedAuth;

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

    public Boolean getIntegratedAuth() {
        return integratedAuth;
    }
}
