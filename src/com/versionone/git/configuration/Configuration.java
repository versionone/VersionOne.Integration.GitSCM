package com.versionone.git.configuration;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@XmlRootElement(name="Configuration")
public class Configuration {
    @XmlElement(name = "VersionOneConnection")
    private VersionOneSettings versionOneSettings;

    @XmlElement(name = "Instance")
    @XmlElementWrapper(name = "GitSettings")
    private List<GitSettings> gitSettings;

    @XmlElement(name = "LocalDirectory")
    private String localDirectory;

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

    private Configuration() { }

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
        LOG.info("Loading configuration...");
        Configuration config = null;
        InputStream stream = null;

        try {
            final Class<Configuration> thisClass = Configuration.class;
            JAXBContext jc = JAXBContext.newInstance(thisClass);

            Unmarshaller um = jc.createUnmarshaller();
            stream = new FileInputStream(fileName);
            config = (Configuration) um.unmarshal(stream);
            LOG.info("Configuration loaded successfully");
        } catch (JAXBException ex) {
            LOG.fatal("Couldn't read configuration file, please check for invalid XML", ex);
            System.exit(-1);
        } catch (FileNotFoundException ex) {
            LOG.fatal(String.format("Couldn't find configuration file at the specified location (%s)", fileName), ex);
            System.exit(-1);
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

    public List<GitSettings> getGitSettings() {
        return gitSettings;
    }

    public String getReferenceAttribute() {
        return referenceAttribute;
    }

    public String getReferenceExpression() {
        return referenceExpression;
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

    public String getLocalDirectory() {
        return localDirectory;
    }
}
