package com.versionone.git.configuration;

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
    private VersionOneSettings versionOneSettings;
    @XmlElement(name = "GitSettings")
    private GitSettings gitSettings;
    @XmlElement(name = "Link")
    private Link link;

    @XmlElement(name = "UseBranchName")
    private Boolean useBranchName;
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

    public static void reset() {
        configuration = null;
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

    public VersionOneSettings getVersionOneSettings() {
        return versionOneSettings;
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

    public Boolean getUseBranchName() {
        return useBranchName;
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
}
