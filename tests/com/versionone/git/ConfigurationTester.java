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
    public void config() throws IOException {
        Configuration config = Configuration.getInstance(ConfigurationTester.class.getResource("test_configuration.xml").getPath());
        VersionOneSettings v1 = config.getVersionOneSettings();
        ProxySettings proxy = v1.getProxySettings();

        Assert.assertEquals("Incorrect amount of git repositories.", 3, config.getGitSettings().size());

        GitSettings git = config.getGitSettings().get(0);
        Link link = git.getLink();
        Assert.assertEquals("VersionOne path is incorrect.", "http://integsrv01/VersionOne12/", v1.getPath());
        Assert.assertEquals("VersionOne user name is incorrect.", "admin", v1.getUserName());
        Assert.assertEquals("VersionOne password is incorrect.", "admin", v1.getPassword());
        Assert.assertEquals("Integrated Windows Authentication is not correct .", true, v1.getIntegratedAuth());
        Assert.assertEquals("Incorrect settings for using proxy.", false, proxy.getUseProxy());
        Assert.assertEquals("Proxy path is incorrect.", "http://proxy:3128/", proxy.getPath());
        Assert.assertEquals("Proxy user name is incorrect.", "proxyUser", proxy.getUserName());
        Assert.assertEquals("Proxy password is incorrect.", "proxyUserPass", proxy.getPassword());
        Assert.assertEquals("Git repository path is incorrect.", "github.com/Account.git", git.getRepositoryPath());
        Assert.assertEquals("Git password is incorrect.", "password", git.getPassword());
        Assert.assertEquals("Git passphrase is incorrect.", "passphrase", git.getPassphrase());
        Assert.assertEquals("Git branch name is incorrect.", "master", git.getWatchedBranch());
        Assert.assertEquals("Git local directory is incorrect.", "e:/gittmp/", config.getLocalDirectory());
        Assert.assertEquals("Incorrect settings for processing through branch name.", false, git.getUseBranchName());
        Assert.assertEquals("Incorrect settings for timeout.", 10000, config.getTimeoutMillis());
        Assert.assertEquals("Incorrect reference attribute name.", "Number", config.getReferenceAttribute());
        Assert.assertEquals("Incorrect reference expression pattern.", "[A-Z]{1,2}-[0-9]+", config.getReferenceExpression());
        Assert.assertEquals("Incorrect link name template.", "ChangeSet: {0}", link.getLinkNameTemplate());
        Assert.assertEquals("Incorrect link URL template.", "http://github.com/account/{0}", link.getLinkUrlTemplate());
        Assert.assertEquals("Incorrect show on menu settings.", true, link.isLinkOnMenu());
        Assert.assertEquals("Incorrect comment for update.", "Updated by VersionOne.ServiceHost", config.getChangeComment());
        Assert.assertEquals("Incorrect always create settings.", false, config.isAlwaysCreate());

        GitSettings git2 = config.getGitSettings().get(1);
        Assert.assertEquals("Git repository path is incorrect.", "github.com/Account2.git", git2.getRepositoryPath());
        Assert.assertEquals("Git password is incorrect.", "password2", git2.getPassword());
        Assert.assertEquals("Git passphrase is incorrect.", "passphrase2", git2.getPassphrase());
        Assert.assertEquals("Git branch name is incorrect.", "master2", git2.getWatchedBranch());
        Assert.assertEquals("Incorrect settings for processing through branch name.", true, git2.getUseBranchName());
    }

    @Test
    public void gitSettingsNotEquals() {
        Configuration config = Configuration.getInstance(ConfigurationTester.class.getResource("test_configuration.xml").getPath());
        GitSettings git1 = config.getGitSettings().get(0);
        GitSettings git2 = config.getGitSettings().get(1);

        Assert.assertFalse(git1.equals(git2));
    }

    @Test
    public void gitSettingsEquals() {
        Configuration config = Configuration.getInstance(ConfigurationTester.class.getResource("test_configuration.xml").getPath());
        GitSettings git1 = config.getGitSettings().get(0);
        GitSettings git2 = config.getGitSettings().get(2);

        Assert.assertTrue(git1.equals(git2));
    }
}
