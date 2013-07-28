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
        Configuration config = Configuration.getInstance("test_configuration.xml");
        VersionOneConnection v1 = config.getVersionOneConnection();
        ProxySettings proxy = v1.getProxySettings();

        Assert.assertEquals("Incorrect amount of git repositories.", 4, config.getGitConnections().size());

        GitConnection git = config.getGitConnections().get(0);
        ChangeSet changeSet = config.getChangeSet();
        Link link = git.getLink();

        Assert.assertEquals("VersionOne path is incorrect.", "http://server/VersionOne/", v1.getPath());
        Assert.assertEquals("VersionOne user name is incorrect.", "admin", v1.getUserName());
        Assert.assertEquals("VersionOne password is incorrect.", "admin", v1.getPassword());
        Assert.assertEquals("Integrated Windows Authentication is not correct .", true, v1.getIntegratedAuth());
        Assert.assertEquals("Incorrect settings for using proxy.", false, proxy.getUseProxy());
        Assert.assertEquals("Proxy path is incorrect.", "http://proxy:3128/", proxy.getPath());
        Assert.assertEquals("Proxy user name is incorrect.", "proxyUser", proxy.getUserName());
        Assert.assertEquals("Proxy password is incorrect.", "proxyUserPass", proxy.getPassword());
        Assert.assertEquals("Git repository path is incorrect.", "https://github.com/account/repo.git", git.getRepositoryPath());
        Assert.assertEquals("Git password is incorrect.", "password", git.getPassword());
        Assert.assertEquals("Git passphrase is incorrect.", "passphrase", git.getPassphrase());
        Assert.assertEquals("Git branch name is incorrect.", "master", git.getWatchedBranch());
        Assert.assertEquals("Git ingore branch filter is incorrect", "personal", git.getBranchFilter());
        Assert.assertEquals("Git local directory is incorrect.", "./repos", config.getLocalDirectory());
        Assert.assertEquals("Incorrect settings for processing through branch name.", false, git.getUseBranchName());
        Assert.assertEquals("Incorrect setting for polling interval in seconds.", 300, config.getPollIntervalInSeconds());
        Assert.assertEquals("Incorrect reference attribute name.", "Number", changeSet.getReferenceAttribute());
        Assert.assertEquals("Incorrect reference expression pattern.", "[A-Z]{1,2}-[0-9]+", changeSet.getReferenceExpression());
        Assert.assertEquals("Incorrect link name template.", "ChangeSet: {0}", link.getNameTemplate());
        Assert.assertEquals("Incorrect link URL template.", "https://github.com/account/repo/{0}", link.getUrlTemplate());
        Assert.assertEquals("Incorrect show on menu settings.", true, link.isOnMenu());
        Assert.assertEquals("Incorrect comment for update.", "Updated by Git", changeSet.getChangeComment());
        Assert.assertEquals("Incorrect always create settings.", false, changeSet.isAlwaysCreate());

        GitConnection git2 = config.getGitConnections().get(2);
        Assert.assertEquals("Git repository path is incorrect.", "git@git.yourcompany.com:account/repo.git", git2.getRepositoryPath());
        Assert.assertEquals("Git password is incorrect.", null, git2.getPassword());
        Assert.assertEquals("Git passphrase is incorrect.", null, git2.getPassphrase());
        Assert.assertEquals("Git branch name is incorrect.", null, git2.getWatchedBranch());
        Assert.assertEquals("Incorrect settings for processing through branch name.", false, git2.getUseBranchName());
        Assert.assertEquals("Incorrect settings for ignore branc filter", null, git2.getBranchFilter());
    }

    @Test
    public void gitConnectionNotEquals() {
        Configuration config = Configuration.getInstance("test_configuration.xml");
        GitConnection git1 = config.getGitConnections().get(0);
        GitConnection git2 = config.getGitConnections().get(2);

        Assert.assertFalse(git1.equals(git2));
    }

    @Test
    public void gitConnectionEquals() {
        Configuration config = Configuration.getInstance("test_configuration.xml");
        GitConnection git1 = config.getGitConnections().get(0);
        GitConnection git2 = config.getGitConnections().get(1);

        Assert.assertTrue(git1.equals(git2));
    }
}
