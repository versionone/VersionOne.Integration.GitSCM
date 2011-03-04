package com.versionone.git.configuration;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class ProxySettings {
    @XmlElement(name = "UseProxy")
    private boolean useProxy;

    @XmlElement(name = "Path")
    private String proxyPath;

    @XmlElement(name = "UserName")
    private String proxyUserName;

    @XmlElement(name = "Password")
    private String proxyPassword;

    public boolean getUseProxy() {
        return useProxy;
    }

    @XmlTransient
    public void setUseProxy(boolean value) {
        useProxy = value;
    }

    public String getPath() {
        return proxyPath;
    }

    public String getUserName() {
        return proxyUserName;
    }

    public String getPassword() {
        return proxyPassword;
    }
}
