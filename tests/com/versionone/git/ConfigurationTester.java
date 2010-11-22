package com.versionone.git;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Tester for configuration
 */
public class ConfigurationTester {

    @Test
    public void configTest() throws IOException {
        Configuration config = Configuration.getInstance(ConfigurationTester.class.getResource("test_configuration.xml").getPath());
        Configuration.VersionOneConnection v1 = config.getVersionOneConnection();
        Configuration.GitSettings git = config.getGitSettings();
        Configuration.Link link = config.getLink();
        Assert.assertEquals("VersionOne path is incorrect.", "http://VersionOne.com/VersionOne/", v1.getPath());
        Assert.assertEquals("VersionOne user name is incorrect.", "admin", v1.getUserName());
        Assert.assertEquals("VersionOne password is incorrect.", "adminpass", v1.getPassword());
        Assert.assertEquals("Git repository path is incorrect.", "github.com/Account.git", git.getRepositoryPath());
        Assert.assertEquals("Git password is incorrect.", "password", git.getPassword());
        Assert.assertEquals("Git passphrase is incorrect.", "passphrase", git.getPassphrase());
        Assert.assertEquals("Git branch name is incorrect.", "master", git.getWatchedBranch());
        Assert.assertEquals("Git local directory is incorrect.", "e:/gittmp/", git.getLocalDirectory());
        Assert.assertEquals("Incorrect settings for processing through branch name.", false, config.getProcessingThroughBranchesName());
        Assert.assertEquals("Incorrect settings for timeout.", 10000, config.getTimeoutMillis());
        Assert.assertEquals("Incorrect reference attribute name.", "Number", config.getReferenceAttribute());
        Assert.assertEquals("Incorrect reference expression pattern.", "[A-Z]{1,2}-[0-9]+", config.getReferenceExpression());
        Assert.assertEquals("Incorrect link name template.", "ChangeSet: {0}", link.getLinkNameTemplate());
        Assert.assertEquals("Incorrect link URL template.", "http://github.com/account/{0}", link.getLinkUrlTemplate());
        Assert.assertEquals("Incorrect show on menu settings.", true, link.getLinkOnMenu());
    }
}
