package com.versionone.git;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@XmlRootElement(name="configuration")
public class Configuration {

    @XmlElement(name = "versiononeconnection")
    private VersionOneConnection versionOneConnection;
    @XmlElement(name = "gitsettings")
    private GitSettings gitSettings;
    @XmlElement(name = "link")
    private Link link;

    @XmlElement(name = "branchprocessing")
    private Boolean isProcessingThroughBranchesName;
    @XmlElement(name = "referenceattribute")
    private String referenceAttribute;
    @XmlElement(name = "referenceexpression")
    private String referenceExpression;
    @XmlElement(name = "timeout")
    private int timeoutMillis;

    private static Configuration configuration;
    private static final Logger LOG = Logger.getLogger("GitIntegration");


    public Configuration() {
        timeoutMillis = 5000;
    }

    /*
    public static Configuration mock() {
        Configuration configuration = new Configuration();
        configuration.repositoryPath = "git@github.com:versionone/ExigenTest.git";
        configuration.referenceExpression = "[a-zA-Z]+";
        configuration.passphrase = "v10000";
        configuration.watchedBranch = "master";
        configuration.localDirectory = "c:/temp/checkout_v1";
        return configuration;
    }*/

    public static Configuration getInstance() {
        if (configuration == null) {
            InputStream stream = null;
            try {
                final Class<Configuration> thisClass = Configuration.class;
                JAXBContext jc = JAXBContext.newInstance(thisClass);

                Unmarshaller um = jc.createUnmarshaller();
                //stream = thisClass.getResourceAsStream(thisClass.getSimpleName() + ".xml");
                //stream = new FileInputStream("e:\\WORK_VERSION_ONE\\V1Integration\\Java\\Git\\configuration.xml");
                stream = new FileInputStream("configuration.xml");
                configuration = (Configuration) um.unmarshal(stream);
            } catch (JAXBException ex) {
                LOG.warn("Can't use configuration", ex);
                configuration = new Configuration();
            } catch (FileNotFoundException ex) {
                LOG.warn("Can't load configuration.xml", ex);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        // Do nothing
                    }
                }
            }
        }
        return configuration;
    }

    public VersionOneConnection getVersionOneConnection() {
        return versionOneConnection;
    }

    public GitSettings getGitSettings() {
        return gitSettings;
    }

    public Link getLink() {
        return link;
    }

    public String getReferenceAttribute() {
        return referenceAttribute;
    }

    public String getReferenceExpression() {
        return referenceExpression;
    }

    public Boolean getProcessingThroughBranchesName() {
        return isProcessingThroughBranchesName;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    public static class VersionOneConnection {
        @XmlElement(name = "path")
        private String versionOnePath;
        @XmlElement(name = "username")
        private String versionOneUserName;
        @XmlElement(name = "password")
        private String versionOnePassword;

        public String getPath() {
            return versionOnePath;
        }
        public String getUserName() {
            return versionOneUserName;
        }
        public String getPassword() {
            return versionOnePassword;
        }
    }

    public static class GitSettings {
        @XmlElement(name = "path")
        private String repositoryPath;
        @XmlElement(name = "watchedbranchname")
        private String watchedBranch;
        @XmlElement(name = "password")
        private String password;
        @XmlElement(name = "sshpassphrase")
        private String passphrase;
        @XmlElement(name = "localdirectory")
        private String localDirectory;       

        public String getRepositoryPath() {
            return repositoryPath;
        }
        public String getPassword() {
            return password;
        }
        public String getPassphrase() {
            return passphrase;
        }
        public String getLocalDirectory() {
            return localDirectory;
        }
        public String getWatchedBranch() {
            return watchedBranch;
        }
    }

    public static class Link {
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
        public Boolean getLinkOnMenu() {
            return linkOnMenu;
        }
    }
}
