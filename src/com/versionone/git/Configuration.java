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

@XmlRootElement(name="Configuration")
public class Configuration {

    @XmlElement(name = "VersionOneConnection")
    private VersionOneConnection versionOneConnection;
    @XmlElement(name = "GitSettings")
    private GitSettings gitSettings;
    @XmlElement(name = "Link")
    private Link link;

    @XmlElement(name = "BranchProcessing")
    private Boolean isProcessingThroughBranchesName;
    @XmlElement(name = "ReferenceAttribute")
    private String referenceAttribute;
    @XmlElement(name = "ReferenceExpression")
    private String referenceExpression;
    @XmlElement(name = "Timeout")
    private int timeoutMillis;
    @XmlElement(name = "AlwaysCreate")
    private boolean alwaysCreate;
    @XmlElement(name = "ChangeComment")
    private String changeComment;

    private static Configuration configuration;
    private static final Logger LOG = Logger.getLogger("GitIntegration");


    private Configuration() {
    }

    /***
     * Load configuration.
     * @return configuration
     */
    public static Configuration getInstance() {
        return getInstance("configuration.xml");
    }

    /***
     * Load specify configuration. 
     * @param fullPathToConfig full path to config file (include file)
     * @return configuration
     */
    public static Configuration getInstance(String fullPathToConfig) {
        if (configuration == null) {
            configuration = loadConfiguration(fullPathToConfig);
        }
        return configuration;
    }

    private static Configuration loadConfiguration(String fileName) {
        Configuration config = null;
        InputStream stream = null;
        try {
            final Class<Configuration> thisClass = Configuration.class;
            JAXBContext jc = JAXBContext.newInstance(thisClass);

            Unmarshaller um = jc.createUnmarshaller();
            stream = new FileInputStream(fileName);
            config = (Configuration) um.unmarshal(stream);
        } catch (JAXBException ex) {
            LOG.warn("Can't use configuration", ex);
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
        return config;
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

    public boolean isAlwaysCreate() {
        return alwaysCreate;
    }

    public String getChangeComment() {
        return changeComment;
    }

    public static class VersionOneConnection {
        @XmlElement(name = "Path")
        private String versionOnePath;
        @XmlElement(name = "UserName")
        private String versionOneUserName;
        @XmlElement(name = "Password")
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
        @XmlElement(name = "Path")
        private String repositoryPath;
        @XmlElement(name = "WatchedBranchName")
        private String watchedBranch;
        @XmlElement(name = "Password")
        private String password;
        @XmlElement(name = "SshPassphrase")
        private String passphrase;
        @XmlElement(name = "LocalDirectory")
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
        public Boolean isLinkOnMenu() {
            return linkOnMenu;
        }
    }
}
