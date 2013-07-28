package com.versionone.git;

import com.versionone.git.configuration.ChangeSet;
import com.versionone.git.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ChangeSetInfoTester {

    private Configuration config;
    ChangeSet changeSetConfig;

    @Test
    public void shouldReplaceArgsInNameTemplate() {
        loadConfig("test_configuration.xml");
        String name = getTestChange(0).getName(changeSetConfig);

        Assert.assertEquals("Placeholder {0} was still present", false, name.contains("{0}"));
        Assert.assertEquals("Commit date/time was not inserted", true, name.contains("2012-01-31 23:59:59"));

        Assert.assertEquals("Placeholder {1} was still present", false, name.contains("{1}"));
        Assert.assertEquals("Author was not inserted", true, name.contains("Author"));

        Assert.assertEquals("Placeholder {2} was still present", false, name.contains("{2}"));
        Assert.assertEquals("Repository path was not inserted", true, name.contains("account/repo.git"));
    }

    @Test
    public void shouldFormatRepoPathsInNameTemplate() {

        // Test the different formatting options
        loadConfig("test_changesetinfo_fullpath.xml");
        String name = getTestChange(0).getName(changeSetConfig);
        Assert.assertEquals("in https://github.com/account/repo.git", name.substring(name.indexOf("in")));

        loadConfig("test_configuration.xml");
        name = getTestChange(0).getName(changeSetConfig);
        Assert.assertEquals("in account/repo.git", name.substring(name.indexOf("in")));

        loadConfig("test_changesetinfo_nameonly.xml");
        name = getTestChange(0).getName(changeSetConfig);
        Assert.assertEquals("in repo.git", name.substring(name.indexOf("in")));

        // Test the different URI formats
        testGitConnectionUri(0);
        testGitConnectionUri(2);
        testGitConnectionUri(3);
    }

    private void testGitConnectionUri(int gitConnectionIndex) {
        loadConfig("test_configuration.xml");
        String name  = getTestChange(gitConnectionIndex).getName(changeSetConfig);
        Assert.assertEquals("in account/repo.git", name.substring(name.indexOf("in")));
    }

    private void loadConfig(String configFile) {
        Configuration.reset();
        config = Configuration.getInstance(configFile);
        changeSetConfig = config.getChangeSet();
    }

    private ChangeSetInfo getTestChange(int connectionIndex) {
        Calendar cal =  GregorianCalendar.getInstance();
        cal.set(2012, Calendar.JANUARY, 31, 23, 59, 59);
        Date changeDate = cal.getTime();

        ArrayList<String> affectedWorkitems = new ArrayList<String>();
        affectedWorkitems.add("S-123456");

        return new ChangeSetInfo(
                config.getGitConnections().get(connectionIndex),
                "Author",
                "Description",
                null,
                "COMMITHASH",
                changeDate,
                affectedWorkitems
        );
    }
}