package com.versionone.git;

import com.versionone.git.configuration.*;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;

/**
 * Tester for configuration
 */
public class ConfigurationTester {
    @Test
    public void configTest() throws IOException {
        Configuration config = Configuration.getInstance(ConfigurationTester.class.getResource("test_configuration.xml").getPath());
        VersionOneSettings v1 = config.getVersionOneSettings();
        ProxySettings proxy = v1.getProxySettings();
        GitSettings git = config.getGitSettings();
        Link link = config.getLink();

        Assert.assertEquals("VersionOne path is incorrect.", "http://VersionOne.com/VersionOne/", v1.getPath());
        Assert.assertEquals("VersionOne user name is incorrect.", "admin", v1.getUserName());
        Assert.assertEquals("VersionOne password is incorrect.", "adminpass", v1.getPassword());
        Assert.assertEquals("Integrated Windows Authentication is not correct .", true, v1.getIntegratedAuth());
        Assert.assertEquals("Incorrect settings for using proxy.", false, proxy.getUseProxy());
        Assert.assertEquals("Proxy path is incorrect.", "http://proxy:3128/", proxy.getPath());
        Assert.assertEquals("Proxy user name is incorrect.", "proxyUser", proxy.getUserName());
        Assert.assertEquals("Proxy password is incorrect.", "proxyUserPass", proxy.getPassword());
        Assert.assertEquals("Git repository path is incorrect.", "github.com/Account.git", git.getRepositoryPath());
        Assert.assertEquals("Git password is incorrect.", "password", git.getPassword());
        Assert.assertEquals("Git passphrase is incorrect.", "passphrase", git.getPassphrase());
        Assert.assertEquals("Git branch name is incorrect.", "master", git.getWatchedBranch());
        Assert.assertEquals("Git local directory is incorrect.", "e:/gittmp/", git.getLocalDirectory());
        Assert.assertEquals("Incorrect settings for processing through branch name.", false, config.getUseBranchName());
        Assert.assertEquals("Incorrect settings for timeout.", 10000, config.getTimeoutMillis());
        Assert.assertEquals("Incorrect reference attribute name.", "Number", config.getReferenceAttribute());
        Assert.assertEquals("Incorrect reference expression pattern.", "[A-Z]{1,2}-[0-9]+", config.getReferenceExpression());
        Assert.assertEquals("Incorrect link name template.", "ChangeSet: {0}", link.getLinkNameTemplate());
        Assert.assertEquals("Incorrect link URL template.", "http://github.com/account/{0}", link.getLinkUrlTemplate());
        Assert.assertEquals("Incorrect show on menu settings.", true, link.isLinkOnMenu());
        Assert.assertEquals("Incorrect comment for update.", "Updated by VersionOne.ServiceHost", config.getChangeComment());
        Assert.assertEquals("Incorrect always create settings.", false, config.isAlwaysCreate());
    }
}
