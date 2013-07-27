package com.versionone.git;

import com.versionone.apiclient.*;
import com.versionone.git.configuration.Configuration;
import com.versionone.git.configuration.VersionOneConnection;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.DateFormat;
import java.util.*;

@RunWith(value = Parameterized.class)
public class ChangeSetWriterTester {
    @Parameterized.Parameters
    public static Collection<Object[]> generateData() {
      Object[][] data = new Object[][] {
              {true, "B-01013"},
              {false, "B-01014"}
      };

      return Arrays.asList(data);
    }

    private Configuration config;
    private IAssetType changeSetType;

    private final String changeSetTypeDef = "ChangeSet";
    private final String referenceAttrDef = "Reference";
    private final String linksAttrDef = "Links.URL";
    private final String nameAttrDef = "Name";
    private final String descriptionAttrDef = "Description";
    
    private final String workitemNumber;
    private final Boolean useProxy;

    private IVersionOneConnector connector;

    public ChangeSetWriterTester(boolean useProxy, String workitemNumber) {
        this.useProxy = useProxy;
        this.workitemNumber = workitemNumber;
    }

    @Before
    public void before() throws VersionOneException {
        config = Configuration.getInstance(ConfigurationTester.class.getResource("test_configuration_changesetwriter.xml").getPath());
        VersionOneConnection connectionInfo = config.getVersionOneConnection();

        connectionInfo.getProxySettings().setUseProxy(useProxy);

        connector = new VersionOneConnector();
        connector.connect(connectionInfo);
        changeSetType = connector.getMetaModel().getAssetType(changeSetTypeDef);    
    }

    @Test
    @Ignore("Integration test. Set real workitem in generateData() method.")
    public void publish() throws VersionOneException, APIException, OidException, ConnectionException {
        List<String> refs = new LinkedList<String>();
        refs.add(workitemNumber);

        Calendar cal =  GregorianCalendar.getInstance();
        cal.set(2010, 11, 1, 14, 43, 56);
        Date date = cal.getTime();

        ChangeSetInfo changeSet = new ChangeSetInfo(config.getGitConnections().get(0), "test author", "test message",  new LinkedList<String>(),
                "test_revision", date, refs);

        ChangeSetWriter writer = new ChangeSetWriter(config.getChangeSet(), connector);
        writer.publish(changeSet);

        Asset[] list = findExistingChangeset(changeSet.getRevision()).getAssets();
        Assert.assertEquals("Affected change sets count is incorrect:", 1, list.length);

        Asset changeSetFromV1 = list[0];

        String changeSetName = String.format("'%1$s' on '%2$s'", changeSet.getAuthor(),
                getFormattedTime(changeSet.getChangeDate()));

        Assert.assertEquals("Name is incorrect:",
                changeSetFromV1.getAttribute(changeSetType.getAttributeDefinition(nameAttrDef)).getValue().toString(),
                changeSetName);

        Assert.assertEquals("Description is incorrect:",
                changeSetFromV1.getAttribute(changeSetType.getAttributeDefinition(descriptionAttrDef)).getValue().toString(),
                changeSet.getMessage());

        Assert.assertEquals("Reference is incorrect:",
                changeSetFromV1.getAttribute(changeSetType.getAttributeDefinition(referenceAttrDef)).getValue().toString(),
                changeSet.getRevision());

        Assert.assertNotNull("Link is null:",
                changeSetFromV1.getAttribute(changeSetType.getAttributeDefinition(linksAttrDef)).getValue().toString());
    }

     private QueryResult findExistingChangeset(String revision) throws OidException, APIException, ConnectionException {
        FilterTerm term = new FilterTerm(changeSetType.getAttributeDefinition(referenceAttrDef));
        term.equal(revision);

        Query q = new Query(changeSetType);
        q.getSelection().add(changeSetType.getAttributeDefinition(nameAttrDef));
        q.getSelection().add(changeSetType.getAttributeDefinition(descriptionAttrDef));
        q.getSelection().add(changeSetType.getAttributeDefinition(referenceAttrDef));
        q.getSelection().add(changeSetType.getAttributeDefinition(linksAttrDef));
        q.setFilter(term);
        q.setPaging(new Paging(0, 1));

        return connector.getServices().retrieve(q);
    }

    private String getFormattedTime(Date changeDate) {
        DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
        String formattedChangeDate = dateFormatter.format(changeDate);
        return String.format("%1$s UTC%2$tz", formattedChangeDate, changeDate);
    }
}
